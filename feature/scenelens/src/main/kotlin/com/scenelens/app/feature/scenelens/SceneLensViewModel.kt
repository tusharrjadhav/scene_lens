package com.scenelens.app.feature.scenelens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.scenelens.app.domain.AnalyzeSceneUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class SceneLensViewModel(
  private val analyzeScene: AnalyzeSceneUseCase,
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
        val result = analyzeScene(uri)
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

  class Factory(
    private val analyzeScene: AnalyzeSceneUseCase,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      require(modelClass.isAssignableFrom(SceneLensViewModel::class.java)) {
        "Unknown ViewModel class: ${modelClass.name}"
      }
      return SceneLensViewModel(analyzeScene) as T
    }
  }
}
