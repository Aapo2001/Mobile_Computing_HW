package com.example.myapplication.service

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import com.example.myapplication.notification.NotificationHelper
import kotlin.math.sqrt

class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var notificationHelper: NotificationHelper

    private val binder = LocalBinder()
    private var isSensorRegistered = false
    private var lastShakeTime: Long = 0
    private var shakeCount: Int = 0
    private val shakeThreshold = 12.0f
    private val shakeTimeWindow = 500L

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


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = notificationHelper.createServiceNotification()
        startForeground(
            NotificationHelper.SERVICE_NOTIFICATION_ID,
            notification,
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        registerSensorListener()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        registerSensorListener()
        return binder
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]
                val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH
                if (acceleration > shakeThreshold) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastShakeTime > shakeTimeWindow) {
                        lastShakeTime = currentTime
                        shakeCount++
                        if (shakeCount % 3 == 0) {
                            notificationHelper.showShakeNotification(shakeCount)
                        }
                    }
                }
                onSensorDataChanged?.invoke(x, y, z, shakeCount)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    fun resetShakeCount() {
        shakeCount = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isSensorRegistered) {
            sensorManager.unregisterListener(this)
            isSensorRegistered = false
        }
    }
}
