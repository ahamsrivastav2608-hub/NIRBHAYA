package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object SafetyDataSerializer : Serializer<SafetyData> {
    override val defaultValue: SafetyData = SafetyData()

    override suspend fun readFrom(input: InputStream): SafetyData {
        return try {
            Json.decodeFromString(
                deserializer = SafetyData.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: SafetyData, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = SafetyData.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}

private val Context.safetyDataStore: DataStore<SafetyData> by dataStore(
    fileName = "safety_data.json",
    serializer = SafetyDataSerializer
)

class SafetyRepository(private val context: Context) {

    private val dataStore = context.safetyDataStore

    fun getContacts(): Flow<List<EmergencyContact>> = dataStore.data.map { it.contacts }

    suspend fun addContact(contact: EmergencyContact) {
        dataStore.updateData { current ->
            current.copy(contacts = current.contacts + contact)
        }
    }

    suspend fun updateContact(contact: EmergencyContact) {
        dataStore.updateData { current ->
            current.copy(contacts = current.contacts.map {
                if (it.id == contact.id) contact else it
            })
        }
    }

    suspend fun deleteContact(contactId: String) {
        dataStore.updateData { current ->
            current.copy(contacts = current.contacts.filter { it.id != contactId })
        }
    }

    suspend fun setPrimaryContact(contactId: String) {
        dataStore.updateData { current ->
            current.copy(contacts = current.contacts.map {
                it.copy(isPrimary = it.id == contactId)
            })
        }
    }

    fun getIncidents(): Flow<List<EmergencyIncident>> = dataStore.data.map { it.incidents }

    suspend fun addIncident(incident: EmergencyIncident) {
        dataStore.updateData { current ->
            current.copy(incidents = current.incidents + incident)
        }
    }

    suspend fun updateIncident(incident: EmergencyIncident) {
        dataStore.updateData { current ->
            current.copy(incidents = current.incidents.map {
                if (it.id == incident.id) incident else it
            })
        }
    }

    fun getSettings(): Flow<SafetySettings> = dataStore.data.map { it.settings }

    suspend fun updateSettings(settings: SafetySettings) {
        dataStore.updateData { current ->
            current.copy(settings = settings)
        }
    }

    suspend fun deleteMedia(uri: String) {
        dataStore.updateData { current ->
            current.copy(incidents = current.incidents.map { incident ->
                incident.copy(
                    videoUris = incident.videoUris.filter { it != uri },
                    audioUris = incident.audioUris.filter { it != uri }
                )
            })
        }
    }

    fun getIncidentById(id: String): Flow<EmergencyIncident?> = 
        dataStore.data.map { it.incidents.find { incident -> incident.id == id } }
}
