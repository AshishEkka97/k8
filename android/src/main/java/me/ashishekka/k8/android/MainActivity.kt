package me.ashishekka.k8.android

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import me.ashishekka.k8.core.Chip8
import me.ashishekka.k8.core.Chip8Impl
import me.ashishekka.k8.core.KeyEventType
import me.ashishekka.k8.core.VideoMemory


class MainActivity : AppCompatActivity() {

    private val chip8 = Chip8Impl(lifecycleScope)

    private val toneGenerator by lazy { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val romFile = assets.open("c8games/INVADERS")
        val romData = romFile.readBytes()
        val emulator = findViewById<ComposeView>(R.id.emulator)
        chip8.loadRom(romData)
        emulator?.setContent {
            MaterialTheme { // or AppCompatTheme
                MainLayout(chip8, toneGenerator)
            }
        }
        chip8.start()
    }
}

@Composable
fun MainLayout(chip8: Chip8, toneGenerator: ToneGenerator) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("K8 (Kate)") }) },
    ) {
        val sound = chip8.getSoundState()
        PlaySound(toneGenerator, sound.value)
        Column(
            modifier = Modifier.fillMaxHeight().padding(it),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val videoMemory = chip8.getVideoMemoryState()
            Row { Screen(videoMemory.value) }
            Row { Keypad(chip8) }
        }
    }
}

@Composable
fun Screen(videoMemory: VideoMemory) {
    BoxWithConstraints {
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val blockSize = size.width / 64
            videoMemory.forEachIndexed { row, rowData ->
                rowData.forEachIndexed { col, _ ->
                    val xx = blockSize * col.toFloat()
                    val yy = blockSize * row.toFloat()
                    val color = if (videoMemory[row][col]) Color.White else Color.Black
                    drawRect(color, topLeft = Offset(xx, yy), Size(blockSize, blockSize))
                }
            }
        }
    }
}

@Composable
fun Keypad(chip8: Chip8) {
    Column() {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Key(1, chip8)
            Key(2, chip8)
            Key(3, chip8)
            Key(12, chip8)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Key(4, chip8)
            Key(5, chip8)
            Key(6, chip8)
            Key(13, chip8)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Key(7, chip8)
            Key(8, chip8)
            Key(9, chip8)
            Key(14, chip8)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Key(10, chip8)
            Key(0, chip8)
            Key(11, chip8)
            Key(15, chip8)
        }
    }
}

@kotlin.OptIn(ExperimentalFoundationApi::class)
@Composable
fun Key(number: Int, chip8: Chip8) {
    Button(
        modifier = Modifier.combinedClickable(
            onClick = { chip8.onKey(number, KeyEventType.CLICK) },
            onLongClick = { chip8.onKey(number, KeyEventType.LONG) }
        ),
        onClick = { chip8.onKey(number, KeyEventType.CLICK) }
    ) {
        Text(
            text = "${number.toUInt().toString(16).toUpperCase()}",
            style = MaterialTheme.typography.h6
        )
    }
}

@Composable
fun PlaySound(toneGenerator: ToneGenerator, play: Boolean) {
    if (play) toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    else toneGenerator.stopTone()
}