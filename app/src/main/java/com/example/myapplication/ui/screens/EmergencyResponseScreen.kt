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
    val sosState by viewModel.sosState.collectAsState()
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

@Composable
private fun LocationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = PrimaryText, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun LocationMiniItem(label: String, value: String) {
    Column {
        Text(label, color = SecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = PrimaryText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
