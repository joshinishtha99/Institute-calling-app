package com.institute.calling.ui.owner

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.institute.calling.domain.model.City

private sealed interface AddTarget {
    data object AddCity : AddTarget
    data class AddBranch(val cityId: String, val cityName: String) : AddTarget
    data class AddCaller(val branchId: String, val branchName: String) : AddTarget
}

@Composable
fun ManagementScreen(
    onBack: () -> Unit,
    viewModel: ManagementViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var target by remember { mutableStateOf<AddTarget?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { inner ->
        Column(Modifier.fillMaxSize().padding(inner).padding(horizontal = 22.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                Spacer(Modifier.padding(horizontal = 2.dp))
                Text("Cities & branches", fontSize = 20.sp, fontWeight = FontWeight.Medium)
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
                        onClick = { target = AddTarget.AddCity },
                        enabled = !state.submitting,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    ) { Text("+ Add city") }

                    LazyColumn(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.cities) { city ->
                            CityBlock(
                                city = city,
                                submitting = state.submitting,
                                onAddBranch = { target = AddTarget.AddBranch(city.id, city.name) },
                                onAddCaller = { branchId, branchName -> target = AddTarget.AddCaller(branchId, branchName) },
                            )
                        }
                    }
                }
            }
        }
    }

    when (val t = target) {
        AddTarget.AddCity -> NameDialog(
            title = "Add city",
            label = "City name",
            onDismiss = { target = null },
            onConfirm = { name -> viewModel.addCity(name); target = null },
        )
        is AddTarget.AddBranch -> NameDialog(
            title = "Add branch in ${t.cityName}",
            label = "Branch name",
            onDismiss = { target = null },
            onConfirm = { name -> viewModel.addBranch(t.cityId, name); target = null },
        )
        is AddTarget.AddCaller -> CallerDialog(
            branchName = t.branchName,
            onDismiss = { target = null },
            onConfirm = { name, pin -> viewModel.addCaller(t.branchId, name, pin); target = null },
        )
        null -> Unit
    }
}

@Composable
private fun CityBlock(
    city: City,
    submitting: Boolean,
    onAddBranch: () -> Unit,
    onAddCaller: (branchId: String, branchName: String) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(city.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                TextButton(onClick = onAddBranch, enabled = !submitting) { Text("+ branch") }
            }
            city.branches.forEach { branch ->
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(branch.name, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                        Text("${branch.callers.size} callers", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { onAddCaller(branch.id, branch.name) }, enabled = !submitting) { Text("+ caller") }
                }
            }
        }
    }
}

@Composable
private fun NameDialog(title: String, label: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(label) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim()) }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun CallerDialog(branchName: String, onDismiss: () -> Unit, onConfirm: (name: String, pin: String) -> Unit) {
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
            TextButton(
                onClick = { onConfirm(name.trim(), pin) },
                enabled = name.isNotBlank() && pinValid,
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
