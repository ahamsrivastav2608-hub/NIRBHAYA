package com.example.myapplication.logic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.SafetyRepository
import com.example.myapplication.data.TimelineEvent
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.*

class EmergencyService : LifecycleService() {

    private lateinit var repository: SafetyRepository
    private lateinit var recordingManager: RecordingManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    // Use GlobalScope or a non-cancelled scope for finalization to ensure DB updates complete
    private val persistenceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentIncidentId: String? = null

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "emergency_service_channel"
        const val ACTION_START_SOS = "ACTION_START_SOS"
        const val ACTION_STOP_SOS = "ACTION_STOP_SOS"
        const val EXTRA_INCIDENT_ID = "EXTRA_INCIDENT_ID"
        const val EXTRA_IS_TEST = "EXTRA_IS_TEST"
    }

    override fun onCreate() {
        super.onCreate()
        repository = SafetyRepository(applicationContext)
        recordingManager = RecordingManager(applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        recordingManager.onRecordingFinalized = { uri ->
            Log.d("EmergencyService", "Video finalizing: $uri")
            updateIncidentWithVideo(uri.toString())
        }
        recordingManager.onAudioRecordingFinalized = { uri ->
            Log.d("EmergencyService", "Audio finalizing: $uri")
            updateIncidentWithAudio(uri.toString())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        when (intent?.action) {
            ACTION_START_SOS -> {
                val incidentId = intent.getStringExtra(EXTRA_INCIDENT_ID)
                val isTest = intent.getBooleanExtra(EXTRA_IS_TEST, false)
                startEmergency(incidentId, isTest)
            }
            ACTION_STOP_SOS -> {
                stopEmergency()
            }
        }
        
        return START_STICKY
    }

    private fun startEmergency(incidentId: String?, isTest: Boolean) {
        currentIncidentId = incidentId
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isTest) "SOS Test Active" else "SOS Emergency Active")
            .setContentText("Recording and sharing location in background")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(createPendingIntent())
            .build()

        startForeground(NOTIFICATION_ID, notification)
        
        startLocationUpdates()
        recordingManager.startRecording(this)
        Log.d("EmergencyService", "SOS Started: $incidentId, isTest: $isTest")
    }

    private fun stopEmergency() {
        recordingManager.stopRecording()
        stopLocationUpdates()
        // Don't stopSelf immediately, wait a bit for recording finalization callbacks
        // or just let the system clean up. But stopForeground is fine.
        stopForeground(STOP_FOREGROUND_REMOVE)
        
        // We give it 2 seconds to finalize the database entries before killing the service
        persistenceScope.launch {
            delay(2000)
            withContext(Dispatchers.Main) {
                stopSelf()
            }
        }
        Log.d("EmergencyService", "SOS Stopping initiated")
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Emergency SOS Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(3000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val loc = locationResult.lastLocation ?: return
                updateLocationInRepository(loc.latitude, loc.longitude, loc.accuracy)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                mainLooper
            )
        } catch (e: SecurityException) {
            Log.e("EmergencyService", "Location permission missing", e)
        }
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    private fun updateLocationInRepository(lat: Double, lng: Double, accuracy: Float) {
        currentIncidentId?.let { id ->
            persistenceScope.launch {
                val incident = repository.getIncidentById(id).first()
                incident?.let {
                    val updated = it.copy(
                        latitude = lat,
                        longitude = lng,
                        accuracy = accuracy
                    )
                    repository.updateIncident(updated)
                }
            }
        }
    }

    private fun updateIncidentWithVideo(uri: String) {
        val id = currentIncidentId
        if (id != null) {
            persistenceScope.launch {
                val incident = repository.getIncidentById(id).first()
                incident?.let {
                    val updated = it.copy(
                        videoUris = it.videoUris + uri,
                        timeline = it.timeline + TimelineEvent("Recording Finalized (Video)", System.currentTimeMillis())
                    )
                    repository.updateIncident(updated)
                    Log.d("EmergencyService", "Incident updated with video: $id")
                }
            }
        } else {
            Log.e("EmergencyService", "Cannot update incident: currentIncidentId is null")
        }
    }

    private fun updateIncidentWithAudio(uri: String) {
        val id = currentIncidentId
        if (id != null) {
            persistenceScope.launch {
                val incident = repository.getIncidentById(id).first()
                incident?.let {
                    val updated = it.copy(
                        audioUris = it.audioUris + uri,
                        timeline = it.timeline + TimelineEvent("Recording Finalized (Audio)", System.currentTimeMillis())
                    )
                    repository.updateIncident(updated)
                    Log.d("EmergencyService", "Incident updated with audio: $id")
                }
            }
        }
    }

    override fun onDestroy() {
        // We don't cancel persistenceScope here immediately if we want to ensure updates finish
        // but normally onDestroy is the final signal. 
        // If the process is killed, nothing we can do.
        super.onDestroy()
    }
}
