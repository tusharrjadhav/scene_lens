package com.scenelens.app.feature.home

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.scenelens.app.R
import com.scenelens.app.core.ui.SceneLensUiTokens
import com.scenelens.app.ui.theme.SceneLensTheme

@Composable
internal fun HomeScreen(
  onTakePhoto: () -> Unit,
  onChoosePhoto: () -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(
      horizontal = SceneLensUiTokens.ScreenPadding,
      vertical = 16.dp,
    ),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    item { Intro() }
    item {
      StartImageCard(
        onTakePhoto = onTakePhoto,
        onChoosePhoto = onChoosePhoto,
      )
    }
    item {
      Text(
        text = stringResource(R.string.what_it_finds),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics { heading() },
      )
    }
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FeatureRow(R.string.objects_title, R.string.objects_description)
        FeatureRow(R.string.reading_title, R.string.reading_description)
        FeatureRow(R.string.confidence_title, R.string.confidence_description)
      }
    }
    item { PrivacyNote() }
  }
}

@Composable
private fun Intro() {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    AssistChip(
      onClick = {},
      enabled = false,
      label = { Text(stringResource(R.string.offline_badge)) },
      leadingIcon = { Icon(Icons.Rounded.Security, contentDescription = null) },
      colors = AssistChipDefaults.assistChipColors(
        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        disabledLeadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
      ),
    )
    Text(
      text = stringResource(R.string.home_title),
      style = MaterialTheme.typography.displaySmall,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.semantics { heading() },
    )
    Text(
      text = stringResource(R.string.home_subtitle),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun StartImageCard(
  onTakePhoto: () -> Unit,
  onChoosePhoto: () -> Unit,
) {
  ElevatedCard(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
  ) {
    Column(
      modifier = Modifier.padding(SceneLensUiTokens.ScreenPadding),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(
        Icons.Rounded.AutoAwesome,
        contentDescription = null,
        modifier = Modifier.size(32.dp),
      )
      Text(
        text = stringResource(R.string.home_prompt),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
      )
      Button(
        onClick = onTakePhoto,
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = SceneLensUiTokens.PrimaryActionHeight),
      ) {
        Icon(Icons.Rounded.CameraAlt, contentDescription = null)
        Text(stringResource(R.string.take_photo), modifier = Modifier.padding(start = 10.dp))
      }
      OutlinedButton(
        onClick = onChoosePhoto,
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = SceneLensUiTokens.PrimaryActionHeight),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer),
      ) {
        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
        Text(stringResource(R.string.choose_photo), modifier = Modifier.padding(start = 10.dp))
      }
    }
  }
}

@Composable
private fun FeatureRow(
  @StringRes title: Int,
  @StringRes description: Int,
) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(SceneLensUiTokens.CardPadding),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.Top,
    ) {
      Icon(
        Icons.Rounded.CheckCircle,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
      )
      Column {
        Text(stringResource(title), fontWeight = FontWeight.SemiBold)
        Text(
          stringResource(description),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun PrivacyNote() {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
  ) {
    Row(
      modifier = Modifier.padding(SceneLensUiTokens.CardPadding),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(Icons.Rounded.Security, contentDescription = null)
      Column {
        Text(stringResource(R.string.privacy_title), fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.privacy_body), style = MaterialTheme.typography.bodyMedium)
      }
    }
  }
}

@Preview(name = "Phone", showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
  SceneLensTheme {
    HomeScreen(onTakePhoto = {}, onChoosePhoto = {})
  }
}
