package com.example.myapplication.logic

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.SafetyRepository
import kotlinx.coroutines.*
import kotlin.math.sqrt

class ShakeTriggerService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var repository: SafetyRepository
    private lateinit var triggerManager: EmergencyTriggerManager
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var lastShakeTime: Long = 0
    private val SHAKE_THRESHOLD = 12f
    private val SHAKE_COOLDOWN = 2000L

    companion object {
        private const val CHANNEL_ID = "shake_trigger_channel"
        private const val NOTIFICATION_ID = 202
        
        fun start(context: Context) {
            val intent = Intent(context, ShakeTriggerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            context.stopService(Intent(context, ShakeTriggerService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = SafetyRepository(applicationContext)
        triggerManager = EmergencyTriggerManager.getInstance(applicationContext, repository)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        Log.d("ShakeTriggerService", "Shake detection started in background")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            val acceleration = sqrt((x * x) + (y * y) + (z * z)) - SensorManager.GRAVITY_EARTH
            if (acceleration > SHAKE_THRESHOLD) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTime > SHAKE_COOLDOWN) {
                    lastShakeTime = now
                    Log.d("ShakeTriggerService", "Shake detected in background! Triggering SOS...")
                    triggerSOS()
                }
            }
        }
    }

    private fun triggerSOS() {
        triggerManager.triggerSOS(isTest = false)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Shake Protection Active")
            .setContentText("Nirbhaya is monitoring for emergency shakes")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Shake Detection Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
        super.onDestroy()
        Log.d("ShakeTriggerService", "Shake detection stopped")
    }
}
