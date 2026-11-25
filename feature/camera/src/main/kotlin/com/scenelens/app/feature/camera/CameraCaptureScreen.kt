package com.scenelens.app.feature.camera

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.scenelens.app.core.ui.R
import com.scenelens.app.core.ui.SceneLensUiTokens

@Composable
fun CameraCaptureScreen(
  onImageCaptured: (Uri) -> Unit,
  onError: (String) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val previewDescription = stringResource(R.string.camera_preview_description)
  val controller = remember {
    LifecycleCameraController(context).apply {
      cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
      setEnabledUseCases(CameraController.IMAGE_CAPTURE)
      imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
    }
  }
  val photoCapture = remember(controller) { CameraPhotoCapture(context, controller) }
  var isCapturing by remember { mutableStateOf(false) }

  BackHandler(onBack = onClose)
  DisposableEffect(controller, lifecycleOwner) {
    controller.bindToLifecycle(lifecycleOwner)
    onDispose { controller.unbind() }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.scrim),
  ) {
    AndroidView(
      factory = {
        PreviewView(context).apply {
          this.controller = controller
          implementationMode = PreviewView.ImplementationMode.COMPATIBLE
          scaleType = PreviewView.ScaleType.FIT_CENTER
        }
      },
      modifier = Modifier
        .fillMaxSize()
        .semantics { contentDescription = previewDescription },
    )
    CameraHeader(onClose = onClose, modifier = Modifier.align(Alignment.TopStart))
    CaptureControls(
      isCapturing = isCapturing,
      onCapture = {
        photoCapture.capture(
          onStarted = { isCapturing = true },
          onFinished = { isCapturing = false },
          onSuccess = onImageCaptured,
          onError = onError,
        )
      },
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}

@Composable
private fun CameraHeader(
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f))
      .padding(horizontal = 12.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    FilledIconButton(
      onClick = onClose,
      colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
      ),
    ) {
      Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close_camera))
    }
    Column(modifier = Modifier.padding(start = 12.dp)) {
      Text(
        text = stringResource(R.string.camera_title),
        color = MaterialTheme.colorScheme.inverseOnSurface,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = stringResource(R.string.camera_hint),
        color = MaterialTheme.colorScheme.inverseOnSurface,
        style = MaterialTheme.typography.bodySmall,
      )
    }
  }
}

@Composable
private fun CaptureControls(
  isCapturing: Boolean,
  onCapture: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f))
      .padding(28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    FilledIconButton(
      onClick = onCapture,
      enabled = !isCapturing,
      modifier = Modifier
        .size(SceneLensUiTokens.CaptureControlSize)
        .clip(CircleShape),
      colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
      ),
    ) {
      Icon(
        Icons.Rounded.PhotoCamera,
        contentDescription = stringResource(R.string.capture_photo),
        modifier = Modifier.size(36.dp),
      )
    }
    Text(
      text = stringResource(if (isCapturing) R.string.capturing else R.string.capture_photo),
      color = MaterialTheme.colorScheme.inverseOnSurface,
    )
  }
}
