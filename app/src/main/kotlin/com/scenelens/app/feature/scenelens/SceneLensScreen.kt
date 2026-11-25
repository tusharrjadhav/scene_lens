package com.scenelens.app.feature.scenelens

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.scenelens.app.R
import com.scenelens.app.core.clipboard.rememberTextClipboard
import com.scenelens.app.core.speech.rememberSceneSpeaker
import com.scenelens.app.feature.home.HomeScreen
import com.scenelens.app.feature.results.AnalysisResultScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SceneLensScreen(
  uiState: SceneLensUiState,
  onTakePhoto: () -> Unit = {},
  onChoosePhoto: () -> Unit = {},
  onReset: () -> Unit = {},
  onSelectItem: (String) -> Unit = {},
  onClearError: () -> Unit = {},
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val speaker = rememberSceneSpeaker()
  val clipboard = rememberTextClipboard()

  LaunchedEffect(uiState.errorMessage) {
    uiState.errorMessage?.let { message ->
      snackbarHostState.showSnackbar(message)
      onClearError()
    }
  }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          Text(
            text = stringResource(R.string.app_name),
            fontWeight = FontWeight.Bold,
          )
        },
        actions = {
          if (uiState.analysis != null) {
            IconButton(onClick = onReset) {
              Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.new_image),
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
        ),
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) { padding ->
    val contentModifier = Modifier
      .padding(padding)
      .navigationBarsPadding()
    when {
      uiState.isAnalyzing -> AnalysisProgress(contentModifier)
      uiState.analysis != null -> AnalysisResultScreen(
        analysis = uiState.analysis,
        selectedItemId = uiState.selectedItemId,
        canSpeak = speaker.isReady,
        onSpeak = speaker.speak,
        onStop = speaker.stop,
        onCopy = clipboard::copy,
        onSelectItem = onSelectItem,
        onNewImage = onChoosePhoto,
        modifier = contentModifier,
      )
      else -> HomeScreen(
        onTakePhoto = onTakePhoto,
        onChoosePhoto = onChoosePhoto,
        modifier = contentModifier,
      )
    }
  }
}
