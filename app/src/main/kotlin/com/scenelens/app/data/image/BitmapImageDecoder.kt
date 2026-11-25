package com.scenelens.app.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.max

class BitmapImageDecoder(
  private val context: Context,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
  suspend fun decode(uri: Uri, targetLongSide: Int = DEFAULT_TARGET_LONG_SIDE): Bitmap =
    withContext(dispatcher) {
      val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      openInput(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
      require(bounds.outWidth > 0 && bounds.outHeight > 0) {
        "The selected file is not a readable image."
      }

      var sampleSize = 1
      while (max(bounds.outWidth, bounds.outHeight) / sampleSize > targetLongSide) {
        sampleSize *= 2
      }

      val decoded = openInput(uri).use { input ->
        BitmapFactory.decodeStream(
          input,
          null,
          BitmapFactory.Options().apply { inSampleSize = sampleSize },
        )
      } ?: error("The image could not be decoded.")

      decoded.withOrientation(readOrientation(uri))
    }

  private fun readOrientation(uri: Uri): Int = openInput(uri).use { input ->
    ExifInterface(input).getAttributeInt(
      ExifInterface.TAG_ORIENTATION,
      ExifInterface.ORIENTATION_NORMAL,
    )
  }

  private fun openInput(uri: Uri): InputStream =
    context.contentResolver.openInputStream(uri)
      ?: error("SceneLens could not open the selected image.")

  private fun Bitmap.withOrientation(orientation: Int): Bitmap {
    val matrix = Matrix().apply {
      when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
          postRotate(90f)
          postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
          postRotate(270f)
          postScale(-1f, 1f)
        }
        else -> return this@withOrientation
      }
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true).also { rotated ->
      if (rotated != this) recycle()
    }
  }

  private companion object {
    const val DEFAULT_TARGET_LONG_SIDE = 1600
  }
}
