package com.example.myapplication.ui.map

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.navigation.MapDest
import com.example.myapplication.navigation.NavBar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    currentDestination: NavDestination?
) {
    val context = LocalContext.current

    val viewModel: MapViewModel = viewModel(
        factory = MapViewModel.provideFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()

    // Default location (Helsinki, Finland)
    val defaultLocation = LatLng(60.1699, 24.9384)

    // Camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    // Map UI settings
    val mapUiSettings = MapUiSettings(
        zoomControlsEnabled = true,
        myLocationButtonEnabled = false // We'll use our own button
    )

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(fineGranted, coarseGranted)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            NavBar(title = MapDest.label)
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentDestination = currentDestination
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    uiState.currentLocation?.let { location ->
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(location, 15f)
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            // Permission Card
            if (!uiState.hasLocationPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location"
                            )
                            Text(
                                text = "Location Permission Required",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = "Grant location permission to see your position on the map and enable context-aware features.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Location info card (context-aware)
            if (uiState.hasLocationPermission && uiState.currentLocation != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Your Location",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.currentLocation?.let { loc ->
                            Text(
                                text = "Lat: %.4f, Lng: %.4f".format(loc.latitude, loc.longitude),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = "Tap on map to add markers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Map
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = uiState.mapProperties,
                    uiSettings = mapUiSettings,
                    onMapClick = { latLng ->
                        viewModel.addMarker(latLng)
                    }
                ) {
                    // User-added markers
                    uiState.markers.forEachIndexed { index, position ->
                        Marker(
                            state = MarkerState(position = position),
                            title = "Marker ${index + 1}",
                            snippet = "Lat: %.4f, Lng: %.4f".format(position.latitude, position.longitude)
                        )
                    }

                    // Current location marker (if available)
                    uiState.currentLocation?.let { location ->
                        Marker(
                            state = MarkerState(position = location),
                            title = "You are here",
                            snippet = "Your current location"
                        )
                    }
                }
            }
        }
    }
}
