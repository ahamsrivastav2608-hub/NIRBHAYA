package com.example.myapplication.ui.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.SafetyViewModel
import com.example.myapplication.logic.SOSState
import com.example.myapplication.ui.theme.*

@Composable
fun EmergencyActivationScreen(viewModel: SafetyViewModel, onNavigateToResponse: () -> Unit, onCancel: () -> Unit) {
    val sosState by viewModel.sosState.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundPulse")
    val pulseColor by infiniteTransition.animateColor(
        initialValue = Background,
        targetValue = EmergencyRed.copy(alpha = 0.15f),
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    if (sosState == SOSState.Active) {
        onNavigateToResponse()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pulseColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "SOS TRIGGERED",
                    color = EmergencyRed,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "AUTO-STARTING IN",
                    color = PrimaryText.copy(alpha = 0.6f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = countdown.toString(),
                    color = EmergencyRed,
                    fontSize = 140.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "SECONDS",
                    color = PrimaryText.copy(alpha = 0.6f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Your location and video will be shared.",
                    color = SecondaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Button(
            onClick = {
                viewModel.cancelSOS()
                onCancel()
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryText),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text(
                "CANCEL TRIGGER",
                color = Background,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}
