package com.cholobotar.app

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            val vm: StudioViewModel = viewModel()
            CholobotarTheme(dark = vm.dark) {
                MainScreen(vm)
            }
        }
    }
}

@Composable
fun MainScreen(vm: StudioViewModel) {
    val ctx = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.setMic(true)
    }
    val toggleMic: () -> Unit = {
        if (vm.micOn) {
            vm.setMic(false)
        } else if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            vm.setMic(true)
        } else {
            permLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    val pickClosed = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) vm.importImage(uri, false)
    }
    val pickOpen = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) vm.importImage(uri, true)
    }

    // one frame loop for the whole app: bounce, mouth, teleprompter
    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.1f)
            last = now
            vm.frame(dt, now)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 },
                    icon = { Text("🎬") }, label = { Text("Studio") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 },
                    icon = { Text("📜") }, label = { Text("Script") })
                NavigationBarItem(selected = tab == 2, onClick = { tab = 2 },
                    icon = { Text("⚙️") }, label = { Text("Setup") })
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                0 -> StudioScreen(vm, toggleMic)
                1 -> ScriptScreen(vm)
                else -> SetupScreen(
                    vm, toggleMic,
                    onPickClosed = { pickClosed.launch("image/*") },
                    onPickOpen = { pickOpen.launch("image/*") }
                )
            }
        }
    }
}
