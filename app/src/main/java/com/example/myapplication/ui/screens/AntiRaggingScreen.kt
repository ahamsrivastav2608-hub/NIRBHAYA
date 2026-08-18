package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.SafetyViewModel
import com.example.myapplication.ui.theme.SecondaryText
import com.example.myapplication.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AntiRaggingScreen(viewModel: SafetyViewModel, onBack: () -> Unit) {
    var description by remember { mutableStateOf("") }
    var submittedId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anonymous Report") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (submittedId == null) {
                Text(
                    text = "Report Misconduct or Ragging",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your identity remains completely anonymous. A unique tracking ID will be generated.",
                    fontSize = 14.sp,
                    color = SecondaryText,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description of incident") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = { Text("What happened? When? Where?") }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (description.isNotBlank()) {
                            submittedId = viewModel.submitAntiRaggingReport(description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = description.isNotBlank()
                ) {
                    Text("SUBMIT ANONYMOUSLY")
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = SuccessGreen
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "REPORT SUBMITTED",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your tracking ID is:",
                    fontSize = 16.sp,
                    color = SecondaryText
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(
                        text = submittedId ?: "",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                Text(
                    text = "Save this ID to check status later.",
                    fontSize = 14.sp,
                    color = SecondaryText
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DONE")
                }
            }
        }
    }
}
