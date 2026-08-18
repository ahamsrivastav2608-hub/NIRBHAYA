package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.*

@Composable
fun NirbhayaCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = MainCard,
        contentColor = PrimaryText
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            content = content
        )
    }
}

@Composable
fun SOSButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SOSPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScaleAnimation"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaAnimation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(280.dp)
    ) {
        // Outer Glow
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(scale)
                .background(
                    color = EmergencyRed.copy(alpha = alpha),
                    shape = CircleShape
                )
        )
        
        // Button
        Button(
            onClick = onClick,
            modifier = Modifier
                .size(180.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = EmergencyRed,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp)
        ) {
            Text(
                text = "SOS",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 48.sp
                )
            )
        }
    }
}

@Composable
fun StatusIndicator(
    activeCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) { index ->
            val isActive = index < activeCount
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Cyan else Border)
                    .then(
                        if (isActive) Modifier.border(2.dp, Cyan.copy(alpha = 0.5f), CircleShape)
                        else Modifier
                    )
            )
        }
    }
}

data class TimelineStep(
    val title: String,
    val time: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean = false
)

@Composable
fun EmergencyTimeline(
    steps: List<TimelineStep>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        steps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    step.isCurrent -> Cyan
                                    step.isCompleted -> SuccessGreen
                                    else -> Border
                                }
                            )
                    )
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(
                                    if (step.isCompleted) SuccessGreen else Border
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (step.isCompleted || step.isCurrent) PrimaryText else SecondaryText
                    )
                    Text(
                        text = step.time,
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecondaryText
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPlayerPlaceholder(uri: String, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Video: ${uri.takeLast(20)}", color = Color.White)
        }
    }
}

@Composable
fun AudioPlayerPlaceholder(uri: String, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MainCard)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Audiotrack,
                contentDescription = "Audio",
                tint = Cyan
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Audio Recording", color = PrimaryText, fontWeight = FontWeight.Bold)
                Text(uri.takeLast(25), color = SecondaryText, fontSize = 12.sp)
            }
            IconButton(onClick = onClick) {
                Icon(androidx.compose.material.icons.Icons.Default.PlayArrow, contentDescription = "Play", tint = Cyan)
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = PrimaryBlue
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .width(100.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        color = MainCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = PrimaryText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 10.sp,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun ProtectionStatusItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) SuccessGreen else SecondaryText,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                fontSize = 10.sp
            )
            Text(
                text = if (isActive) "ON" else "OFF",
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) SuccessGreen else SecondaryText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
