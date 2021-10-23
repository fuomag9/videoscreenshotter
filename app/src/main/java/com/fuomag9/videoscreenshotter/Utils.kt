package com.fuomag9.videoscreenshotter

import android.content.Context
import android.media.AudioManager
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun SoundIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    androidx.compose.material.IconButton(onClick = { playClickSound(context);onClick() }) {
        content()
    }
}

@Composable
fun SoundButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    Button(
        onClick = { playClickSound(context);onClick() },
        modifier = modifier,
        enabled = enabled
    ) {
        content()
    }
}

fun playClickSound(context: Context) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f)
}
