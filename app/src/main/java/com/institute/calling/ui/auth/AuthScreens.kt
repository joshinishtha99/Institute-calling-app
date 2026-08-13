package com.institute.calling.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.institute.calling.domain.model.City
import com.institute.calling.ui.components.BackTopBar
import com.institute.calling.ui.components.InitialAvatar
import com.institute.calling.ui.components.ListRow
import com.institute.calling.ui.components.avatarColorFor
import com.institute.calling.ui.session.SessionViewModel

private val screenPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp)

@Composable
fun LandingScreen(onOwner: () -> Unit, onEmployee: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(78.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(38.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("Institute Calling", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("Choose how you're signing in", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(36.dp))
        RoleButton(
            title = "Owner login",
            subtitle = "Neha, Deepa & admins",
            icon = Icons.Filled.Person,
            accent = Color(0xFF5B4BD6),
            onClick = onOwner,
        )
        Spacer(Modifier.height(14.dp))
        RoleButton(
            title = "Employee login",
            subtitle = "Pick your city, branch, then name",
            icon = Icons.Filled.Phone,
            accent = MaterialTheme.colorScheme.primary,
            onClick = onEmployee,
        )
    }
}

@Composable
private fun RoleButton(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, onClick: () -> Unit) {
    Surface(
        color = accent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().height(74.dp).clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(accent, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun CityScreen(
    sessionViewModel: SessionViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCitySelected: () -> Unit,
) {
    val state by sessionViewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(screenPadding)) {
        BackTopBar("Your city", "Select your city", onBack)
        when {
            state.loadingStructure -> LoadingBlock()
            state.structureError != null -> ErrorBlock(state.structureError!!) { sessionViewModel.loadStructure() }
            state.cities.isEmpty() -> EmptyBlock("No cities set up yet.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(state.cities) { index, city ->
                    ListRow(
                        title = city.name,
                        subtitle = "${city.branches.size} branch${if (city.branches.size != 1) "es" else ""}",
                        leading = {
                            LeadingIcon(Icons.Filled.LocationOn, avatarColorFor(index))
                        },
                        onClick = { sessionViewModel.selectCity(city); onCitySelected() },
                    )
                }
            }
        }
    }
}

@Composable
fun BranchScreen(
    sessionViewModel: SessionViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onBranchSelected: () -> Unit,
) {
    val state by sessionViewModel.state.collectAsStateWithLifecycle()
    val city: City? = state.selectedCity
    Column(Modifier.fillMaxSize().padding(screenPadding)) {
        BackTopBar(city?.name ?: "Branch", "Select your branch", onBack)
        when {
            city == null -> EmptyBlock("Pick a city first.")
            city.branches.isEmpty() -> EmptyBlock("No branches in this city yet.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(city.branches) { index, branch ->
                    ListRow(
                        title = branch.name,
                        subtitle = "${branch.callers.size} caller${if (branch.callers.size != 1) "s" else ""}",
                        leading = { LeadingIcon(Icons.Outlined.Home, avatarColorFor(index)) },
                        onClick = { sessionViewModel.selectBranch(branch); onBranchSelected() },
                    )
                }
            }
        }
    }
}

@Composable
fun CallerPickScreen(
    sessionViewModel: SessionViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCallerSelected: () -> Unit,
) {
    val state by sessionViewModel.state.collectAsStateWithLifecycle()
    val branch = state.selectedBranch
    Column(Modifier.fillMaxSize().padding(screenPadding)) {
        BackTopBar(branch?.name ?: "Branch", "Tap your name", onBack)
        when {
            branch == null -> EmptyBlock("Pick a branch first.")
            branch.callers.isEmpty() -> EmptyBlock("No callers here yet. An owner can add them.")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(branch.callers) { index, caller ->
                    ListRow(
                        title = caller.name,
                        subtitle = branch.name,
                        leading = { InitialAvatar(caller.initial, avatarColorFor(index)) },
                        onClick = { sessionViewModel.selectCaller(caller.id, caller.name); onCallerSelected() },
                    )
                }
            }
        }
    }
}

@Composable
fun OwnerPickScreen(
    sessionViewModel: SessionViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOwnerSelected: () -> Unit,
) {
    val state by sessionViewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(screenPadding)) {
        BackTopBar("Owners", "Tap your name to sign in", onBack)
        when {
            state.loadingStructure -> LoadingBlock()
            state.structureError != null -> ErrorBlock(state.structureError!!) { sessionViewModel.loadStructure() }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(state.owners) { index, owner ->
                        ListRow(
                            title = owner.name,
                            subtitle = "Owner",
                            leading = { InitialAvatar(owner.initial, avatarColorFor(index)) },
                            onClick = { sessionViewModel.selectOwner(owner); onOwnerSelected() },
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "More owners can be added later.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun LeadingIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Box(Modifier.size(46.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun LoadingBlock() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun EmptyBlock(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(14.dp))
        androidx.compose.material3.Button(onClick = onRetry) { Text("Retry") }
    }
}
