package com.scenelens.app.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.scenelens.app.R

@Composable
fun CameraPermissionRationale(
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
