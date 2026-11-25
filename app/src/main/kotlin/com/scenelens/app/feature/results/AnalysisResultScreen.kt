package com.scenelens.app.feature.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scenelens.app.R
import com.scenelens.app.core.ui.SceneLensUiTokens
import com.scenelens.app.core.ui.labelResource
import com.scenelens.app.domain.model.SceneAnalysis

@Composable
internal fun AnalysisResultScreen(
  analysis: SceneAnalysis,
  selectedItemId: String?,
  canSpeak: Boolean,
  onSpeak: (String) -> Unit,
  onStop: () -> Unit,
  onCopy: (String) -> Unit,
  onSelectItem: (String) -> Unit,
  onNewImage: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val selectedItem = analysis.items.firstOrNull { it.id == selectedItemId }
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      AnalyzedImage(
        analysis = analysis,
        selectedItemId = selectedItemId,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
      )
    }
    item {
      ResultSummary(
        summary = analysis.summary,
        canSpeak = canSpeak,
        onSpeak = onSpeak,
        onStop = onStop,
      )
    }
    if (selectedItem != null) {
      item {
        SelectedItemCard(
          item = selectedItem,
          modifier = Modifier.padding(horizontal = 20.dp),
          onSpeak = onSpeak,
          canSpeak = canSpeak,
        )
      }
    }
    item {
      Text(
        text = stringResource(R.string.detected_items),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .padding(horizontal = 20.dp)
          .semantics { heading() },
      )
    }
    item {
      DetectedItemRow(
        analysis = analysis,
        selectedItemId = selectedItemId,
        onSelectItem = onSelectItem,
      )
    }
    item {
      DetailedBreakdown(analysis.details)
    }
    analysis.recognizedText?.let { text ->
      item {
        RecognizedTextCard(
          text = text,
          onCopy = { onCopy(text) },
          modifier = Modifier.padding(horizontal = 20.dp),
        )
      }
    }
    item {
      Button(
        onClick = onNewImage,
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = SceneLensUiTokens.PrimaryActionHeight)
          .padding(horizontal = 20.dp),
      ) {
        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
        Text(
          stringResource(R.string.choose_another),
          modifier = Modifier.padding(start = 8.dp),
        )
      }
    }
    item {
      Text(
        text = stringResource(R.string.safety_note),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp),
      )
    }
  }
}

@Composable
private fun ResultSummary(
  summary: String,
  canSpeak: Boolean,
  onSpeak: (String) -> Unit,
  onStop: () -> Unit,
) {
  Column(
    modifier = Modifier.padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Text(
      text = summary,
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.semantics { heading() },
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      ElevatedButton(
        onClick = { onSpeak(summary) },
        enabled = canSpeak,
        modifier = Modifier
          .weight(1f)
          .heightIn(min = SceneLensUiTokens.SpeechControlHeight),
      ) {
        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null)
        Text(stringResource(R.string.read_aloud), modifier = Modifier.padding(start = 8.dp))
      }
      OutlinedButton(
        onClick = onStop,
        modifier = Modifier.heightIn(min = SceneLensUiTokens.SpeechControlHeight),
      ) {
        Icon(Icons.Rounded.Stop, contentDescription = stringResource(R.string.stop_speech))
      }
    }
  }
}

@Composable
private fun DetectedItemRow(
  analysis: SceneAnalysis,
  selectedItemId: String?,
  onSelectItem: (String) -> Unit,
) {
  if (analysis.items.isEmpty()) {
    Text(
      text = stringResource(R.string.no_items),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 20.dp),
    )
  } else {
    LazyRow(
      contentPadding = PaddingValues(horizontal = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      items(analysis.items, key = { it.id }) { item ->
        val state = item.confidenceBand?.let { stringResource(it.labelResource()) }
          ?: item.source.name.replace('_', ' ').lowercase()
        FilterChip(
          selected = selectedItemId == item.id,
          onClick = { onSelectItem(item.id) },
          label = { Text(item.label) },
          modifier = Modifier
            .heightIn(min = SceneLensUiTokens.MinimumTouchTarget)
            .semantics {
              stateDescription = state
            },
        )
      }
    }
  }
}

@Composable
private fun DetailedBreakdown(details: List<String>) {
  Column(
    modifier = Modifier.padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Text(
      text = stringResource(R.string.detailed_breakdown),
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.semantics { heading() },
    )
    details.forEach { detail ->
      Text(
        text = "• $detail",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
