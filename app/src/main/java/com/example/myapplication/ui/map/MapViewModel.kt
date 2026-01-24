package com.example.myapplication.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MapUiState(
    val hasLocationPermission: Boolean = false,
    val currentLocation: LatLng? = null,
    val markers: List<LatLng> = emptyList(),
    val mapProperties: MapProperties = MapProperties(
        isMyLocationEnabled = false,
        mapType = MapType.NORMAL
    )
)

class MapViewModel(
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MapUiState(hasLocationPermission = checkLocationPermission())
    )
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    init {
        if (_uiState.value.hasLocationPermission) {
            fetchCurrentLocation()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun onPermissionResult(fineLocationGranted: Boolean, coarseLocationGranted: Boolean) {
        val hasPermission = fineLocationGranted || coarseLocationGranted
        _uiState.update { it.copy(
            hasLocationPermission = hasPermission,
            mapProperties = it.mapProperties.copy(isMyLocationEnabled = hasPermission)
        )}
        if (hasPermission) {
            fetchCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        if (!_uiState.value.hasLocationPermission) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                _uiState.update { state ->
                    state.copy(currentLocation = LatLng(location.latitude, location.longitude))
                }
            }
        }
    }

    fun addMarker(latLng: LatLng) {
        _uiState.update { it.copy(markers = it.markers + latLng) }
    }

    companion object {
        fun provideFactory(
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MapViewModel(context) as T
            }
        }
    }
}
