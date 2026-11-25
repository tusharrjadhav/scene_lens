package com.scenelens.app.feature.scenelens

import android.net.Uri
import com.scenelens.app.domain.AnalyzeSceneUseCase
import com.scenelens.app.domain.model.SceneAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SceneLensViewModelTest {
  private val dispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialState_isReadyForAnImage() {
    val viewModel = SceneLensViewModel(FailingAnalyzer)

    assertFalse(viewModel.uiState.value.isAnalyzing)
    assertFalse(viewModel.uiState.value.isCameraOpen)
    assertNull(viewModel.uiState.value.analysis)
  }

  @Test
  fun openCamera_setsCameraStateAndClearsErrors() {
    val viewModel = SceneLensViewModel(FailingAnalyzer)
    viewModel.showError("Camera unavailable")

    viewModel.openCamera()

    assertTrue(viewModel.uiState.value.isCameraOpen)
    assertNull(viewModel.uiState.value.errorMessage)
  }

  @Test
  fun reset_returnsToTheEmptyState() {
    val viewModel = SceneLensViewModel(FailingAnalyzer)
    viewModel.showError("Camera unavailable")
    viewModel.selectItem("object-1")

    viewModel.reset()

    assertNull(viewModel.uiState.value.errorMessage)
    assertNull(viewModel.uiState.value.selectedItemId)
  }
}

private object FailingAnalyzer : AnalyzeSceneUseCase {
  override suspend fun invoke(uri: Uri): SceneAnalysis =
    throw IllegalArgumentException("Unreadable image")
}
