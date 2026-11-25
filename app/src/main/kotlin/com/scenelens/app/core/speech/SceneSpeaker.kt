package com.scenelens.app.core.speech

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

data class SceneSpeaker(
  val isReady: Boolean,
  val speak: (String) -> Unit,
  val stop: () -> Unit,
)

@Composable
fun rememberSceneSpeaker(): SceneSpeaker {
  val context = LocalContext.current
  var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
  var isReady by remember { mutableStateOf(false) }

  DisposableEffect(context) {
    val engine = TextToSpeech(context) { status ->
      isReady = status == TextToSpeech.SUCCESS
    }
    textToSpeech = engine
    onDispose {
      isReady = false
      engine.stop()
      engine.shutdown()
      textToSpeech = null
    }
  }

  return SceneSpeaker(
    isReady = isReady,
    speak = { text ->
      textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "scenelens-result")
    },
    stop = { textToSpeech?.stop() },
  )
}
