package com.scenelens.app.ui.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scenelens.app.R
import com.scenelens.app.data.ConfidenceBand
import com.scenelens.app.data.DetectedItem
import com.scenelens.app.data.DetectionSource
import com.scenelens.app.data.SceneAnalysis
import com.scenelens.app.data.SceneLensUiState
import com.scenelens.app.ui.theme.SceneLensTheme
import kotlin.math.roundToInt

@Composable
fun SceneLensApp(
  viewModel: SceneLensViewModel = viewModel(
    factory = SceneLensViewModel.Factory(LocalContext.current),
  ),
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showPermissionRationale by remember { mutableStateOf(false) }
  val cameraPermissionDenied = stringResource(R.string.camera_permission_denied)
  val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
    uri?.let { viewModel.analyze(it) }
  }
  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { granted ->
    if (granted) {
      viewModel.openCamera()
    } else {
      viewModel.showError(cameraPermissionDenied)
    }
  }

  if (uiState.isCameraOpen) {
    CameraCaptureScreen(
      onImageCaptured = { viewModel.analyze(it, deleteInputAfter = true) },
      onError = {
        viewModel.closeCamera()
        viewModel.showError(it)
      },
      onClose = viewModel::closeCamera,
    )
  } else {
    SceneLensScreen(
      uiState = uiState,
      onTakePhoto = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
          PackageManager.PERMISSION_GRANTED
        ) {
          viewModel.openCamera()
        } else {
          showPermissionRationale = true
        }
      },
      onChoosePhoto = {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
      },
      onReset = viewModel::reset,
      onSelectItem = viewModel::selectItem,
      onClearError = viewModel::clearError,
    )
  }

  if (showPermissionRationale) {
    CameraPermissionRationale(
      onContinue = {
        showPermissionRationale = false
        permissionLauncher.launch(Manifest.permission.CAMERA)
      },
      onDismiss = { showPermissionRationale = false },
    )
  }
}

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
  val clipboard = LocalClipboardManager.current

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
              Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.new_image))
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
    when {
      uiState.isAnalyzing -> AnalysisProgress(Modifier.padding(padding))
      uiState.analysis != null -> AnalysisResultScreen(
        analysis = uiState.analysis,
        selectedItemId = uiState.selectedItemId,
        canSpeak = speaker.isReady,
        onSpeak = { speaker.speak(it) },
        onStop = { speaker.stop() },
        onCopy = {
          clipboard.setText(AnnotatedString(it))
        },
        onSelectItem = onSelectItem,
        onNewImage = onChoosePhoto,
        modifier = Modifier
          .padding(padding)
          .navigationBarsPadding(),
      )
      else -> StartScreen(
        onTakePhoto = onTakePhoto,
        onChoosePhoto = onChoosePhoto,
        modifier = Modifier
          .padding(padding)
          .navigationBarsPadding(),
      )
    }
  }
}

@Composable
private fun StartScreen(
  onTakePhoto: () -> Unit,
  onChoosePhoto: () -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    item {
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
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
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
              .heightIn(min = 56.dp),
          ) {
            Icon(Icons.Rounded.CameraAlt, contentDescription = null)
            Text(stringResource(R.string.take_photo), modifier = Modifier.padding(start = 10.dp))
          }
          OutlinedButton(
            onClick = onChoosePhoto,
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 56.dp),
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
    item {
      PrivacyNote()
    }
  }
}

@Composable
private fun FeatureRow(title: Int, description: Int) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
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
      modifier = Modifier.padding(16.dp),
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

