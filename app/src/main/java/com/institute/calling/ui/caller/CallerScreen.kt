package com.institute.calling.ui.caller

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.Manifest
import android.content.pm.PackageManager
import com.institute.calling.domain.model.AuthUser
import com.institute.calling.domain.model.Disposition
import com.institute.calling.ui.components.InitialAvatar
import com.institute.calling.ui.components.NumericKeypad
import com.institute.calling.ui.components.avatarColorFor
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CallerScreen(
    user: AuthUser,
    onLogout: () -> Unit,
    viewModel: CallerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    val callPermissions = arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.CALL_PHONE] == true &&
            result[Manifest.permission.READ_PHONE_STATE] == true
        if (granted) {
            viewModel.onCall()
        } else {
            scope.launch { snackbarHostState.showSnackbar("Phone permissions are needed to place calls") }
        }
    }

    fun startCall() {
        val granted = callPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (granted) viewModel.onCall() else permissionLauncher.launch(callPermissions)
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { inner ->
        Column(Modifier.fillMaxSize().padding(inner).padding(horizontal = 22.dp)) {
            CallerAppBar(user = user, callsToday = state.callsToday, onLogout = onLogout)
            when (state.phase) {
                CallPhase.IDLE -> DialPad(state, viewModel, onCall = { startCall() })
                CallPhase.CALLING, CallPhase.CONNECTED -> InCall(state, onEnd = viewModel::onEndCall)
                CallPhase.DISPOSITION -> DispositionStep(state, user, viewModel)
            }
        }
    }
}

@Composable
private fun CallerAppBar(user: AuthUser, callsToday: Int, onLogout: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InitialAvatar(user.initial, avatarColorFor(1), size = 40)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(user.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text(user.branchLabel ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = CircleShape) {
            Text(
                "$callsToday today",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        IconButton(onClick = onLogout) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
        }
    }
}

@Composable
private fun DialPad(state: CallerUiState, viewModel: CallerViewModel, onCall: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(8.dp))
        Text("Enter number", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = state.number.ifEmpty { "Tap to dial" },
            fontSize = 32.sp,
            color = if (state.number.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 6.dp),
        )
        if (state.hasInvalidNumber) {
            Text(
                "Enter a valid 10-digit mobile",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        NumericKeypad(
            extraKeyLabel = "paste",
            onKey = viewModel::onDigit,
            onBackspace = viewModel::onBackspace,
            onExtra = viewModel::onPasteSample,
        )
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier
                .size(70.dp)
                .background(
                    if (state.canCall) Color(0xFF146C2E) else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape,
                )
                .clickable(enabled = state.canCall, onClick = onCall),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Phone, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun InCall(state: CallerUiState, onEnd: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(92.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(38.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(
            if (state.phase == CallPhase.CONNECTED) "CONNECTED" else "CALLING…",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(state.number, fontSize = 28.sp)
        Text(formatDuration(state.elapsedSeconds.toLong()), fontSize = 42.sp, fontWeight = FontWeight.Light)
        Spacer(Modifier.height(34.dp))
        Box(
            Modifier.size(70.dp).background(MaterialTheme.colorScheme.error, CircleShape).clickable(onClick = onEnd),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.CallEnd, contentDescription = "End call", tint = MaterialTheme.colorScheme.onError, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun DispositionStep(state: CallerUiState, user: AuthUser, viewModel: CallerViewModel) {
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(6.dp))
        Text("How did it go?", fontSize = 23.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))
        Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryCol("Number", state.number)
                SummaryCol("Time", "${formatClock(state.startTimeMillis)}–${formatClock(state.endTimeMillis)}")
                SummaryCol("Duration", formatDuration(state.durationSecondsForUi()))
            }
        }
        Spacer(Modifier.height(14.dp))
        Disposition.entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = 9.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                row.forEach { d ->
                    OutcomeChip(d, state.selectedDisposition == d, Modifier.weight(1f)) { viewModel.onSelectDisposition(d) }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        OutlinedTextField(
            value = state.notes,
            onValueChange = viewModel::onNotesChange,
            placeholder = { Text("Add a note (optional)") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { viewModel.onSave(user) },
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth().height(54.dp),
        ) {
            Text(if (state.saving) "Saving…" else "Save", fontSize = 16.sp)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SummaryCol(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun OutcomeChip(disposition: Disposition, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        shape = RoundedCornerShape(15.dp),
        modifier = modifier
            .then(if (!selected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(15.dp)) else Modifier)
            .clickable(onClick = onClick),
    ) {
        Text(
            disposition.label,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 15.dp),
        )
    }
}

private fun CallerUiState.durationSecondsForUi(): Long =
    ((endTimeMillis - startTimeMillis) / 1000).coerceAtLeast(0)

private fun formatDuration(totalSeconds: Long): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", m, s)
}

private fun formatClock(millis: Long): String {
    if (millis == 0L) return "—"
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
    return String.format(
        Locale.getDefault(),
        "%02d:%02d",
        cal.get(java.util.Calendar.HOUR_OF_DAY),
        cal.get(java.util.Calendar.MINUTE),
    )
}
