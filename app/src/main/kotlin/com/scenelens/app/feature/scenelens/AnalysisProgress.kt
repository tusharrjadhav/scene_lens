package com.scenelens.app.feature.scenelens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.scenelens.app.R

@Composable
internal fun AnalysisProgress(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .semantics { liveRegion = LiveRegionMode.Polite },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    CircularProgressIndicator(modifier = Modifier.size(56.dp))
    Text(
      text = stringResource(R.string.analyzing),
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.padding(top = 16.dp),
    )
    Text(
      text = stringResource(R.string.analyzing_private),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 4.dp),
    )
  }
}
