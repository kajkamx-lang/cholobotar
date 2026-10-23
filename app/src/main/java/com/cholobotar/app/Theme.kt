package com.cholobotar.app

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class GlassColors(
    val panel: Color,
    val border: Color,
    val text: Color,
    val subtext: Color,
    val accent: Color,
    val bg: Brush
)

val LightGlass = GlassColors(
    panel = Color(0xB3FFFFFF),
    border = Color(0x1F000000),
    text = Color(0xFF14181F),
    subtext = Color(0xFF5A6272),
    accent = Color(0xFF6C4DFF),
    bg = Brush.verticalGradient(listOf(Color(0xFFF2F5FA), Color(0xFFE6ECF8)))
)

val DarkGlass = GlassColors(
    panel = Color(0x1FFFFFFF),
    border = Color(0x33FFFFFF),
    text = Color.White,
    subtext = Color(0xFFB0B0C0),
    accent = Color(0xFF8E6CFF),
    bg = SolidColor(Color.Black)
)

val LocalGlass = staticCompositionLocalOf { LightGlass }

@Composable
fun CholobotarTheme(dark: Boolean, content: @Composable () -> Unit) {
    val g = if (dark) DarkGlass else LightGlass
    val scheme = if (dark) {
        darkColorScheme(
            primary = g.accent, background = Color.Black, surface = Color.Black,
            onBackground = Color.White, onSurface = Color.White
        )
    } else {
        lightColorScheme(
            primary = g.accent, background = Color(0xFFF2F5FA), surface = Color(0xFFF7F9FD),
            onBackground = Color(0xFF14181F), onSurface = Color(0xFF14181F)
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            if (activity != null) {
                WindowCompat.getInsetsController(activity.window, view).isAppearanceLightStatusBars = !dark
            }
        }
    }
    CompositionLocalProvider(LocalGlass provides g) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val g = LocalGlass.current
    Box(
        modifier
            .clip(shape)
            .background(g.panel)
            .border(1.dp, g.border, shape),
        content = content
    )
}

@Composable
fun GlassChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val g = LocalGlass.current
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .clip(shape)
            .background(if (selected) g.accent.copy(alpha = 0.3f) else g.panel)
            .border(1.dp, if (selected) g.accent else g.border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = g.text, fontSize = 13.sp)
    }
}
