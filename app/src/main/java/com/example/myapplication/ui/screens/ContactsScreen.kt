package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.EmergencyContact
import com.example.myapplication.ui.SafetyViewModel
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(viewModel: SafetyViewModel, onBack: () -> Unit) {
    val contacts by viewModel.contacts.collectAsState()
    
    var showAddContactDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<EmergencyContact?>(null) }
    
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }

    if (showAddContactDialog || contactToEdit != null) {
        AlertDialog(
            onDismissRequest = { 
                showAddContactDialog = false
                contactToEdit = null
                name = ""; number = ""; relationship = ""
            },
            title = { Text(if (contactToEdit != null) "Edit Contact" else "Add Emergency Contact", color = PrimaryText) },
            containerColor = MainCard,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText,
                            focusedBorderColor = EmergencyRed,
                            unfocusedBorderColor = Border
                        )
                    )
                    OutlinedTextField(
                        value = number,
                        onValueChange = { number = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText,
                            focusedBorderColor = EmergencyRed,
                            unfocusedBorderColor = Border
                        )
                    )
                    OutlinedTextField(
                        value = relationship,
                        onValueChange = { relationship = it },
                        label = { Text("Relationship (e.g. Father, Friend)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText,
                            focusedBorderColor = EmergencyRed,
                            unfocusedBorderColor = Border
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (contactToEdit != null) {
                            viewModel.updateContact(contactToEdit!!.copy(name = name, phoneNumber = number, relationship = relationship))
                        } else {
                            viewModel.addContact(name, number, relationship)
                        }
                        showAddContactDialog = false
                        contactToEdit = null
                        name = ""; number = ""; relationship = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("SAVE")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddContactDialog = false
                    contactToEdit = null
                    name = ""; number = ""; relationship = ""
                }) {
                    Text("CANCEL", color = SecondaryText)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text("Emergency Contacts", fontWeight = FontWeight.Bold, color = PrimaryText) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddContactDialog = true },
                containerColor = EmergencyRed,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Manage your trusted contacts who will be notified during emergencies.",
                color = SecondaryText,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (contacts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PeopleOutline, 
                            contentDescription = null, 
                            modifier = Modifier.size(64.dp),
                            tint = SecondaryText.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No contacts added yet", color = SecondaryText)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(contacts) { contact ->
                        ContactItemPremium(
                            contact = contact,
                            onEdit = {
                                contactToEdit = it
                                name = it.name
                                number = it.phoneNumber
                                relationship = it.relationship
                            },
                            onDelete = { viewModel.deleteContact(it.id) },
                            onSetPrimary = { viewModel.setPrimaryContact(it.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactItemPremium(
    contact: EmergencyContact,
    onEdit: (EmergencyContact) -> Unit,
    onDelete: (EmergencyContact) -> Unit,
    onSetPrimary: (EmergencyContact) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MainCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        ListItem(
            headlineContent = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(contact.name, color = PrimaryText, fontWeight = FontWeight.Bold)
                    if (contact.isPrimary) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = EmergencyRed.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "PRIMARY", 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Bold,
                                color = EmergencyRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            },
            supportingContent = { 
                Text(
                    "${contact.relationship} • ${contact.phoneNumber}",
                    color = SecondaryText,
                    fontSize = 12.sp
                ) 
            },
            leadingContent = { 
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = SecondaryCard
                ) {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = null, 
                        tint = if (contact.isPrimary) EmergencyRed else SecondaryText,
                        modifier = Modifier.padding(10.dp)
                    ) 
                }
            },
            trailingContent = {
                Row {
                    if (!contact.isPrimary) {
                        IconButton(onClick = { onSetPrimary(contact) }) {
                            Icon(Icons.Default.StarBorder, contentDescription = "Set Primary", tint = SecondaryText)
                        }
                    }
                    IconButton(onClick = { onEdit(contact) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SecondaryText)
                    }
                    IconButton(onClick = { onDelete(contact) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EmergencyRed.copy(alpha = 0.7f))
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
