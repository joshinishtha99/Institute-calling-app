package com.institute.calling.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.institute.calling.domain.model.Role
import com.institute.calling.ui.components.InitialAvatar
import com.institute.calling.ui.components.NumericKeypad
import com.institute.calling.ui.components.PinDots
import com.institute.calling.ui.components.avatarColorFor
import com.institute.calling.ui.session.SessionViewModel

@Composable
fun PinScreen(
    sessionViewModel: SessionViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLoggedIn: (Role) -> Unit,
) {
    val state by sessionViewModel.state.collectAsStateWithLifecycle()
    var pin by remember { mutableStateOf("") }

    val isOwner = state.pendingRole == Role.OWNER
    val name = if (isOwner) state.pendingOwnerName else state.pendingCallerName
    val roleLabel = if (isOwner) "Owner" else "Caller"

    Column(Modifier.fillMaxSize().padding(PaddingValues(horizontal = 22.dp, vertical = 8.dp))) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(6.dp))
            InitialAvatar((name?.firstOrNull()?.uppercase() ?: "?"), avatarColorFor(if (isOwner) 0 else 1))
            Spacer(Modifier.height(10.dp))
            Text(name ?: "", fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Text(roleLabel, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            Text(
                state.authError ?: "Enter your PIN",
                fontSize = 14.sp,
                color = if (state.authError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            PinDots(filled = pin.length)
        }

        Spacer(Modifier.weight(1f))

        NumericKeypad(
            extraKeyLabel = null,
            onKey = { d -> if (pin.length < 4) pin += d },
            onBackspace = { pin = pin.dropLast(1) },
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { sessionViewModel.login(pin) { role -> onLoggedIn(role) } },
            enabled = pin.length == 4 && !state.authenticating,
            modifier = Modifier.fillMaxWidth().height(54.dp),
        ) {
            Text(if (state.authenticating) "Signing in…" else "Log in", fontSize = 16.sp)
        }
    }
}
