package com.scenelens.app.feature.scenelens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scenelens.app.domain.AnalyzeSceneUseCase
import com.scenelens.app.core.ui.R
import com.scenelens.app.core.ui.CameraPermissionRationale
import com.scenelens.app.feature.camera.CameraCaptureScreen

@Composable
fun SceneLensApp(
  analyzeScene: AnalyzeSceneUseCase,
  viewModel: SceneLensViewModel = viewModel(
    factory = SceneLensViewModel.Factory(analyzeScene),
  ),
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showPermissionRationale by remember { mutableStateOf(false) }
  val cameraPermissionDenied = stringResource(R.string.camera_permission_denied)
  val photoPicker = rememberLauncherForActivityResult(
    ActivityResultContracts.PickVisualMedia(),
  ) { uri ->
    uri?.let(viewModel::analyze)
  }
  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { granted ->
    if (granted) viewModel.openCamera() else viewModel.showError(cameraPermissionDenied)
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
        if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
          ) == PackageManager.PERMISSION_GRANTED
        ) {
          viewModel.openCamera()
        } else {
          showPermissionRationale = true
        }
      },
      onChoosePhoto = {
        photoPicker.launch(
          PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
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
