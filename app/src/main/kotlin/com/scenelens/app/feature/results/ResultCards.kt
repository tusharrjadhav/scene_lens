package com.scenelens.app.feature.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scenelens.app.R
import com.scenelens.app.core.ui.SceneLensUiTokens
import com.scenelens.app.core.ui.labelResource
import com.scenelens.app.domain.model.ConfidenceBand
import com.scenelens.app.domain.model.DetectedItem
import kotlin.math.roundToInt

@Composable
internal fun SelectedItemCard(
  item: DetectedItem,
  canSpeak: Boolean,
  onSpeak: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val confidenceLabel = item.confidenceBand?.let { stringResource(it.labelResource()) }
  ElevatedCard(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(SceneLensUiTokens.CardPadding),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          item.label,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
        ResultBadge(item.confidenceBand)
      }
      Text(
        text = stringResource(
          when (item.confidenceBand) {
            ConfidenceBand.HIGH -> R.string.confidence_high
            ConfidenceBand.MEDIUM -> R.string.confidence_medium
            ConfidenceBand.LOW -> R.string.confidence_low
            null -> R.string.ocr_no_confidence
          },
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      TextButton(
        onClick = { onSpeak(item.speechDescription(confidenceLabel)) },
        enabled = canSpeak,
      ) {
        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null)
        Text(stringResource(R.string.read_item), modifier = Modifier.padding(start = 8.dp))
      }
    }
  }
}

@Composable
private fun ResultBadge(band: ConfidenceBand?) {
  val (label, color) = when (band) {
    ConfidenceBand.HIGH ->
      stringResource(band.labelResource()) to MaterialTheme.colorScheme.primaryContainer
    ConfidenceBand.MEDIUM ->
      stringResource(band.labelResource()) to MaterialTheme.colorScheme.secondaryContainer
    ConfidenceBand.LOW ->
      stringResource(band.labelResource()) to MaterialTheme.colorScheme.errorContainer
    null ->
      stringResource(R.string.recognized_text_short) to
        MaterialTheme.colorScheme.secondaryContainer
  }
  Surface(color = color, shape = CircleShape) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
    )
  }
}

@Composable
internal fun RecognizedTextCard(
  text: String,
  onCopy: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.recognized_text),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = onCopy) {
          Icon(
            Icons.Rounded.ContentCopy,
            contentDescription = stringResource(R.string.copy_text),
          )
        }
      }
      Text(text, style = MaterialTheme.typography.bodyLarge)
    }
  }
}

private fun DetectedItem.speechDescription(confidenceLabel: String?): String =
  if (confidence == null) {
    "Recognized text: $label."
  } else {
    "${confidenceLabel.orEmpty()}: $label. " +
      "${(confidence * 100).roundToInt()} percent confidence."
  }
