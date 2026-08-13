package com.institute.calling.ui.owner

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.institute.calling.domain.model.CallLogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallReviewScreen(
    onBack: () -> Unit,
    viewModel: CallReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showPicker by remember { mutableStateOf(false) }

    // Device back goes up one level; exits the screen only at the top level.
    BackHandler(enabled = true) { if (!viewModel.goUp()) onBack() }

    val title = when (state.level) {
        ReviewLevel.CITIES -> "Call review"
        ReviewLevel.BRANCHES -> state.selectedCity ?: "Branches"
        ReviewLevel.CALLS -> state.currentBranch?.branchName ?: "Calls"
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (!viewModel.goUp()) onBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.padding(horizontal = 2.dp))
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            IconButton(onClick = { showPicker = true }) { Icon(Icons.Filled.DateRange, contentDescription = "Pick date") }
        }

        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().clickable { showPicker = true },
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.padding(horizontal = 6.dp))
                Column(Modifier.weight(1f)) {
                    Text(prettyDate(state.dateMillis), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimary)
                    Text("${state.grandTotal} calls total", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
                Text("Change", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Spacer(Modifier.height(12.dp))

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Column(
                Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(state.error!!, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { viewModel.reload() }) { Text("Retry") }
            }
            else -> when (state.level) {
                ReviewLevel.CITIES -> CityList(state, viewModel)
                ReviewLevel.BRANCHES -> BranchList(state, viewModel)
                ReviewLevel.CALLS -> CallList(state)
            }
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.dateMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.onDateSelected(it) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun CityList(state: CallReviewUiState, viewModel: CallReviewViewModel) {
    if (state.cities.all { it.total == 0 }) {
        EmptyDay()
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.cities) { city ->
            DrillRow(
                icon = Icons.Filled.LocationOn,
                title = city.cityName,
                trailing = "${city.total} calls",
                onClick = { viewModel.openCity(city.cityName) },
            )
        }
    }
}

@Composable
private fun BranchList(state: CallReviewUiState, viewModel: CallReviewViewModel) {
    val branches = state.currentCity?.branches.orEmpty()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(branches) { branch ->
            DrillRow(
                icon = Icons.Outlined.Home,
                title = branch.branchName,
                trailing = "${branch.total} calls",
                onClick = { viewModel.openBranch(branch.branchId) },
            )
        }
    }
}

@Composable
private fun CallList(state: CallReviewUiState) {
    val calls = state.currentBranch?.calls.orEmpty()
    if (calls.isEmpty()) {
        EmptyDay()
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            OutcomeBreakdown(calls)
            Spacer(Modifier.height(6.dp))
            Text(
                "Calls",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(calls) { call -> CallRow(call) }
    }
}

/** Ordered outcomes with a label and colour for the breakdown bars. */
private val OUTCOME_META: List<Triple<String, String, Color>> = listOf(
    Triple("INTERESTED", "Interested", Color(0xFF2E7D32)),
    Triple("FOLLOW_UP", "Follow Up", Color(0xFF1565C0)),
    Triple("NOT_INTERESTED", "Not Interested", Color(0xFF757575)),
    Triple("BUSY", "Busy", Color(0xFFEF6C00)),
    Triple("WRONG_NUMBER", "Wrong Number", Color(0xFFC62828)),
    Triple("SWITCHED_OFF", "Switched Off", Color(0xFF6A1B9A)),
)

@Composable
private fun OutcomeBreakdown(calls: List<CallLogEntry>) {
    val counts = calls.groupingBy { it.disposition }.eachCount()
    val total = calls.size
    val max = (counts.values.maxOrNull() ?: 0).coerceAtLeast(1)

    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Outcomes", fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text("$total total", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            OUTCOME_META.forEach { (raw, label, color) ->
                val count = counts[raw] ?: 0
                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontSize = 12.5.sp, modifier = Modifier.width(96.dp))
                    Box(
                        Modifier
                            .weight(1f)
                            .height(16.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
                    ) {
                        if (count > 0) {
                            Box(
                                Modifier
                                    .fillMaxWidth(count.toFloat() / max.toFloat())
                                    .height(16.dp)
                                    .background(color, RoundedCornerShape(8.dp)),
                            )
                        }
                    }
                    Text(
                        "$count",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(36.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    )
                }
            }
        }
    }
}

@Composable
private fun DrillRow(icon: ImageVector, title: String, trailing: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.padding(horizontal = 6.dp))
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text(trailing, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.padding(horizontal = 2.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun CallRow(call: CallLogEntry) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(call.callerName, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text(dispositionLabel(call.disposition), fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(2.dp))
            Text("+91 ${call.phoneNumber}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(
                "${clock(call.startMillis)}–${clock(call.endMillis)} · ${duration(call.durationSeconds)}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (call.notes.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text("\u201C${call.notes}\u201D", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun EmptyDay() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No calls on this date.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

private val prettyFmt = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private fun prettyDate(millis: Long): String = prettyFmt.format(Date(millis))

private val clockFmt = SimpleDateFormat("HH:mm", Locale.getDefault()) // device-local time
private fun clock(millis: Long): String = if (millis == 0L) "\u2014" else clockFmt.format(Date(millis))

private fun duration(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", m, s)
}

private fun dispositionLabel(raw: String): String = when (raw) {
    "INTERESTED" -> "Interested"
    "FOLLOW_UP" -> "Follow Up"
    "NOT_INTERESTED" -> "Not Interested"
    "BUSY" -> "Busy"
    "WRONG_NUMBER" -> "Wrong Number"
    "SWITCHED_OFF" -> "Switched Off"
    else -> raw
}