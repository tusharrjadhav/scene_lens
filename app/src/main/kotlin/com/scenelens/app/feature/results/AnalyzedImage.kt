package com.scenelens.app.feature.results

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scenelens.app.R
import com.scenelens.app.domain.model.DetectedItem
import com.scenelens.app.domain.model.DetectionSource
import com.scenelens.app.domain.model.SceneAnalysis

@Composable
internal fun AnalyzedImage(
  analysis: SceneAnalysis,
  selectedItemId: String?,
  modifier: Modifier = Modifier,
) {
  val aspectRatio = analysis.image.width.toFloat() / analysis.image.height.toFloat()
  Box(
    modifier = modifier
      .aspectRatio(aspectRatio)
      .clip(RoundedCornerShape(20.dp))
      .background(MaterialTheme.colorScheme.surfaceContainerLow),
  ) {
    Image(
      bitmap = analysis.image.asImageBitmap(),
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
  val selected = MaterialTheme.colorScheme.tertiary
  Canvas(modifier = modifier) {
    items
      .filter { it.source != DetectionSource.IMAGE_LABEL }
      .forEach { item ->
        val isSelected = item.id == selectedItemId
        drawRoundRect(
          color = if (isSelected) selected else primary,
          topLeft = Offset(
            x = item.bounds.left * size.width,
            y = item.bounds.top * size.height,
          ),
          size = Size(
            width = (item.bounds.right - item.bounds.left) * size.width,
            height = (item.bounds.bottom - item.bounds.top) * size.height,
          ),
          cornerRadius = CornerRadius(10.dp.toPx()),
          style = Stroke(width = if (isSelected) 6.dp.toPx() else 3.dp.toPx()),
        )
      }
  }
}
