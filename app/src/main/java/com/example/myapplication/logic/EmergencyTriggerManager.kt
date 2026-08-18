package com.example.myapplication.logic

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import com.example.myapplication.data.EmergencyIncident
import com.example.myapplication.data.SafetyRepository
import com.example.myapplication.data.TimelineEvent
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

enum class SOSState {
    Idle, Triggering, Active, Cancelled
}

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long = System.currentTimeMillis()
)

class EmergencyTriggerManager private constructor(
    private val context: Context,
    private val repository: SafetyRepository
) {
    private val _state = MutableStateFlow(SOSState.Idle)
    val state: StateFlow<SOSState> = _state

    private val _isTestMode = MutableStateFlow(false)
    val isTestMode: StateFlow<Boolean> = _isTestMode

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    private val _location = MutableStateFlow<LocationInfo?>(null)
    val location: StateFlow<LocationInfo?> = _location

    private val _countdown = MutableStateFlow(3)
    val countdown: StateFlow<Int> = _countdown

    private val _isManualRecording = MutableStateFlow(false)
    val isManualRecording: StateFlow<Boolean> = _isManualRecording

    private val _manualRecordingTime = MutableStateFlow(0L)
    val manualRecordingTime: StateFlow<Long> = _manualRecordingTime

    private var countdownJob: Job? = null
    private var timerJob: Job? = null
    private var manualTimerJob: Job? = null
    private val _currentIncidentId = MutableStateFlow<String?>(null)
    val currentIncidentId: StateFlow<String?> = _currentIncidentId
    
    var lifecycleOwner: LifecycleOwner? = null
    var onSOSActivated: (() -> Unit)? = null

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    private val recordingManager = RecordingManager(context)

    private var powerButtonPressCount = 0
    private var lastPowerButtonPressTime: Long = 0

    private val powerButtonReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF || intent?.action == Intent.ACTION_SCREEN_ON) {
                val now = System.currentTimeMillis()
                if (now - lastPowerButtonPressTime > 1500) {
                    powerButtonPressCount = 1
                } else {
                    powerButtonPressCount++
                }
                lastPowerButtonPressTime = now
                
                if (powerButtonPressCount >= 4) {
                    powerButtonPressCount = 0
                    if (_state.value == SOSState.Idle) {
                        triggerSOS()
                    }
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: EmergencyTriggerManager? = null

        fun getInstance(context: Context, repository: SafetyRepository): EmergencyTriggerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EmergencyTriggerManager(context, repository).also { INSTANCE = it }
            }
        }
    }

    init {
        recordingManager.onRecordingFinalized = { uri ->
            updateIncidentWithVideo(uri.toString())
        }
        recordingManager.onAudioRecordingFinalized = { uri ->
            updateIncidentWithAudio(uri.toString())
        }
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        context.registerReceiver(powerButtonReceiver, filter)
    }

    fun triggerSOS(isTest: Boolean = false) {
        if (_state.value == SOSState.Idle || _state.value == SOSState.Cancelled) {
            _isTestMode.value = isTest
            _state.value = SOSState.Triggering
            startCountdown()
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        _countdown.value = 3
        startLocationUpdates()
        
        countdownJob = CoroutineScope(Dispatchers.Default).launch {
            while (_countdown.value > 0) {
                delay(1.seconds)
                _countdown.value -= 1
            }
            activateSOS()
        }
    }

    private fun activateSOS() {
        _state.value = SOSState.Active
        _elapsedTime.value = 0L
        startTimer()
        
        val loc = _location.value
        val incidentId = UUID.randomUUID().toString()
        _currentIncidentId.value = incidentId
        
        val isTest = _isTestMode.value
        val incidentType = if (isTest) "TEST SOS" else "SOS Alert"
        
        val newIncident = EmergencyIncident(
            id = incidentId,
            type = incidentType,
            timestamp = System.currentTimeMillis(),
            status = if (isTest) "Test" else "Active",
            latitude = loc?.latitude ?: 0.0,
            longitude = loc?.longitude ?: 0.0,
            accuracy = loc?.accuracy ?: 0f,
            timeline = listOf(
                TimelineEvent("$incidentType Triggered", System.currentTimeMillis()),
                TimelineEvent("Location Shared", System.currentTimeMillis())
            ),
            videoUris = emptyList(),
            audioUris = emptyList()
        )
        
        CoroutineScope(Dispatchers.IO).launch {
            repository.addIncident(newIncident)
        }

        val serviceIntent = Intent(context, EmergencyService::class.java).apply {
            action = EmergencyService.ACTION_START_SOS
            putExtra(EmergencyService.EXTRA_INCIDENT_ID, incidentId)
            putExtra(EmergencyService.EXTRA_IS_TEST, isTest)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        if (!isTest) {
            onSOSActivated?.invoke()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            val startTime = System.currentTimeMillis()
            while (_state.value == SOSState.Active) {
                _elapsedTime.value = System.currentTimeMillis() - startTime
                delay(1.seconds)
            }
        }
    }

    private fun updateIncidentWithVideo(uri: String) {
        _currentIncidentId.value?.let { id ->
            CoroutineScope(Dispatchers.IO).launch {
                val incident = repository.getIncidentById(id).first()
                incident?.let {
                    val updated = it.copy(
                        videoUris = it.videoUris + uri,
                        timeline = it.timeline + TimelineEvent("Recording Finalized (Video)", System.currentTimeMillis())
                    )
                    repository.updateIncident(updated)
                }
            }
        }
    }

    private fun updateIncidentWithAudio(uri: String) {
        _currentIncidentId.value?.let { id ->
            CoroutineScope(Dispatchers.IO).launch {
                val incident = repository.getIncidentById(id).first()
                incident?.let {
                    val updated = it.copy(
                        audioUris = it.audioUris + uri,
                        timeline = it.timeline + TimelineEvent("Recording Finalized (Audio)", System.currentTimeMillis())
                    )
                    repository.updateIncident(updated)
                }
            }
        }
    }

    fun cancelSOS() {
        countdownJob?.cancel()
        timerJob?.cancel()
        
        val serviceIntent = Intent(context, EmergencyService::class.java).apply {
            action = EmergencyService.ACTION_STOP_SOS
        }
        context.startService(serviceIntent)

        stopLocationUpdates()
        _state.value = SOSState.Cancelled
        _isTestMode.value = false
        CoroutineScope(Dispatchers.Default).launch {
            delay(2.seconds)
            _state.value = SOSState.Idle
            _currentIncidentId.value = null
        }
    }

    fun simulateFourPressTrigger() {
        triggerSOS()
    }

    fun startManualRecording(lifecycleOwner: LifecycleOwner, previewView: androidx.camera.view.PreviewView? = null) {
        _isManualRecording.value = true
        _manualRecordingTime.value = 0L
        startManualTimer()
        recordingManager.startRecording(lifecycleOwner, previewView)
    }

    private fun startManualTimer() {
        manualTimerJob?.cancel()
        manualTimerJob = CoroutineScope(Dispatchers.Default).launch {
            val startTime = System.currentTimeMillis()
            while (_isManualRecording.value) {
                _manualRecordingTime.value = System.currentTimeMillis() - startTime
                delay(1.seconds)
            }
        }
    }

    fun stopManualRecording() {
        _isManualRecording.value = false
        manualTimerJob?.cancel()
        recordingManager.stopRecording()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(5000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { loc ->
                    _location.value = LocationInfo(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracy = loc.accuracy
                    )
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }
}
