package com.scenelens.app.core.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

fun interface TextClipboard {
  fun copy(text: String)
}

@Composable
fun rememberTextClipboard(): TextClipboard {
  val context = LocalContext.current.applicationContext
  return remember(context) {
    TextClipboard { text ->
      context.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(
        ClipData.newPlainText("SceneLens text", text),
      )
    }
  }
}
