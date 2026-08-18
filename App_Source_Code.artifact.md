# Nirbhaya - Emergency & Safety App Source Code

This document contains the complete source code for the Nirbhaya Android application, organized by file path.

---

## 1. Project Configuration

### [build.gradle.kts (App Level)](file:///D:/app%20dev%20dev%20dev/app/build.gradle.kts)
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.myapplication"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    // Google Play Services
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)

    // DataStore & Serialization
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

### [AndroidManifest.xml](file:///D:/app%20dev%20dev%20dev/app/src/main/AndroidManifest.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="28" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.CALL_PHONE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.SEND_SMS" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />

    <uses-feature android:name="android.hardware.camera" android:required="false" />
    <uses-feature android:name="android.hardware.telephony" android:required="false" />
    <uses-feature android:name="android.hardware.location.gps" android:required="false" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">

        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="YOUR_API_KEY_HERE"/>

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".logic.EmergencyService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="location|camera|microphone" />

        <service
            android:name=".logic.ShakeTriggerService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="specialUse">
            <property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_TYPE_DESCRIPTION"
                android:value="Monitoring accelerometer for emergency shake gestures to trigger SOS alerts." />
        </service>

        <receiver
            android:name=".logic.BootReceiver"
            android:enabled="true"
            android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>
    </application>

