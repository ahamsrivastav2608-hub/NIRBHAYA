package com.example.myapplication.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.SafetyViewModel
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentDetailScreen(
    incidentId: String,
    viewModel: SafetyViewModel,
    onBack: () -> Unit,
    onNavigateToGallery: () -> Unit
) {
    val incident by viewModel.getIncidentDetails(incidentId).collectAsState(initial = null)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Incident Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToGallery) {
                        Icon(Icons.Default.Collections, contentDescription = "Gallery")
                    }
                }
            )
        }
    ) { padding ->
        incident?.let { item ->
            val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
            
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.type,
                                style = MaterialTheme.typography.headlineMedium,
                                color = if (item.type.contains("SOS")) EmergencyRed else PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                            Badge(
                                containerColor = if (item.status == "Resolved") SuccessGreen else Cyan
                            ) {
                                Text(item.status, modifier = Modifier.padding(4.dp), color = Color.White)
                            }
                        }
                        Text(
                            text = "Case ID: ${item.id}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText
                        )
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText
                        )
                    }
                }

                // Location Section
                item {
                    NirbhayaCard {
                        Text("LOCATION", style = MaterialTheme.typography.labelLarge, color = Cyan, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Lat: ${item.latitude}", color = PrimaryText)
                        Text("Long: ${item.longitude}", color = PrimaryText)
                        Text("Accuracy: ${item.accuracy}m", color = SecondaryText, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val uri = "geo:${item.latitude},${item.longitude}?q=${item.latitude},${item.longitude}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("OPEN IN GOOGLE MAPS")
                        }
                    }
                }

                // Timeline Section
                item {
                    Text("TIMELINE", style = MaterialTheme.typography.labelLarge, color = Cyan, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    val steps = item.timeline.map { event ->
                        TimelineStep(
                            title = event.description,
                            time = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(event.timestamp)),
                            isCompleted = true
                        )
                    }
                    EmergencyTimeline(steps = steps)
                }

                // Media Recordings
                if (item.videoUris.isNotEmpty() || item.audioUris.isNotEmpty()) {
                    item {
                        Text("MEDIA RECORDINGS", style = MaterialTheme.typography.labelLarge, color = Cyan, fontWeight = FontWeight.Bold)
                    }

                    items(item.videoUris) { uri ->
                        VideoPlayerPlaceholder(
                            uri = uri,
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.setDataAndType(Uri.parse(uri), "video/*")
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle
                                }
                            }
                        )
                    }

                    items(item.audioUris) { uri ->
                        AudioPlayerPlaceholder(
                            uri = uri,
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.setDataAndType(Uri.parse(uri), "audio/*")
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle
                                }
                            }
                        )
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
