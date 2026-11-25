package com.scenelens.app.core.ui

import androidx.annotation.StringRes
import com.scenelens.app.core.ui.R
import com.scenelens.app.domain.model.ConfidenceBand

@StringRes
fun ConfidenceBand.labelResource(): Int = when (this) {
  ConfidenceBand.HIGH -> R.string.confidence_likely
  ConfidenceBand.MEDIUM -> R.string.confidence_possible
  ConfidenceBand.LOW -> R.string.confidence_uncertain
}