</manifest>
```

---

## 2. Core Application & UI Logic

### [MainActivity.kt](file:///D:/app%20dev%20dev%20dev/app/src/main/java/com/example/myapplication/MainActivity.kt)
```kotlin
package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.logic.SOSState
import com.example.myapplication.ui.SafetyViewModel
import com.example.myapplication.ui.components.DemoFingerOverlay
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            // Handle denied permissions (e.g., show a message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val safetyViewModel: SafetyViewModel = viewModel()
                val sosState by safetyViewModel.sosState.collectAsState()
                val demoModeEnabled by safetyViewModel.demoModeEnabled.collectAsState()
                val showDisclosure by safetyViewModel.showWhatsAppDisclosure.collectAsState()

                // Set LifecycleOwner for RecordingManager and request permissions
                LaunchedEffect(Unit) {
                    safetyViewModel.setLifecycleOwner(this@MainActivity)

                    val permissions = arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.SEND_SMS
                    )

                    val missingPermissions = permissions.filter {
                        ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                    }

                    if (missingPermissions.isNotEmpty()) {
                        requestPermissionLauncher.launch(missingPermissions.toTypedArray())
                    }
                }

                // WhatsApp Disclosure Dialog
                if (showDisclosure) {
                    AlertDialog(
                        onDismissRequest = { safetyViewModel.dismissWhatsAppDisclosure() },
                        title = { Text("WhatsApp Sharing Disclosure") },
                        text = {
                            Text("To share your location with emergency contacts, WhatsApp will be opened. Please ensure you have it installed and manually share your live location if needed for continuous updates.")
                        },
                        confirmButton = {
                            TextButton(onClick = { safetyViewModel.dismissWhatsAppDisclosure() }) {
                                Text("Got it")
                            }
                        }
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            val currentBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = currentBackStackEntry?.destination?.route

                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "history",
                                    onClick = { navController.navigate("history") },
                                    icon = { Icon(Icons.Default.History, contentDescription = "Activity") },
                                    label = { Text("Activity") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "contacts",
                                    onClick = { navController.navigate("contacts") },
                                    icon = { Icon(Icons.Default.People, contentDescription = "Contacts") },
                                    label = { Text("Contacts") }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "settings",
                                    onClick = { navController.navigate("settings") },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("home") {
                                HomeScreen(
                                    viewModel = safetyViewModel,
                                    onNavigateToActivation = { /* Managed by global state */ },
                                    onNavigateToReport = { navController.navigate("report") },
                                    onNavigateToSettings = { navController.navigate("settings") },
                                    onNavigateToContacts = { navController.navigate("contacts") },
                                    onNavigateToGallery = { navController.navigate("gallery") }
                                )
                            }
                            composable("report") {
                                AntiRaggingScreen(
                                    viewModel = safetyViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("history") {
                                HistoryScreen(
                                    viewModel = safetyViewModel,
                                    onViewDetails = { id -> navController.navigate("incident/$id") }
                                )
                            }
                            composable("contacts") {
                                ContactsScreen(
                                    viewModel = safetyViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("incident/{id}") { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("id") ?: ""
                                IncidentDetailScreen(
                                    incidentId = id,
                                    viewModel = safetyViewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToGallery = { navController.navigate("gallery") }
                                )
                            }
                            composable("gallery") {
                                GalleryScreen(
                                    viewModel = safetyViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    viewModel = safetyViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }

                    // Emergency State Takeover Logic
                    if (sosState == SOSState.Triggering) {
                        EmergencyActivationScreen(
                            viewModel = safetyViewModel,
                            onNavigateToResponse = { /* State transitions automatically */ },
                            onCancel = { safetyViewModel.cancelSOS() }
                        )
                    } else if (sosState == SOSState.Active) {
                        EmergencyResponseScreen(
                            viewModel = safetyViewModel,
                            onCancel = { safetyViewModel.cancelSOS() }
                        )
                    }

                    // Demo Finger Overlay
                    DemoFingerOverlay(
                        isVisible = demoModeEnabled,
                        onTriggerSOS = { safetyViewModel.triggerSOS() },
                        onComplete = { safetyViewModel.toggleDemoMode(false) }
                    )
                }
            }
        }
    }
}
```

### [SafetyViewModel.kt](file:///D:/app%20dev%20dev%20dev/app/src/main/java/com/example/myapplication/ui/SafetyViewModel.kt)
```kotlin
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
        makeEmergencyCall(context, _wardenNumber.value)
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
```

---

## 3. Data Layer

### [Models.kt](file:///D:/app%20dev%20dev%20dev/app/src/main/java/com/example/myapplication/data/Models.kt)
```kotlin
package com.example.myapplication.data

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isPrimary: Boolean = false
)

@Serializable
data class TimelineEvent(
    val description: String,
    val timestamp: Long
)

@Serializable
data class EmergencyIncident(
    val id: String,
    val type: String,
    val timestamp: Long,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timeline: List<TimelineEvent>,
    val videoUris: List<String>,
    val audioUris: List<String>
)

@Serializable
data class SafetySettings(
    val powerButtonTrigger: Boolean = true,
    val volumeButtonTrigger: Boolean = true,
    val shakeDetection: Boolean = false,
    val silentSos: Boolean = false
)

@Serializable
data class SafetyData(
    val contacts: List<EmergencyContact> = emptyList(),
    val incidents: List<EmergencyIncident> = emptyList(),
    val settings: SafetySettings = SafetySettings()
)
```

### [SafetyRepository.kt](file:///D:/app%20dev%20dev%20dev/app/src/main/java/com/example/myapplication/data/SafetyRepository.kt)
```kotlin
package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object SafetyDataSerializer : Serializer<SafetyData> {
    override val defaultValue: SafetyData = SafetyData()

    override suspend fun readFrom(input: InputStream): SafetyData {
        return try {
            Json.decodeFromString(
                deserializer = SafetyData.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: SafetyData, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = SafetyData.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}

private val Context.safetyDataStore: DataStore<SafetyData> by dataStore(
    fileName = "safety_data.json",
    serializer = SafetyDataSerializer
)

class SafetyRepository(private val context: Context) {

    private val dataStore = context.safetyDataStore

    fun getContacts(): Flow<List<EmergencyContact>> = dataStore.data.map { it.contacts }

    suspend fun addContact(contact: EmergencyContact) {
        dataStore.updateData { current ->
            current.copy(contacts = current.contacts + contact)
        }
    }

    suspend fun updateContact(contact: EmergencyContact) {
        dataStore.updateData { current ->
            current.copy(contacts = current.contacts.map {
                if (it.id == contact.id) contact else it
            })
        }
    }

    suspend fun deleteContact(contactId: String) {
        dataStore.updateData { current ->
            current.copy(contacts = current.contacts.filter { it.id != contactId })
        }
    }

    suspend fun setPrimaryContact(contactId: String) {
        dataStore.updateData { current ->
            current.copy(contacts = current.contacts.map {
                it.copy(isPrimary = it.id == contactId)
            })
        }
    }

    fun getIncidents(): Flow<List<EmergencyIncident>> = dataStore.data.map { it.incidents }

    suspend fun addIncident(incident: EmergencyIncident) {
        dataStore.updateData { current ->
            current.copy(incidents = current.incidents + incident)
        }
    }

    suspend fun updateIncident(incident: EmergencyIncident) {
        dataStore.updateData { current ->
            current.copy(incidents = current.incidents.map {
                if (it.id == incident.id) incident else it
            })
        }
    }

    fun getSettings(): Flow<SafetySettings> = dataStore.data.map { it.settings }

    suspend fun updateSettings(settings: SafetySettings) {
        dataStore.updateData { current ->
            current.copy(settings = settings)
        }
    }

    suspend fun deleteMedia(uri: String) {
        dataStore.updateData { current ->
            current.copy(incidents = current.incidents.map { incident ->
                incident.copy(
                    videoUris = incident.videoUris.filter { it != uri },
                    audioUris = incident.audioUris.filter { it != uri }
                )
            })
        }
    }

    fun getIncidentById(id: String): Flow<EmergencyIncident?> =
        dataStore.data.map { it.incidents.find { incident -> incident.id == id } }
}
```

---

## 4. Background Services & Logic

### [EmergencyTriggerManager.kt](file:///D:/app%20dev%20dev%20dev/app/src/main/java/com/example/myapplication/logic/EmergencyTriggerManager.kt)
```kotlin
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
```

### [EmergencyService.kt](file:///D:/app%20dev%20dev%20dev/app/src/main/java/com/example/myapplication/logic/EmergencyService.kt)
```kotlin
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
```

### [ShakeTriggerService.kt](file:///D:/app%20dev%20dev%20dev/app/src/main/java/com/example/myapplication/logic/ShakeTriggerService.kt)
```kotlin
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
```

### [RecordingManager.kt](file:///D:/app%20dev%20dev%20dev/app/src/main/java/com/example/myapplication/logic/RecordingManager.kt)
```kotlin
package com.example.myapplication.logic

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RecordingManager(private val context: Context) {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioUri: Uri? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    var onRecordingFinalized: ((Uri) -> Unit)? = null
    var onAudioRecordingFinalized: ((Uri) -> Unit)? = null

    fun startRecording(lifecycleOwner: LifecycleOwner, previewView: PreviewView? = null) {
        if (recording != null) return // Already recording

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                val useCases = mutableListOf<androidx.camera.core.UseCase>(videoCapture!!)

                previewView?.let {
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(it.surfaceProvider)
                    useCases.add(preview)
                }

                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, *useCases.toTypedArray()
                )

                recordVideo()
                startAudioRecording()
            } catch (exc: Exception) {
                Log.e("RecordingManager", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun recordVideo() {
        val videoCapture = this.videoCapture ?: return

        val name = "NIRBHAYA_SOS_" + SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Nirbhaya-Emergency")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        try {
            recording = videoCapture.output
                .prepareRecording(context, mediaStoreOutputOptions)
                .apply {
                    try {
                        withAudioEnabled()
                    } catch (e: SecurityException) {
                        Log.e("RecordingManager", "Audio recording permission not granted", e)
                    }
                }
                .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                    when(recordEvent) {
                        is VideoRecordEvent.Start -> {
                            Log.d("RecordingManager", "Recording started")
                        }
                        is VideoRecordEvent.Finalize -> {
                            if (!recordEvent.hasError()) {
                                Log.d("RecordingManager", "Video capture succeeded: ${recordEvent.outputResults.outputUri}")
                                onRecordingFinalized?.invoke(recordEvent.outputResults.outputUri)
                            } else {
                                recording?.close()
                                recording = null
                                Log.e("RecordingManager", "Video capture ends with error: ${recordEvent.error}")
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("RecordingManager", "Failed to start recording", e)
        }
    }

    private fun startAudioRecording() {
        val name = "NIRBHAYA_AUDIO_" + SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Nirbhaya-Emergency")
            }
        }

        currentAudioUri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

        currentAudioUri?.let { uri ->
            try {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "w")
                parcelFileDescriptor?.let { pfd ->
                    mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        MediaRecorder(context)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaRecorder()
                    }.apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        setOutputFile(pfd.fileDescriptor)
                        prepare()
                        start()
                        Log.d("RecordingManager", "Audio recording started at $uri")
                    }
                }
            } catch (e: Exception) {
                Log.e("RecordingManager", "MediaRecorder failed", e)
            }
        }
    }

    fun stopRecording() {
        recording?.stop()
        recording = null

        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            currentAudioUri?.let {
                onAudioRecordingFinalized?.invoke(it)
            }
            currentAudioUri = null
            Log.d("RecordingManager", "Audio recording stopped")
        } catch (e: Exception) {
            Log.e("RecordingManager", "Error stopping MediaRecorder", e)
        }
    }
}
```

---

## 5. UI Screens (Compose)

### [HomeScreen.kt](file:///D:/app%20dev%20dev%20dev/app/src/main/java/com/example/myapplication/ui/screens/HomeScreen.kt)
```kotlin
package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.SafetyViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: SafetyViewModel,
    onNavigateToActivation: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToGallery: () -> Unit
) {
    val demoModeEnabled by viewModel.demoModeEnabled.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val protectionStatus by viewModel.protectionStatus.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isManualRecording by viewModel.isManualRecording.collectAsState()
    val manualRecordingTime by viewModel.manualRecordingTime.collectAsState()
    val context = LocalContext.current

    HomeScreenContent(
        demoModeEnabled = demoModeEnabled,
        contacts = contacts,
        protectionStatus = protectionStatus,
        settings = settings,
        isManualRecording = isManualRecording,
        manualRecordingTime = manualRecordingTime,
        onTriggerSOS = { viewModel.triggerSOS() },
        onTestSOS = { viewModel.startTestSOS() },
        onShareLocation = { viewModel.shareCurrentLocation(context) },
        onManualRecord = { viewModel.manualVideoRecording(context) },
        onStopManualRecord = { viewModel.stopManualRecording() },
        onToggleDemoMode = { viewModel.toggleDemoMode(it) },
        onUpdateSettings = { viewModel.updateSettings(it) },
        onNavigateToReport = onNavigateToReport,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToContacts = onNavigateToContacts,
        onNavigateToGallery = onNavigateToGallery
    )
}

@Composable
fun HomeScreenContent(
    demoModeEnabled: Boolean,
    contacts: List<com.example.myapplication.data.EmergencyContact>,
    protectionStatus: com.example.myapplication.ui.ProtectionStatus,
    settings: com.example.myapplication.data.SafetySettings,
    isManualRecording: Boolean,
    manualRecordingTime: Long,
    onTriggerSOS: () -> Unit,
    onTestSOS: () -> Unit,
    onShareLocation: () -> Unit,
    onManualRecord: () -> Unit,
    onStopManualRecord: () -> Unit,
    onToggleDemoMode: (Boolean) -> Unit,
    onUpdateSettings: (com.example.myapplication.data.SafetySettings) -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToGallery: () -> Unit
) {
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 40.dp, bottom = 24.dp)
        ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF4D64).copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4D64).copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFF4D64),
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    val isSafe = protectionStatus.contactsConfigured && protectionStatus.locationPermission
                    Text(
                        text = if (isSafe) "NIRBHAYA" else "ALERT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = PrimaryText,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (isSafe) "Stay Alert. Stay Safe." else "Finish setup for full safety.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = (if (protectionStatus.contactsConfigured) SuccessGreen else EmergencyRed).copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, (if (protectionStatus.contactsConfigured) SuccessGreen else EmergencyRed).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (protectionStatus.contactsConfigured) SuccessGreen else EmergencyRed, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (protectionStatus.contactsConfigured) "PROTECTED" else "UNPROTECTED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (protectionStatus.contactsConfigured) SuccessGreen else EmergencyRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = SecondaryText, modifier = Modifier.size(24.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Greeting
        Text(
            text = "Welcome to Nirbhaya",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            fontSize = 22.sp
        )
        Text(
            text = "You're protected. We've got your back.",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        // SOS Button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .scale(scale)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(EmergencyRed.copy(alpha = 0.3f), Color.Transparent),
                        ),
                        CircleShape
                    )
            ) {
                Surface(
                    modifier = Modifier.size(150.dp),
                    onClick = onTriggerSOS,
                    shape = CircleShape,
                    color = EmergencyRed,
                    shadowElevation = 20.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "SOS",
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "TAP TO TRIGGER",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Emergency alert will be sent to\nyour contacts with location & video.",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Action Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.GpsFixed,
                title = "Share Location",
                subtitle = "Send your real-time location",
                onClick = onShareLocation,
                iconColor = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Default.Videocam,
                title = "Record Video",
                subtitle = "Record and save evidence",
                onClick = onManualRecord,
                iconColor = Color(0xFFA678FF),
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Default.NotificationsActive,
                title = "Test SOS",
                subtitle = "Check if everything is working",
                onClick = onTestSOS,
                iconColor = Color(0xFFFFB347),
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Default.Contacts,
                title = "Emergency Contacts",
                subtitle = "View and manage contacts",
                onClick = onNavigateToContacts,
                iconColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Protection Status
        SectionHeader("Protection Status", "All Systems Active")
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MainCard,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Border)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProtectionStatusItem(Icons.Default.GpsFixed, "Location", protectionStatus.locationPermission)
                ProtectionStatusItem(Icons.Default.CameraAlt, "Camera", protectionStatus.cameraPermission)
                ProtectionStatusItem(Icons.Default.Mic, "Microphone", protectionStatus.micPermission)
                ProtectionStatusItem(Icons.Default.Language, "Internet", protectionStatus.internetAvailable)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Your Triggers
        SectionHeader("Your Triggers", "Manage", onNavigateToSettings)
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TriggerRow(
                Icons.Default.PowerSettingsNew,
                "Power Button (4 taps)",
                "Trigger SOS",
                settings.powerButtonTrigger,
                onToggle = { onUpdateSettings(settings.copy(powerButtonTrigger = it)) }
            )
            TriggerRow(
                Icons.Default.VolumeUp,
                "Volume Buttons",
                "Trigger SOS",
                settings.volumeButtonTrigger,
                onToggle = { onUpdateSettings(settings.copy(volumeButtonTrigger = it)) }
            )
            TriggerRow(
                Icons.Default.ScreenRotation,
                "Shake Detection",
                "Trigger SOS",
                settings.shakeDetection,
                onToggle = { onUpdateSettings(settings.copy(shakeDetection = it)) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Emergency Contacts Preview
        SectionHeader("Emergency Contacts", "View All", onNavigateToContacts)
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (contacts.isEmpty()) {
                Text("No contacts added", color = SecondaryText, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
            }
            contacts.take(2).forEach { contact ->
                ContactPreviewRow(contact)
            }

            Button(
                onClick = { onNavigateToContacts() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.3f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Add Emergency Contact", color = EmergencyRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        }

        if (isManualRecording) {
            RecordingOverlay(
                timeElapsed = manualRecordingTime,
                onStop = onStopManualRecord
            )
        }
    }
}
```

### [EmergencyResponseScreen.kt](file:///D:/app%20dev%20dev%20dev/app/src/main/java/com/example/myapplication/ui/screens/EmergencyResponseScreen.kt)
```kotlin
package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.SafetyViewModel
import com.example.myapplication.ui.components.EmergencyTimeline
import com.example.myapplication.ui.components.TimelineStep
import com.example.myapplication.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ui.components.NirbhayaCard
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmergencyResponseScreen(viewModel: SafetyViewModel, onCancel: () -> Unit) {
    val location by viewModel.location.collectAsState()
    val isTest by viewModel.isTestMode.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val policeEnabled by viewModel.policeResponseEnabled.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val contacts by viewModel.contacts.collectAsState()
    val primaryContact = remember(contacts) { contacts.find { it.isPrimary } ?: contacts.firstOrNull() }

    val incidentId by viewModel.currentIncidentId.collectAsState()
    val incident by (incidentId?.let { viewModel.getIncidentDetails(it) } ?: flowOf(null)).collectAsState(initial = null)

    val timelineSteps = remember(incident) {
        incident?.timeline?.map { event ->
            TimelineStep(
                title = event.description,
                time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp)),
                isCompleted = true
            )
        } ?: listOf(
            TimelineStep("SOS Triggered", "Just now", isCompleted = true),
            TimelineStep("Location Shared", "Pending", isCompleted = false)
        )
    }

    var visible by remember { mutableStateOf(false) }
    var showCancelConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    if (showCancelConfirmation) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmation = false },
            title = { Text("Resolve Emergency?") },
            text = { Text("Are you sure you want to stop the SOS session and stop recording? Your evidence will be saved.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancelSOS()
                    onCancel()
                }) {
                    Text("YES, I'M SAFE", color = SuccessGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmation = false }) {
                    Text("NO, KEEP SOS ACTIVE", color = PrimaryText)
                }
            }
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val recAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recPulse"
    )

    val formatTime = remember(elapsedTime) {
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { it / 2 })
                ) {
                    Text(
                        text = if (isTest) "TEST SOS ACTIVE" else "EMERGENCY ACTIVE",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isTest) Cyan else EmergencyRed,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "ID: ${incidentId?.takeLast(8) ?: "Generating..."}",
                    color = PrimaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Duration: $formatTime",
                    color = SecondaryText,
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .graphicsLayer { alpha = recAlpha }
                        .background(EmergencyRed, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "REC",
                    color = EmergencyRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(800, delayMillis = 200)) + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            NirbhayaCard {
                Text(
                    "LIVE LOCATION & RECORDING",
                    style = MaterialTheme.typography.labelLarge,
                    color = Cyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Border, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Google Maps View", color = SecondaryText)
                }

                Spacer(modifier = Modifier.height(16.dp))

                location?.let { loc ->
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LocationRow("Latitude", String.format(Locale.getDefault(), "%.6f", loc.latitude))
                        LocationRow("Longitude", String.format(Locale.getDefault(), "%.6f", loc.longitude))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            LocationMiniItem("Accuracy", "${String.format(Locale.getDefault(), "%.1f", loc.accuracy)}m")
                            LocationMiniItem("Status", "SHARING LIVE")
                        }
                    }
                } ?: Text("Acquiring precision lock...", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(800, delayMillis = 400))
        ) {
            Column {
                Text(
                    "RESPONSE TIMELINE",
                    style = MaterialTheme.typography.labelLarge,
                    color = SecondaryText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                EmergencyTimeline(steps = timelineSteps, modifier = Modifier.height(200.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(800, delayMillis = 600)) + slideInVertically(initialOffsetY = { it / 2 })
        ) {
            Column {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MainCard,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Border)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("POLICE RESPONSE", fontWeight = FontWeight.Bold, color = PrimaryText)
                            Text("Notify local authorities", fontSize = 12.sp, color = SecondaryText)
                        }
                        Switch(
                            checked = policeEnabled,
                            onCheckedChange = { viewModel.togglePoliceResponse(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmergencyRed,
                                checkedTrackColor = EmergencyRed.copy(alpha = 0.3f),
                                uncheckedThumbColor = SecondaryText,
                                uncheckedTrackColor = Border,
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.callWarden(context) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("CALL WARDEN", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        primaryContact?.let { viewModel.shareLocationToWhatsApp(context, it.phoneNumber) }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(20.dp),
                    enabled = primaryContact != null
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("SHARE ON WHATSAPP", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { showCancelConfirmation = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("RESOLVE & CANCEL", color = EmergencyRed, fontWeight = FontWeight.Bold)
        }
    }
}
```

### [HistoryScreen.kt](file:///D:/app%20dev%20dev%20dev/app/src/main/java/com/example/myapplication/ui/screens/HistoryScreen.kt)
```kotlin
package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.ui.SafetyViewModel
import com.example.myapplication.ui.SOSHistoryItem
import com.example.myapplication.ui.components.NirbhayaCard
import com.example.myapplication.ui.theme.*

import com.example.myapplication.data.EmergencyIncident

@Composable
fun HistoryScreen(viewModel: SafetyViewModel, onViewDetails: (String) -> Unit) {
    val incidents by viewModel.incidents.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = "Sacred Icon",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "INCIDENT HISTORY",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = PrimaryText
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (incidents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No incidents recorded yet", color = SecondaryText)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(incidents) { item ->
                    HistoryCard(item, onViewDetails)
                }
            }
        }
    }
}
```

---

*This document contains the core logical and UI files. Asset files (drawables, mipmaps) and standard themes are omitted for brevity.*
