package com.example.myapplication.ui.sensor

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.navigation.NavBar
import com.example.myapplication.navigation.SensorDest

@Composable
fun SensorScreen(
    modifier: Modifier = Modifier,
    initialShakeCount: Int = 0,
    navController: NavController,
    currentDestination: NavDestination?
) {
    val context = LocalContext.current

    val viewModel: SensorViewModel = viewModel(
        factory = SensorViewModel.provideFactory(context, initialShakeCount)
    )

    val uiState by viewModel.uiState.collectAsState()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            viewModel.unbindService()
        }
    }

    Scaffold(
        topBar = {
            NavBar(title = SensorDest.label)
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentDestination = currentDestination
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                text = "HW4: Sensors & Notifications",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Permission Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.hasNotificationPermission)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
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
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                        Text(
                            text = if (uiState.hasNotificationPermission)
                                "Notification Permission Granted"
                            else
                                "Notification Permission Required",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (!uiState.hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            // Sensor Data Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Sensor",
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Accelerometer Data",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "X: %.2f  Y: %.2f  Z: %.2f m/s²".format(
                            uiState.accelX,
                            uiState.accelY,
                            uiState.accelZ
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Shake Counter Card - M3 styled
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Shake Counter",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.shakeCount}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        FilledTonalButton(
                            onClick = { viewModel.resetShakeCount() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Reset")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Shake device! Notification every 3 shakes.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Service Control - M3 ElevatedCard
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Background Service",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (uiState.isServiceRunning) "Running" else "Stopped",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (uiState.isServiceRunning)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.startSensorService() },
                                enabled = !uiState.isServiceRunning
                            ) {
                                Text("Start")
                            }

                            OutlinedButton(
                                onClick = { viewModel.stopSensorService() },
                                enabled = uiState.isServiceRunning
                            ) {
                                Text("Stop")
                            }
                        }
                    }
                }
            }
        }
    }
}
