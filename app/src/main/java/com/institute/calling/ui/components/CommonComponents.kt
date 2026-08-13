package com.institute.calling.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Deterministic accent colour for an avatar, from a small brand-consistent set. */
private val avatarColors = listOf(
    Color(0xFF5B4BD6), Color(0xFF00696E), Color(0xFF8A5000),
    Color(0xFFBA1A1A), Color(0xFF146C2E), Color(0xFF7B4BD6),
)

fun avatarColorFor(index: Int): Color = avatarColors[index % avatarColors.size]

@Composable
fun InitialAvatar(
    initial: String,
    color: Color,
    size: Int = 46,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(size.dp).background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size / 2.6f).sp)
    }
}

/** A tappable list row with an avatar/leading slot, title, subtitle and chevron. */
@Composable
fun ListRow(
    title: String,
    subtitle: String?,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            leading()
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
fun BackTopBar(title: String, subtitle: String? = null, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Spacer(Modifier.width(4.dp))
        Column {
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Row of four PIN indicator dots. */
@Composable
fun PinDots(filled: Int, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = modifier) {
        repeat(4) { i ->
            val on = i < filled
            Box(
                Modifier
                    .size(15.dp)
                    .background(
                        if (on) MaterialTheme.colorScheme.primary else Color.Transparent,
                        CircleShape,
                    ),
            ) {
                if (!on) {
                    Box(
                        Modifier
                            .size(15.dp)
                            .background(Color.Transparent, CircleShape),
                    )
                }
            }
        }
    }
}

/**
 * A 3x4 numeric keypad. [extraKey] fills the bottom-left slot (e.g. "paste" for the
 * dialpad, or blank for the PIN pad). onKey receives the tapped digit; onBackspace
 * the delete key; onExtra the extra key.
 */
@Composable
fun NumericKeypad(
    extraKeyLabel: String? = null,
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onExtra: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(extraKeyLabel ?: "", "0", "\u2403"), // last is backspace marker
    )
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    Box(Modifier.weight(1f)) {
                        when (key) {
                            "" -> Spacer(Modifier.fillMaxWidth().height(56.dp))
                            "\u2403" -> KeypadButton(leadingBackspace = true, onClick = onBackspace)
                            extraKeyLabel -> KeypadButton(text = extraKeyLabel, subtle = true, onClick = onExtra)
                            else -> KeypadButton(text = key, onClick = { onKey(key) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String? = null,
    subtle: Boolean = false,
    leadingBackspace: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        color = if (subtle || leadingBackspace) Color.Transparent
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = CircleShape,
        modifier = Modifier.fillMaxWidth().height(56.dp).clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                leadingBackspace -> Icon(
                    Icons.Filled.Backspace,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                text != null -> Text(
                    text,
                    fontSize = if (subtle) 14.sp else 24.sp,
                    color = if (subtle) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
