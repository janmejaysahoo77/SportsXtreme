package com.example.sportsxtreme.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.example.sportsxtreme.R
import kotlin.math.max

class AuthBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bgTexture: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.black)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val rect = RectF()
    private var startTimeMs = android.os.SystemClock.uptimeMillis()
    private var bgShader: Shader? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (h > 0) {
            bgShader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(Color.argb(232, 0, 0, 0), Color.argb(218, 0, 5, 4), Color.BLACK),
                floatArrayOf(0f, 0.56f, 1f),
                Shader.TileMode.CLAMP
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startTimeMs = android.os.SystemClock.uptimeMillis()
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val density = resources.displayMetrics.density
        val time = (android.os.SystemClock.uptimeMillis() - startTimeMs) / 1000f

        canvas.drawColor(Color.BLACK)
        bgTexture?.let { bitmap ->
            val scale = max(w / bitmap.width, h / bitmap.height)
            val bw = bitmap.width * scale
            val bh = bitmap.height * scale
            rect.set((w - bw) / 2f, (h - bh) / 2f, (w + bw) / 2f, (h + bh) / 2f)
            bitmapPaint.alpha = 104
            canvas.drawBitmap(bitmap, null, rect, bitmapPaint)
            bitmapPaint.alpha = 255
        }

        paint.shader = bgShader
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.75f * density
        paint.color = Color.argb(30, 193, 255, 0)
        val grid = 34f * density
        var x = (time * 7f * density) % grid - grid
        while (x < w + grid) {
            canvas.drawLine(x, 0f, x - w * 0.1f, h, paint)
            x += grid
        }
        var y = (time * 5f * density) % grid - grid
        while (y < h + grid) {
            canvas.drawLine(0f, y, w, y + h * 0.06f, paint)
            y += grid
        }
        paint.color = Color.argb(34, 0, 127, 255)
        canvas.drawLine(-w * 0.1f, h * 0.53f, w * 0.98f, h * 0.18f, paint)
        paint.color = Color.argb(42, 193, 255, 0)
        canvas.drawLine(w * 0.12f, h * 0.96f, w * 1.08f, h * 0.66f, paint)
        paint.style = Paint.Style.FILL

        postInvalidateOnAnimation()
    }
}
