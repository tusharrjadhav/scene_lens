package com.scenelens.app.feature.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.scenelens.app.R
import java.io.File

class CameraPhotoCapture(
  private val context: Context,
  private val controller: LifecycleCameraController,
) {
  fun capture(
    onStarted: () -> Unit,
    onFinished: () -> Unit,
    onSuccess: (Uri) -> Unit,
    onError: (String) -> Unit,
  ) {
    val photo = runCatching {
      File.createTempFile("scenelens-", ".jpg", context.cacheDir)
    }.getOrElse { error ->
      onError(error.message ?: context.getString(R.string.capture_failed))
      return
    }
    onStarted()
    try {
      controller.takePicture(
        ImageCapture.OutputFileOptions.Builder(photo).build(),
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
          override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            onFinished()
            onSuccess(Uri.fromFile(photo))
          }

          override fun onError(exception: ImageCaptureException) {
            photo.delete()
            onFinished()
            onError(exception.message ?: context.getString(R.string.capture_failed))
          }
        },
      )
    } catch (error: Exception) {
      photo.delete()
      onFinished()
      onError(error.message ?: context.getString(R.string.capture_failed))
    }
  }
}
