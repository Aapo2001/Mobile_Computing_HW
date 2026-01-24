package com.example.myapplication.service

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.example.myapplication.notification.NotificationHelper
import kotlin.math.sqrt

class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var notificationHelper: NotificationHelper

    private val binder = LocalBinder()
    private var isSensorRegistered = false

    // Shake detection variables
    private var lastShakeTime: Long = 0
    private var shakeCount: Int = 0
    private val shakeThreshold = 12.0f  // Acceleration threshold for shake detection
    private val shakeTimeWindow = 500L  // Time window between shakes (ms)

    // Callback for UI updates
    var onSensorDataChanged: ((Float, Float, Float, Int) -> Unit)? = null

    inner class LocalBinder : Binder() {
        fun getService(): SensorService = this@SensorService
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun registerSensorListener() {
        if (!isSensorRegistered) {
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                isSensorRegistered = true
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start as foreground service
        val notification = notificationHelper.createServiceNotification()
        startForeground(
            NotificationHelper.SERVICE_NOTIFICATION_ID,
            notification,
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        // Register sensor listener
        registerSensorListener()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        // Also register sensor when binding (in case onStartCommand was called first)
        registerSensorListener()
        return binder
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                // Calculate acceleration magnitude (excluding gravity approximately)
                val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH

                // Detect shake
                if (acceleration > shakeThreshold) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastShakeTime > shakeTimeWindow) {
                        lastShakeTime = currentTime
                        shakeCount++

                        // Trigger notification every 3 shakes
                        if (shakeCount % 3 == 0) {
                            notificationHelper.showShakeNotification(shakeCount)
                        }
                    }
                }

                // Notify UI of sensor data
                onSensorDataChanged?.invoke(x, y, z, shakeCount)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    fun resetShakeCount() {
        shakeCount = 0
    }

    fun getShakeCount(): Int = shakeCount

    override fun onDestroy() {
        super.onDestroy()
        if (isSensorRegistered) {
            sensorManager.unregisterListener(this)
            isSensorRegistered = false
        }
    }
}
