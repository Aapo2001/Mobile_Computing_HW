package com.example.myapplication.ui.sensor

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.service.SensorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SensorUiState(
    val hasNotificationPermission: Boolean = true,
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f,
    val shakeCount: Int = 0,
    val isServiceRunning: Boolean = false,
    val isBound: Boolean = false
)

class SensorViewModel(
    private val context: Context,
    initialShakeCount: Int = 0
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SensorUiState(
            hasNotificationPermission = checkNotificationPermission(),
            shakeCount = initialShakeCount
        )
    )
    val uiState: StateFlow<SensorUiState> = _uiState.asStateFlow()

    private var sensorService: SensorService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SensorService.LocalBinder
            sensorService = binder.getService()
            sensorService?.onSensorDataChanged = { x, y, z, count ->
                _uiState.update { it.copy(
                    accelX = x,
                    accelY = y,
                    accelZ = z,
                    shakeCount = count
                )}
            }
            _uiState.update { it.copy(isServiceRunning = true, isBound = true) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            sensorService = null
            _uiState.update { it.copy(isServiceRunning = false, isBound = false) }
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasNotificationPermission = granted) }
    }

    fun startSensorService() {
        val intent = Intent(context, SensorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun stopSensorService() {
        if (_uiState.value.isBound) {
            try {
                context.unbindService(serviceConnection)
            } catch (_: IllegalArgumentException) { }
            _uiState.update { it.copy(isBound = false) }
        }
        val intent = Intent(context, SensorService::class.java)
        context.stopService(intent)
        _uiState.update { it.copy(isServiceRunning = false) }
        sensorService = null
    }

    fun resetShakeCount() {
        sensorService?.resetShakeCount()
        _uiState.update { it.copy(shakeCount = 0) }
    }

    fun unbindService() {
        if (_uiState.value.isBound) {
            try {
                context.unbindService(serviceConnection)
            } catch (_: IllegalArgumentException) { }
            _uiState.update { it.copy(isBound = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }

    companion object {
        fun provideFactory(
            context: Context,
            initialShakeCount: Int = 0
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SensorViewModel(context, initialShakeCount) as T
            }
        }
    }
}
