package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
                onClick = onNavigateToContacts,
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

@Composable
fun RecordingOverlay(
    timeElapsed: Long,
    onStop: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(onClick = {}), // Prevent clicks to dashboard
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .scale(alpha + 0.5f)
                        .background(EmergencyRed, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "RECORDING EVIDENCE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val minutes = (timeElapsed / 1000) / 60
            val seconds = (timeElapsed / 1000) % 60
            Text(
                text = java.util.Locale.getDefault().let { locale ->
                    String.format(locale, "%02d:%02d", minutes, seconds)
                },
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp)
            ) {
                Text(
                    text = "STOP RECORDING",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, action: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )
        TextButton(onClick = onClick) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = action,
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun TriggerRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    title: String, 
    subtitle: String, 
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = SecondaryCard
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isEnabled) EmergencyRed else SecondaryText,
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = PrimaryText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = EmergencyRed,
                uncheckedThumbColor = SecondaryText,
                uncheckedTrackColor = Border
            )
        )
    }
}

@Composable
fun ContactPreviewRow(contact: com.example.myapplication.data.EmergencyContact) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = SecondaryCard
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = SecondaryText,
                modifier = Modifier.padding(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = contact.name, color = PrimaryText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = contact.phoneNumber, color = SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
        Row {
            IconButton(onClick = { 
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${contact.phoneNumber}"))
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Call, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = { 
                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:${contact.phoneNumber}"))
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Message, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF07090C)
@Composable
fun HomeScreenPreview() {
    MyApplicationTheme {
        HomeScreenContent(
            demoModeEnabled = false,
            contacts = listOf(
                com.example.myapplication.data.EmergencyContact("1", "Mom", "+91 98765 43210", "Mother", true),
                com.example.myapplication.data.EmergencyContact("2", "Dad", "+91 91234 56789", "Father", false)
            ),
            protectionStatus = com.example.myapplication.ui.ProtectionStatus(true, true, true, true, true),
            settings = com.example.myapplication.data.SafetySettings(),
            isManualRecording = false,
            manualRecordingTime = 0L,
            onTriggerSOS = {},
            onTestSOS = {},
            onShareLocation = {},
            onManualRecord = {},
            onStopManualRecord = {},
            onToggleDemoMode = {},
            onUpdateSettings = {},
            onNavigateToReport = {},
            onNavigateToSettings = {},
            onNavigateToContacts = {},
            onNavigateToGallery = {}
        )
    }
}
