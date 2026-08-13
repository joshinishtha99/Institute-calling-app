package com.institute.calling.ui.owner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.institute.calling.domain.model.StaffMember
import com.institute.calling.ui.components.InitialAvatar
import com.institute.calling.ui.components.avatarColorFor

@Composable
fun BranchStaffScreen(
    onBack: () -> Unit,
    viewModel: BranchStaffViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<StaffMember?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { inner ->
        Column(Modifier.fillMaxSize().padding(inner).padding(horizontal = 22.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Spacer(Modifier.padding(horizontal = 2.dp))
                Column {
                    Text(state.branchName.ifEmpty { "Staff" }, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                    Text("Manage staff", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Column(
                    Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.load() }) { Text("Retry") }
                }
                else -> {
                    OutlinedButton(
                        onClick = { showAdd = true },
                        enabled = !state.submitting,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    ) { Text("+ Add caller") }

                    if (state.staff.isEmpty()) {
                        Text(
                            "No callers yet. Add one above.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 18.dp),
                        )
                    } else {
                        LazyColumn(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(state.staff) { index, member ->
                                StaffRow(member, index) { editing = member }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddCallerDialog(
            branchName = state.branchName,
            onDismiss = { showAdd = false },
            onConfirm = { name, pin -> viewModel.addCaller(name, pin); showAdd = false },
        )
    }

    editing?.let { member ->
        EditCallerDialog(
            member = member,
            onDismiss = { editing = null },
            onRename = { name -> viewModel.rename(member.id, name); editing = null },
            onResetPin = { pin -> viewModel.resetPin(member.id, pin); editing = null },
            onToggleActive = { viewModel.setActive(member.id, !member.isActive); editing = null },
        )
    }
}

@Composable
private fun StaffRow(member: StaffMember, index: Int, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            InitialAvatar(member.initial, avatarColorFor(index), size = 38)
            Spacer(Modifier.padding(horizontal = 6.dp))
            Text(member.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            if (!member.isActive) {
                Surface(color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f), shape = RoundedCornerShape(100.dp)) {
                    Text(
                        "Deactivated",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCallerDialog(branchName: String, onDismiss: () -> Unit, onConfirm: (name: String, pin: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    val pinValid = pin.length == 4 && pin.all { it.isDigit() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add caller to $branchName") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Caller name") },
                    singleLine = true,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("4-digit PIN") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Share this PIN with the caller so they can log in.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim(), pin) }, enabled = name.isNotBlank() && pinValid) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EditCallerDialog(
    member: StaffMember,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    onResetPin: (String) -> Unit,
    onToggleActive: () -> Unit,
) {
    var name by remember { mutableStateOf(member.name) }
    var pin by remember { mutableStateOf("") }
    val pinValid = pin.length == 4 && pin.all { it.isDigit() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${member.name}") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = { onRename(name.trim()) },
                    enabled = name.isNotBlank() && name.trim() != member.name,
                ) { Text("Save name") }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("New PIN (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
                TextButton(onClick = { onResetPin(pin) }, enabled = pinValid) { Text("Reset PIN") }
            }
        },
        confirmButton = {
            TextButton(onClick = onToggleActive) {
                Text(if (member.isActive) "Deactivate" else "Reactivate")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}
