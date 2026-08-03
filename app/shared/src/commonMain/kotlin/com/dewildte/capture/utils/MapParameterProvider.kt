package com.dewildte.capture.utils

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

abstract class MapParameterProvider<T> : PreviewParameterProvider<T> {

  abstract val valueMap: Map<String, T>

  override val values: Sequence<T>
    get() = valueMap.values.asSequence()

  override fun getDisplayName(index: Int): String? {
    return valueMap.keys.toList().getOrNull(index)
  }
}
