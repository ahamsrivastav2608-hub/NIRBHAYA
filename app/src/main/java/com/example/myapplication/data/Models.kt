package com.example.myapplication.data

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isPrimary: Boolean = false
)

@Serializable
data class TimelineEvent(
    val description: String,
    val timestamp: Long
)

@Serializable
data class EmergencyIncident(
    val id: String,
    val type: String,
    val timestamp: Long,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timeline: List<TimelineEvent>,
    val videoUris: List<String>,
    val audioUris: List<String>
)

@Serializable
data class SafetySettings(
    val powerButtonTrigger: Boolean = true,
    val volumeButtonTrigger: Boolean = true,
    val shakeDetection: Boolean = false,
    val silentSos: Boolean = false
)

@Serializable
data class SafetyData(
    val contacts: List<EmergencyContact> = emptyList(),
    val incidents: List<EmergencyIncident> = emptyList(),
    val settings: SafetySettings = SafetySettings()
)
