package com.example.sportsxtreme.common

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/** Applies system-bar insets while preserving a view's existing padding. */
object WindowInsetsUtils {
    fun applySystemBarsPadding(view: View, applyBottom: Boolean = true) {
        val initialLeft = view.paddingLeft
        val initialTop = view.paddingTop
        val initialRight = view.paddingRight
        val initialBottom = view.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(view) { target, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            target.updatePadding(
                left = initialLeft + insets.left,
                top = initialTop + insets.top,
                right = initialRight + insets.right,
                bottom = initialBottom + if (applyBottom) insets.bottom else 0
            )
            windowInsets
        }
        ViewCompat.requestApplyInsets(view)
    }
}