@Composable
private fun AnalysisProgress(modifier: Modifier = Modifier) {
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

@Composable
private fun AnalysisResultScreen(
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
      Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = analysis.summary,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.semantics { heading() },
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          ElevatedButton(
            onClick = { onSpeak(analysis.summary) },
            enabled = canSpeak,
            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
          ) {
            Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null)
            Text(stringResource(R.string.read_aloud), modifier = Modifier.padding(start = 8.dp))
          }
          OutlinedButton(
            onClick = onStop,
            modifier = Modifier.heightIn(min = 52.dp),
          ) {
            Icon(Icons.Rounded.Stop, contentDescription = stringResource(R.string.stop_speech))
          }
        }
      }
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
        modifier = Modifier.padding(horizontal = 20.dp).semantics { heading() },
      )
    }
    item {
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
            FilterChip(
              selected = selectedItemId == item.id,
              onClick = { onSelectItem(item.id) },
              label = { Text(item.label) },
              modifier = Modifier
                .heightIn(min = 48.dp)
                .semantics {
                  stateDescription = item.confidenceBand?.label
                    ?: item.source.name.replace('_', ' ').lowercase()
                },
            )
          }
        }
      }
    }
    item {
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
        analysis.details.forEach { detail ->
          Text(
            text = "• $detail",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
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
          .heightIn(min = 56.dp)
          .padding(horizontal = 20.dp),
      ) {
        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
        Text(stringResource(R.string.choose_another), modifier = Modifier.padding(start = 8.dp))
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
private fun AnalyzedImage(
  analysis: SceneAnalysis,
  selectedItemId: String?,
  modifier: Modifier = Modifier,
) {
  val image = analysis.image.asImageBitmap()
  val aspectRatio = analysis.image.width.toFloat() / analysis.image.height.toFloat()
  Box(
    modifier = modifier
      .aspectRatio(aspectRatio)
      .clip(RoundedCornerShape(20.dp))
      .background(MaterialTheme.colorScheme.surfaceContainerLow),
  ) {
    Image(
      bitmap = image,
      contentDescription = stringResource(R.string.selected_image_description),
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Fit,
    )
    BoundingBoxOverlay(
      items = analysis.items,
      selectedItemId = selectedItemId,
      modifier = Modifier.fillMaxSize(),
    )
  }
}

@Composable
private fun BoundingBoxOverlay(
  items: List<DetectedItem>,
  selectedItemId: String?,
  modifier: Modifier = Modifier,
) {
  val primary = MaterialTheme.colorScheme.primary
  val tertiary = MaterialTheme.colorScheme.tertiary
  Canvas(modifier = modifier) {
    items
      .filter { it.source != DetectionSource.IMAGE_LABEL }
      .forEach { item ->
        val selected = item.id == selectedItemId
        drawRoundRect(
          color = if (selected) tertiary else primary,
          topLeft = Offset(item.bounds.left * size.width, item.bounds.top * size.height),
          size = Size(
            (item.bounds.right - item.bounds.left) * size.width,
            (item.bounds.bottom - item.bounds.top) * size.height,
          ),
          cornerRadius = CornerRadius(10.dp.toPx()),
          style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = if (selected) 6.dp.toPx() else 3.dp.toPx(),
          ),
        )
      }
  }
}

@Composable
private fun SelectedItemCard(
  item: DetectedItem,
  canSpeak: Boolean,
  onSpeak: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val prompt = if (item.confidence == null) {
    "Recognized text: ${item.label}."
  } else {
    "${item.confidenceBand?.label.orEmpty()}: ${item.label}. " +
      "${(item.confidence * 100).roundToInt()} percent confidence."
  }
  ElevatedCard(modifier = modifier.fillMaxWidth()) {
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
          item.label,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
        if (item.confidenceBand == null) {
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = CircleShape,
          ) {
            Text(
              stringResource(R.string.recognized_text_short),
              style = MaterialTheme.typography.labelLarge,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
          }
        } else {
          ConfidenceBadge(item.confidenceBand)
        }
      }
      Text(
        text = when (item.confidenceBand) {
          ConfidenceBand.HIGH -> stringResource(R.string.confidence_high)
          ConfidenceBand.MEDIUM -> stringResource(R.string.confidence_medium)
          ConfidenceBand.LOW -> stringResource(R.string.confidence_low)
          null -> stringResource(R.string.ocr_no_confidence)
        },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      TextButton(
        onClick = { onSpeak(prompt) },
        enabled = canSpeak,
      ) {
        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null)
        Text(stringResource(R.string.read_item), modifier = Modifier.padding(start = 8.dp))
      }
    }
  }
}

@Composable
private fun ConfidenceBadge(band: ConfidenceBand) {
  val color = when (band) {
    ConfidenceBand.HIGH -> MaterialTheme.colorScheme.primaryContainer
    ConfidenceBand.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
    ConfidenceBand.LOW -> MaterialTheme.colorScheme.errorContainer
  }
  Surface(color = color, shape = CircleShape) {
    Text(
      text = band.label,
      style = MaterialTheme.typography.labelLarge,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
    )
  }
}

@Composable
private fun RecognizedTextCard(
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
          Icon(Icons.Rounded.ContentCopy, contentDescription = stringResource(R.string.copy_text))
        }
      }
      Text(text, style = MaterialTheme.typography.bodyLarge)
    }
  }
}

@Composable
private fun CameraPermissionRationale(
  onContinue: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = { Icon(Icons.Rounded.CameraAlt, contentDescription = null) },
    title = { Text(stringResource(R.string.camera_rationale_title)) },
    text = { Text(stringResource(R.string.camera_rationale_body)) },
    confirmButton = {
      Button(onClick = onContinue) {
        Text(stringResource(R.string.continue_label))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.not_now))
      }
    },
  )
}

@Preview(name = "Phone", showBackground = true, showSystemUi = true)
@Composable
private fun SceneLensHomePreview() {
  SceneLensTheme {
    StartScreen(onTakePhoto = {}, onChoosePhoto = {})
  }
}
