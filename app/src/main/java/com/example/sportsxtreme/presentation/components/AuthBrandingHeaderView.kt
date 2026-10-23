package com.example.sportsxtreme.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.sportsxtreme.R
import kotlin.math.min

/** Shared signup and phone-verification branding. */
class AuthBrandingHeaderView(context: Context) : LinearLayout(context) {
    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        addView(LogoMarkView(context), LayoutParams(dp(98), dp(58)).apply {
            bottomMargin = dp(4)
        })
        addView(BrandWordmarkView(context), LayoutParams(dp(210), dp(28)))
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()

    private class LogoMarkView(context: Context) : View(context) {
        private val appIcon: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.appicon)
        private val source = Rect()
        private val bounds = RectF()
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private var logoShader: Shader? = null

        init {
            appIcon?.let { bitmap ->
                source.set(
                    (bitmap.width * 0.2f).toInt(),
                    (bitmap.height * 0.34f).toInt(),
                    (bitmap.width * 0.84f).toInt(),
                    (bitmap.height * 0.62f).toInt()
                )
            }
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            if (w > 0 && h > 0) {
                val cx = w / 2f
                val cy = h / 2f
                val density = resources.displayMetrics.density
                logoShader = LinearGradient(
                    cx - 52f * density, cy, cx + 52f * density, cy,
                    intArrayOf(Color.TRANSPARENT, Color.argb(46, 193, 255, 0), Color.argb(88, 193, 255, 0), Color.argb(46, 193, 255, 0), Color.TRANSPARENT),
                    floatArrayOf(0f, 0.24f, 0.5f, 0.76f, 1f), Shader.TileMode.CLAMP
                )
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val density = resources.displayMetrics.density
            val cx = width / 2f
            val cy = height / 2f
            paint.style = Paint.Style.FILL
            paint.shader = logoShader
            bounds.set(cx - 52f * density, cy - 6f * density, cx + 52f * density, cy + 6f * density)
            canvas.drawRoundRect(bounds, 6f * density, 6f * density, paint)
            paint.shader = null
            paint.color = Color.argb(172, 193, 255, 0)
            bounds.set(cx - 34f * density, cy + 20f * density, cx + 34f * density, cy + 22f * density)
            canvas.drawRoundRect(bounds, 1f * density, 1f * density, paint)

            val bitmap = appIcon ?: return
            if (source.isEmpty) return
            val iconWidth = min(width * 0.86f, 86f * density)
            val iconHeight = iconWidth * source.height() / source.width()
            bounds.set(cx - iconWidth / 2f, cy - iconHeight / 2f, cx + iconWidth / 2f, cy + iconHeight / 2f)
            canvas.drawBitmap(bitmap, source, bounds, bitmapPaint)
        }
    }

    private class BrandWordmarkView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            paint.textSize = min(width / 11.2f, 18f * resources.displayMetrics.density)
            paint.letterSpacing = 0.03f
            val sports = "SPORTS"
            val x = "X"
            val treme = "TREME"
            val total = paint.measureText(sports) + paint.measureText(x) + paint.measureText(treme) + 2f
            var left = width / 2f - total / 2f
            val baseline = height * 0.68f
            paint.color = Color.rgb(236, 243, 238)
            canvas.drawText(sports, left, baseline, paint)
            left += paint.measureText(sports) + 1f
            paint.color = Color.rgb(0, 127, 255)
            canvas.drawText(x, left, baseline, paint)
            left += paint.measureText(x) * 0.93f
            canvas.drawText(treme, left, baseline, paint)
        }
    }
}
