package com.example.myapplication.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.myapplication.ui.SafetyViewModel
import com.example.myapplication.ui.components.NirbhayaCard
import com.example.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(viewModel: SafetyViewModel, onBack: () -> Unit) {
    val incidents by viewModel.incidents.collectAsState()
    val context = LocalContext.current
    
    // Flatten all media with their incident context
    val allMedia = remember(incidents) {
        incidents.flatMap { incident ->
            val videos = incident.videoUris.map { MediaItem(it, MediaType.VIDEO, incident.id, incident.timestamp) }
            val audios = incident.audioUris.map { MediaItem(it, MediaType.AUDIO, incident.id, incident.timestamp) }
            videos + audios
        }.sortedByDescending { it.timestamp }
    }

    var mediaToDelete by remember { mutableStateOf<MediaItem?>(null) }

    if (mediaToDelete != null) {
        AlertDialog(
            onDismissRequest = { mediaToDelete = null },
            title = { Text("Delete Evidence?") },
            text = { Text("Are you sure you want to permanently delete this evidence file? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    mediaToDelete?.let { viewModel.deleteMedia(it.uri) }
                    mediaToDelete = null
                }) {
                    Text("DELETE", color = EmergencyRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mediaToDelete = null }) {
                    Text("CANCEL", color = PrimaryText)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evidence Gallery") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (allMedia.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No media files found", color = SecondaryText)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allMedia) { item ->
                    MediaGalleryCard(
                        item = item,
                        onPlay = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setDataAndType(Uri.parse(item.uri), if (item.type == MediaType.VIDEO) "video/*" else "audio/*")
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle exception
                            }
                        },
                        onShare = {
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.type = if (item.type == MediaType.VIDEO) "video/*" else "audio/*"
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(item.uri))
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            context.startActivity(Intent.createChooser(intent, "Share Evidence"))
                        },
                        onDelete = {
                            mediaToDelete = item
                        }
                    )
                }
            }
        }
    }
}

enum class MediaType { VIDEO, AUDIO }
data class MediaItem(val uri: String, val type: MediaType, val incidentId: String, val timestamp: Long)

@Composable
fun MediaGalleryCard(item: MediaItem, onPlay: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit) {
    val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MainCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable { onPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.type == MediaType.VIDEO) Icons.Default.PlayCircle else Icons.Default.Audiotrack,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Cyan
                )
            }
            
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = if (item.type == MediaType.VIDEO) "Video Evidence" else "Audio Evidence",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
                Text(text = date, style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                Text(text = "ID: ${item.incidentId.takeLast(8)}", style = MaterialTheme.typography.labelSmall, color = Cyan)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EmergencyRed, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
