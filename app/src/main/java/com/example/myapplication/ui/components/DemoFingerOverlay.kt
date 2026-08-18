package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.EmergencyRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DemoFingerOverlay(
    isVisible: Boolean,
    onTriggerSOS: () -> Unit,
    onComplete: () -> Unit
) {
    if (!isVisible) return

    var animationState by remember { mutableStateOf(0) } // 0: Idle, 1: Approach, 2: Press, 3: Ripple, 4: Done

    val fingerPositionX = remember { Animatable(800f) }
    val fingerPositionY = remember { Animatable(1200f) }
    val fingerScale = remember { Animatable(1.2f) }
    val rippleScale = remember { Animatable(0f) }
    val rippleAlpha = remember { Animatable(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Sequence: Finger appears -> Moves to button -> Button depresses -> Ripple -> SOS Triggered
            
            // 1. Approach (Move to center of screen roughly)
            animationState = 1
            fingerPositionX.animateTo(0f, tween(1000, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)))
            fingerPositionY.animateTo(0f, tween(1000, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)))
            
            // 2. Press
            animationState = 2
            fingerScale.animateTo(0.8f, tween(300, easing = LinearOutSlowInEasing))
            
            // 3. Ripple & Glow
            animationState = 3
            launch {
                rippleScale.animateTo(4f, tween(600, easing = FastOutSlowInEasing))
            }
            launch {
                rippleAlpha.animateTo(0.6f, tween(200))
                rippleAlpha.animateTo(0f, tween(400))
            }
            
            delay(200)
            onTriggerSOS()
            
            delay(600)
            // 4. Done
            animationState = 4
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f * rippleAlpha.value))
            .graphicsLayer {
                translationX = fingerPositionX.value
                translationY = fingerPositionY.value
            },
        contentAlignment = Alignment.Center
    ) {
        // Ripple Effect
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(rippleScale.value)
                .alpha(rippleAlpha.value)
                .background(
                    Brush.radialGradient(
                        colors = listOf(EmergencyRed, Color.Transparent),
                        center = Offset.Unspecified,
                        radius = Float.POSITIVE_INFINITY
                    ),
                    CircleShape
                )
        )

        // Realistic Finger (Icon for demo)
        Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(80.dp)
                .scale(fingerScale.value)
                .graphicsLayer {
                    rotationZ = -15f
                    shadowElevation = 10f
                }
        )
    }
}
