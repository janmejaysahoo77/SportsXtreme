package com.example.sportsxtreme.presentation.auth

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView

class VerificationCompleteScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    init {
        setBackgroundColor(Color.BLACK)
        addView(TextView(context).apply {
            text = "Verification complete"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 30f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
        }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        postDelayed({
            (context as? MainActivity)?.showHomeScreen()
        }, COMPLETE_SCREEN_DURATION_MS)
    }

    private companion object {
        const val COMPLETE_SCREEN_DURATION_MS = 2000L
    }
}
