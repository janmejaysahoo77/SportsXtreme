package com.example.sportsxtreme.common

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object WindowInsetsUtils {
    fun applySystemBarsPadding(view: View, applyTop: Boolean = true, applyBottom: Boolean = true) {
        val initialPaddingLeft = view.paddingLeft
        val initialPaddingTop = view.paddingTop
        val initialPaddingRight = view.paddingRight
        val initialPaddingBottom = view.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                initialPaddingLeft + systemBars.left,
                initialPaddingTop + if (applyTop) systemBars.top else 0,
                initialPaddingRight + systemBars.right,
                initialPaddingBottom + if (applyBottom) systemBars.bottom else 0
            )
            insets
        }
    }
}
