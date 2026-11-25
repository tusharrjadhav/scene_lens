package com.scenelens.app.ui.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.scenelens.app.data.OnDeviceSceneAnalyzer
import com.scenelens.app.data.SceneAnalyzer
import com.scenelens.app.data.SceneLensUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class SceneLensViewModel(
  private val analyzer: SceneAnalyzer,
) : ViewModel() {
  private val _uiState = MutableStateFlow(SceneLensUiState())
  val uiState: StateFlow<SceneLensUiState> = _uiState.asStateFlow()
  private var analysisJob: Job? = null

  fun openCamera() {
    _uiState.update { it.copy(isCameraOpen = true, errorMessage = null) }
  }

  fun closeCamera() {
    _uiState.update { it.copy(isCameraOpen = false) }
  }

  fun analyze(uri: Uri, deleteInputAfter: Boolean = false) {
    analysisJob?.cancel()
    analysisJob = viewModelScope.launch {
      _uiState.update {
        it.copy(
          isAnalyzing = true,
          isCameraOpen = false,
          analysis = null,
          selectedItemId = null,
          errorMessage = null,
        )
      }
      try {
        val result = analyzer.analyze(uri)
        _uiState.update { it.copy(isAnalyzing = false, analysis = result) }
      } catch (cancellation: CancellationException) {
        throw cancellation
      } catch (error: Exception) {
        _uiState.update {
          it.copy(
            isAnalyzing = false,
            errorMessage = error.message ?: "SceneLens could not analyze this image.",
          )
        }
      } finally {
        if (deleteInputAfter && uri.scheme == "file") {
          uri.path?.let(::File)?.delete()
        }
      }
    }
  }

  fun selectItem(itemId: String) {
    _uiState.update { it.copy(selectedItemId = itemId) }
  }

  fun reset() {
    analysisJob?.cancel()
    _uiState.update { SceneLensUiState() }
  }

  fun showError(message: String) {
    _uiState.update { it.copy(errorMessage = message) }
  }

  fun clearError() {
    _uiState.update { it.copy(errorMessage = null) }
  }

  class Factory(context: Context) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(SceneLensViewModel::class.java)) {
        return SceneLensViewModel(OnDeviceSceneAnalyzer(appContext)) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}
