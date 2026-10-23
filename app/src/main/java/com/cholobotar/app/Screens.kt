package com.cholobotar.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// ───────────────────────── STUDIO ─────────────────────────

@Composable
fun StudioScreen(vm: StudioViewModel, onToggleMic: () -> Unit) {
    var showPrompter by rememberSaveable { mutableStateOf(true) }
    var showPad by rememberSaveable { mutableStateOf(true) }

    Box(
        Modifier
            .fillMaxSize()
            .background(BACKGROUNDS[vm.background] ?: BACKGROUNDS.getValue("STUDIO"))
    ) {
        AvatarView(
            vm,
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (showPad) 250.dp else 24.dp)
                .fillMaxWidth(0.7f)
                .aspectRatio(1f)
        )
        EffectsOverlay(vm.effects)

        Column(
            Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GlassChip(if (vm.micOn) "🎙 ON" else "🎙 OFF", selected = vm.micOn, onClick = onToggleMic)
                GlassChip("📜 Prompter", selected = showPrompter, onClick = { showPrompter = !showPrompter })
                GlassChip("🎛 Pad", selected = showPad, onClick = { showPad = !showPad })
                GlassChip(if (vm.dark) "☀️ Light" else "🌙 AMOLED", onClick = { vm.toggleDark() })
            }
            if (showPrompter) {
                TeleprompterView(
                    vm.tp,
                    onTap = { vm.togglePlay() },
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.32f)
                )
                PrompterControls(vm)
            }
            Spacer(Modifier.weight(1f))
            if (showPad) ActionPad(vm)
        }
    }
}

@Composable
fun PrompterControls(vm: StudioViewModel) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val running = vm.tp.playing || vm.tp.countdown > 0
        GlassChip(if (running) "⏸" else "▶", selected = running, onClick = { vm.togglePlay() })
        GlassChip("⟲", onClick = { vm.restart() })
        GlassChip("−", onClick = { vm.setWpm(vm.tp.wpm - 10) })
        GlassChip("${vm.tp.wpm} WPM", onClick = { })
        GlassChip("+", onClick = { vm.setWpm(vm.tp.wpm + 10) })
        GlassChip("A−", onClick = { vm.setFont(vm.tp.fontSp - 2) })
        GlassChip("A+", onClick = { vm.setFont(vm.tp.fontSp + 2) })
        GlassChip("🪞", selected = vm.tp.mirror, onClick = { vm.tp.mirror = !vm.tp.mirror })
    }
}

@Composable
fun AvatarView(vm: StudioViewModel, modifier: Modifier = Modifier) {
    val density = LocalDensity.current.density
    Box(
        modifier.graphicsLayer {
            vm.tick // read per frame so the layer redraws
            translationY = vm.bounce.offsetY * density
            translationX = vm.bounce.shakeX * density
        },
        contentAlignment = Alignment.Center
    ) {
        val img: ImageBitmap? =
            if (vm.mouthOpen) (vm.openBmp ?: vm.closedBmp) else (vm.closedBmp ?: vm.openBmp)
        if (img != null) {
            Image(
                bitmap = img,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            DefaultAvatar(vm.mouthOpen)
        }
        val e = vm.emote
        if (e != null) {
            Text(e, fontSize = 56.sp, modifier = Modifier.align(Alignment.TopEnd))
        }
    }
}

@Composable
fun DefaultAvatar(open: Boolean) {
    Canvas(Modifier.size(220.dp)) {
        val c = center
        val r = size.minDimension / 2f
        drawCircle(Color(0xFFFFC857), r, c)
        drawCircle(Color(0xFF2B2B2B), r * 0.09f, Offset(c.x - r * 0.35f, c.y - r * 0.2f))
        drawCircle(Color(0xFF2B2B2B), r * 0.09f, Offset(c.x + r * 0.35f, c.y - r * 0.2f))
        if (open) {
            drawOval(
                Color(0xFF7A1F1F),
                topLeft = Offset(c.x - r * 0.22f, c.y + r * 0.2f),
                size = Size(r * 0.44f, r * 0.42f)
            )
        } else {
            drawLine(
                Color(0xFF2B2B2B),
                Offset(c.x - r * 0.28f, c.y + r * 0.35f),
                Offset(c.x + r * 0.28f, c.y + r * 0.35f),
                strokeWidth = r * 0.06f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun EffectsOverlay(effects: Set<String>) {
    if ("RETRO_GLOW" in effects) {
        Box(Modifier.fillMaxSize().background(Color(0x33FF6EC7)))
    }
    if ("HORROR_RED" in effects) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(listOf(Color(0x33B00020), Color(0xCC4A0000)))
            )
        )
    }
    if ("SPOTLIGHT" in effects) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(listOf(Color.Transparent, Color(0xDD000000)))
            )
        )
    }
}

