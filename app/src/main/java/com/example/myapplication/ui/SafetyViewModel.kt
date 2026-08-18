package com.example.myapplication.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.EmergencyContact
import com.example.myapplication.data.EmergencyIncident
import com.example.myapplication.data.SafetyRepository
import com.example.myapplication.logic.EmergencyTriggerManager
import com.example.myapplication.logic.LocationInfo
import com.example.myapplication.logic.SOSState
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AntiRaggingReport(
    val id: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SOSHistoryItem(
    val id: String,
    val timestamp: String,
    val type: String = "SOS Alert",
    val status: String = "Resolved"
)

data class ProtectionStatus(
    val locationPermission: Boolean,
    val cameraPermission: Boolean,
    val micPermission: Boolean,
    val internetAvailable: Boolean,
    val contactsConfigured: Boolean
)

class SafetyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SafetyRepository(application)
    private val triggerManager = EmergencyTriggerManager.getInstance(application, repository)

    val sosState: StateFlow<SOSState> = triggerManager.state
    val isTestMode: StateFlow<Boolean> = triggerManager.isTestMode
    val elapsedTime: StateFlow<Long> = triggerManager.elapsedTime
    val location: StateFlow<LocationInfo?> = triggerManager.location
    val countdown: StateFlow<Int> = triggerManager.countdown
    val isManualRecording: StateFlow<Boolean> = triggerManager.isManualRecording
    val manualRecordingTime: StateFlow<Long> = triggerManager.manualRecordingTime
    val currentIncidentId: StateFlow<String?> = triggerManager.currentIncidentId

    val contacts: StateFlow<List<EmergencyContact>> = repository.getContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incidents: StateFlow<List<EmergencyIncident>> = repository.getIncidents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<com.example.myapplication.data.SafetySettings> = repository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.myapplication.data.SafetySettings())

    fun updateSettings(settings: com.example.myapplication.data.SafetySettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            val context = getApplication<Application>().applicationContext
            if (settings.shakeDetection) {
                com.example.myapplication.logic.ShakeTriggerService.start(context)
            } else {
                com.example.myapplication.logic.ShakeTriggerService.stop(context)
            }
        }
    }

    private val _reports = MutableStateFlow<List<AntiRaggingReport>>(emptyList())
    val reports: StateFlow<List<AntiRaggingReport>> = _reports

    private val _policeResponseEnabled = MutableStateFlow(false)
    val policeResponseEnabled: StateFlow<Boolean> = _policeResponseEnabled

    private val _demoModeEnabled = MutableStateFlow(false)
    val demoModeEnabled: StateFlow<Boolean> = _demoModeEnabled

    private val _wardenNumber = MutableStateFlow("9876543210")
    val wardenNumber: StateFlow<String> = _wardenNumber

    private val _showWhatsAppDisclosure = MutableStateFlow(false)
    val showWhatsAppDisclosure: StateFlow<Boolean> = _showWhatsAppDisclosure

    private val _whatsAppNumber = MutableStateFlow("")
    val whatsAppNumber: StateFlow<String> = _whatsAppNumber

    val protectionStatus: StateFlow<ProtectionStatus> = combine(
        contacts,
        MutableStateFlow(application) // Just to trigger initial check
    ) { contactList, app ->
        val context = app.applicationContext
        ProtectionStatus(
            locationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
            cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
            micPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED,
            internetAvailable = isInternetAvailable(context),
            contactsConfigured = contactList.isNotEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProtectionStatus(false, false, false, false, false))

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    init {
        triggerManager.onSOSActivated = {
            initiateAutomaticEmergencyCall()
            // Wait for non-null location then share
            viewModelScope.launch {
                triggerManager.location.collect { loc ->
                    if (loc != null) {
                        sendAutomaticAlerts()
                        this.cancel() // Only send the first one automatically
                    }
                }
            }
        }

        // Ensure Shake Detection Service is running if enabled
        viewModelScope.launch {
            settings.collectLatest { safetySettings ->
                val context = getApplication<Application>().applicationContext
                if (safetySettings.shakeDetection) {
                    com.example.myapplication.logic.ShakeTriggerService.start(context)
                }
            }
        }
    }

    private fun initiateAutomaticEmergencyCall() {
        val context = getApplication<Application>().applicationContext
        val allContacts = contacts.value
        val primaryContact = allContacts.find { it.isPrimary } ?: allContacts.firstOrNull()
        
        primaryContact?.let {
            makeEmergencyCall(context, it.phoneNumber)
        } ?: run {
            // Fallback to Warden if no contacts saved
            makeEmergencyCall(context, _wardenNumber.value)
        }
    }

    // Contact Management
    fun addContact(name: String, number: String, relationship: String) {
        viewModelScope.launch {
            val contact = EmergencyContact(
                id = UUID.randomUUID().toString(),
                name = name,
                phoneNumber = number,
                relationship = relationship
            )
            repository.addContact(contact)
        }
    }

    fun updateContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.updateContact(contact)
        }
    }

    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            repository.deleteContact(contactId)
        }
    }

    fun setPrimaryContact(contactId: String) {
        viewModelScope.launch {
            repository.setPrimaryContact(contactId)
        }
    }

    fun getIncidentDetails(id: String) = repository.getIncidentById(id)

    fun dismissWhatsAppDisclosure() {
        _showWhatsAppDisclosure.value = false
    }

    private fun sendAutomaticAlerts() {
        val context = getApplication<Application>().applicationContext
        _showWhatsAppDisclosure.value = true
        shareSOSAlertToAllContacts(context)
    }

    private fun shareSOSAlertToAllContacts(context: Context) {
        val allContacts = contacts.value
        if (allContacts.isEmpty()) return

        // 1. Send ZERO-CLICK Background SMS to EVERY saved contact immediately
        allContacts.forEach { contact ->
            sendBackgroundSMS(context, contact.phoneNumber)
        }

        // 2. Prepare WhatsApp rich-media alert for the Primary Contact (Requires 1-click)
        val primaryContact = allContacts.find { it.isPrimary } ?: allContacts.first()
        shareLocationToWhatsApp(context, primaryContact.phoneNumber)
    }

    private fun sendBackgroundSMS(context: Context, number: String) {
        if (number.isBlank()) return
        
        val loc = location.value
        val lat = loc?.latitude ?: 0.0
        val lng = loc?.longitude ?: 0.0
        val incidentId = triggerManager.currentIncidentId.value ?: "Unknown"
        
        // Use the requested Google Maps search API link
        val link = "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
        val timestamp = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(Date())
        
        val message = "🚨 EMERGENCY ALERT!\nIncident ID: $incidentId\nLocation: $link\nTime: $timestamp\nI need help immediately."

        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(number, null, parts, null, null)
                Log.d("SafetyViewModel", "Background SMS sent to $number")
            } else {
                Log.e("SafetyViewModel", "SEND_SMS permission not granted")
            }
        } catch (e: Exception) {
            Log.e("SafetyViewModel", "Failed to send background SMS to $number", e)
        }
    }

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        triggerManager.lifecycleOwner = lifecycleOwner
    }

    fun setWhatsAppNumber(number: String) {
        _whatsAppNumber.value = number
    }

    fun shareLocationToWhatsApp(context: Context, number: String) {
        val loc = location.value
        val lat = loc?.latitude ?: 0.0
        val lng = loc?.longitude ?: 0.0
        val link = "https://www.google.com/maps?q=$lat,$lng"
        val timestamp = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(Date())
        
        val message = "🚨 NIRBHAYA SOS ALERT! I need help immediately. My Live Location: $link (Time: $timestamp)"
        
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$number&text=${Uri.encode(message)}")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SafetyViewModel", "WhatsApp share failed", e)
        }
    }

    fun triggerSOS() {
        triggerManager.triggerSOS(isTest = false)
        simulateHapticFeedback()
    }

    fun startTestSOS() {
        triggerManager.triggerSOS(isTest = true)
        simulateHapticFeedback()
    }
    
    fun cancelSOS() = triggerManager.cancelSOS()
    fun simulateFourPress() = triggerManager.simulateFourPressTrigger()

    fun togglePoliceResponse(enabled: Boolean) {
        _policeResponseEnabled.value = enabled
    }

    fun toggleDemoMode(enabled: Boolean) {
        _demoModeEnabled.value = enabled
    }

    private fun simulateHapticFeedback() {
        println("Simulating Haptic Feedback")
    }

    fun setWardenNumber(number: String) {
        _wardenNumber.value = number
    }

    fun callWarden(context: Context) {
        val wardenContact = contacts.value.find { it.name.equals("warden", ignoreCase = true) }
        if (wardenContact != null) {
            makeEmergencyCall(context, wardenContact.phoneNumber)
        } else {
            android.widget.Toast.makeText(context, "Please add a contact named 'warden' first", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    fun makeEmergencyCall(context: Context, phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            // Fallback to dialer if permission not granted
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun checkPermissions(context: Context): Boolean {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CALL_PHONE
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun shareCurrentLocation(context: Context) {
        val loc = location.value
        if (loc == null) {
            Log.e("SafetyViewModel", "Location not available")
            return
        }
        val lat = loc.latitude
        val lng = loc.longitude
        val link = "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My Current Location")
            putExtra(Intent.EXTRA_TEXT, "I'm sharing my location for safety: $link (Accuracy: ${loc.accuracy}m)")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Location via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun manualVideoRecording(context: Context) {
        // Trigger manual recording. Since startRecording needs a LifecycleOwner, 
        // we assume the UI provides it via setLifecycleOwner.
        triggerManager.lifecycleOwner?.let {
            triggerManager.startManualRecording(it)
        }
    }

    fun stopManualRecording() {
        triggerManager.stopManualRecording()
    }

    fun submitAntiRaggingReport(description: String): String {
        val id = "SC-${UUID.randomUUID().toString().take(5).uppercase()}"
        val report = AntiRaggingReport(id, description)
        _reports.value += report
        return id
    }

    fun deleteMedia(uriString: String) {
        viewModelScope.launch {
            // 1. Remove from Repository
            repository.deleteMedia(uriString)
            
            // 2. Attempt to delete from disk
            try {
                val uri = Uri.parse(uriString)
                getApplication<Application>().contentResolver.delete(uri, null, null)
                Log.d("SafetyViewModel", "Deleted media file: $uriString")
            } catch (e: Exception) {
                Log.e("SafetyViewModel", "Failed to delete file from disk: $uriString", e)
            }
        }
    }
}
