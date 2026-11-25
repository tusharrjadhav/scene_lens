package com.scenelens.app.feature.scenelens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.scenelens.app.core.ui.R
import com.scenelens.app.ui.theme.SceneLensTheme
import org.junit.Rule
import org.junit.Test

class SceneLensScreenTest {
  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun emptyState_exposesCameraAndGalleryActions() {
    composeTestRule.setContent {
      SceneLensTheme {
        SceneLensScreen(uiState = SceneLensUiState())
      }
    }

    composeTestRule
      .onNodeWithText(composeTestRule.activity.getString(R.string.take_photo))
      .assertIsDisplayed()
    composeTestRule
      .onNodeWithText(composeTestRule.activity.getString(R.string.choose_photo))
      .assertIsDisplayed()
  }
}