@Composable
fun ActionPad(vm: StudioViewModel) {
    val haptics = LocalHapticFeedback.current
    val g = LocalGlass.current
    var page by rememberSaveable { mutableIntStateOf(0) }
    GlassPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PAD_PAGES.forEachIndexed { i, p ->
                    GlassChip(p.name, selected = i == page, onClick = { page = i })
                }
            }
            PAD_PAGES[page].buttons.chunked(3).forEach { rowButtons ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    rowButtons.forEach { b ->
                        val on = vm.isActive(b)
                        val shape = RoundedCornerShape(14.dp)
                        Column(
                            Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(shape)
                                .background(b.color.copy(alpha = if (on) 0.4f else 0.15f))
                                .border(if (on) 2.dp else 1.dp, if (on) b.color else g.border, shape)
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    vm.press(b)
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(b.emoji, fontSize = 20.sp)
                            Text(b.label, color = g.text, fontSize = 11.sp)
                        }
                    }
                    repeat(3 - rowButtons.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

// ───────────────────────── SCRIPT ─────────────────────────

@Composable
fun ScriptScreen(vm: StudioViewModel) {
    val g = LocalGlass.current
    Column(
        Modifier
            .fillMaxSize()
            .background(g.bg)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Script", color = g.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Marker likhle prompter-e dekha jabe na, kintu oi shobder jaygay emote/background auto change hobe. Niche chip tap korle marker script-er shesh-e jure jabe.",
            color = g.subtext, fontSize = 12.sp
        )
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            EMOTES.keys.forEach { k ->
                GlassChip("${EMOTES[k]} $k", onClick = { vm.appendToScript(" [EMOTE:$k] ") })
            }
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BACKGROUNDS.keys.forEach { k ->
                GlassChip("🖼 $k", onClick = { vm.appendToScript(" [BG:$k] ") })
            }
        }
        GlassPanel(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = vm.scriptText,
                onValueChange = { vm.updateScript(it) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = g.text,
                    unfocusedTextColor = g.text,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
        val words = vm.tp.script.words.size
        val mins = words / vm.tp.wpm.toFloat()
        Text(
            "$words words • about ${"%.1f".format(mins)} min at ${vm.tp.wpm} WPM",
            color = g.subtext, fontSize = 12.sp
        )
        LabeledSlider("Speed: ${vm.tp.wpm} WPM", vm.tp.wpm.toFloat(), 60f..300f) { vm.setWpm(it.roundToInt()) }
        LabeledSlider("Font size: ${vm.tp.fontSp} sp", vm.tp.fontSp.toFloat(), 18f..72f) { vm.setFont(it.roundToInt()) }
        LabeledSlider("Prompter opacity", vm.tp.alpha, 0.2f..1f) { vm.setPrompterAlpha(it) }
    }
}

@Composable
fun LabeledSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    val g = LocalGlass.current
    Column {
        Text(label, color = g.text, fontSize = 13.sp)
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

// ───────────────────────── SETUP ─────────────────────────

@Composable
fun SetupScreen(
    vm: StudioViewModel,
    onToggleMic: () -> Unit,
    onPickClosed: () -> Unit,
    onPickOpen: () -> Unit
) {
    val g = LocalGlass.current
    Column(
        Modifier
            .fillMaxSize()
            .background(g.bg)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Setup", color = g.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        GlassPanel(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("2-PNG avatar", color = g.text, fontWeight = FontWeight.SemiBold)
                Text(
                    "Mouth-closed ar mouth-open PNG choose koro. Kichhu choose na korle default face dekhabe.",
                    color = g.subtext, fontSize = 12.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PngSlot("Closed", vm.closedBmp, onPickClosed, Modifier.weight(1f))
                    PngSlot("Open", vm.openBmp, onPickOpen, Modifier.weight(1f))
                }
                GlassChip("Reset to default face", onClick = { vm.resetAvatar() })
            }
        }

        GlassPanel(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Microphone", color = g.text, fontWeight = FontWeight.SemiBold)
                GlassChip(
                    if (vm.micOn) "🎙 Mic ON (tap to stop)" else "🎙 Mic OFF (tap to start)",
                    selected = vm.micOn, onClick = onToggleMic
                )
                Text(
                    if (vm.mouthOpen) "Mouth: OPEN" else "Mouth: closed",
                    color = g.subtext, fontSize = 12.sp
                )
                LinearProgressIndicator(
                    progress = { vm.meter },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))
                )
                LabeledSlider(
                    "Sensitivity: mouth open hoy ${vm.sensitivityDb.roundToInt()} dB-er upore",
                    vm.sensitivityDb, -60f..-20f
                ) { vm.setSensitivity(it) }
                Text(
                    "Kotha bolo, meter ar mouth dekho. Mouth na khulle slider-ta bam-e (kom dB) nao.",
                    color = g.subtext, fontSize = 12.sp
                )
            }
        }

        GlassPanel(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Theme", color = g.text, fontWeight = FontWeight.SemiBold)
                GlassChip(
                    if (vm.dark) "☀️ Light Studio-te jao" else "🌙 AMOLED Dark-e jao",
                    onClick = { vm.toggleDark() }
                )
            }
        }

        Text(
            "Video record korte phone-er built-in screen recorder use koro (Studio tab-e). Built-in export Phase 2-te asbe.",
            color = g.subtext, fontSize = 12.sp, textAlign = TextAlign.Start
        )
    }
}

@Composable
fun PngSlot(label: String, bmp: ImageBitmap?, onPick: () -> Unit, modifier: Modifier = Modifier) {
    val g = LocalGlass.current
    val shape = RoundedCornerShape(14.dp)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape)
                .background(g.panel)
                .border(1.dp, g.border, shape)
                .clickable(onClick = onPick),
            contentAlignment = Alignment.Center
        ) {
            if (bmp != null) {
                Image(bitmap = bmp, contentDescription = label, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
            } else {
                Text("+ $label PNG", color = g.subtext, fontSize = 12.sp)
            }
        }
        Text(label, color = g.text, fontSize = 12.sp)
    }
}
