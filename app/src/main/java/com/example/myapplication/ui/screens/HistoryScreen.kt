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

@Composable
fun HistoryCard(item: EmergencyIncident, onViewDetails: (String) -> Unit) {
    val date = java.text.SimpleDateFormat("dd MMM yyyy • hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(item.timestamp))
    
    NirbhayaCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = item.type,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.type.contains("SOS")) EmergencyRed else PrimaryBlue,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
            }
            Text(
                text = item.status,
                style = MaterialTheme.typography.labelLarge,
                color = if (item.status == "Resolved") SuccessGreen else Cyan,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HorizontalDivider(color = Border, thickness = 1.dp)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ID: ${item.id}",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            TextButton(onClick = { onViewDetails(item.id) }) {
                Text(
                    text = "VIEW DETAILS >",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
