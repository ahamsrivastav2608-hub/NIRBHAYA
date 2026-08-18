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
