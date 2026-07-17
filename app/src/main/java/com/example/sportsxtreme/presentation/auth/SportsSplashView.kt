package com.example.sportsxtreme.presentation.auth

import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.tournament.*
import com.example.sportsxtreme.presentation.components.*
import com.example.sportsxtreme.presentation.auth.*
import com.example.sportsxtreme.presentation.scoring.*
import com.example.sportsxtreme.presentation.match.*
import com.example.sportsxtreme.presentation.media.*
import com.example.sportsxtreme.presentation.home.*
import com.example.sportsxtreme.presentation.team.*
import com.example.sportsxtreme.presentation.profile.*
import com.example.sportsxtreme.presentation.store.*
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import kotlin.math.min
import kotlin.math.sin

class SportsSplashView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val graphite = Color.rgb(3, 6, 15)
    private val inkBlue = Color.rgb(6, 16, 42)
    private val electricBlue = Color.rgb(22, 72, 230)
    private val energyGreen = Color.rgb(190, 255, 20)

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val logoGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val erasePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val logoImagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val sportIconPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val logoSource = Rect()
    private val logoBounds = RectF()
    private val iconBounds = RectF()
    private val imageLogo: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.logosss)
    private val sportLogos: List<Bitmap?> = listOf(
        BitmapFactory.decodeResource(resources, R.drawable.cricketlogo),
        BitmapFactory.decodeResource(resources, R.drawable.footballlogo),
        BitmapFactory.decodeResource(resources, R.drawable.badmintonlogo),
        BitmapFactory.decodeResource(resources, R.drawable.kabaddilogo)
    )
    private var backgroundGradient: LinearGradient? = null
    private var blueGlowGradient: RadialGradient? = null
    private var greenGlowGradient: RadialGradient? = null
    private var logoGlowGradient: RadialGradient? = null
    private var iconGlowGradient: RadialGradient? = null
    private var animatorSet: AnimatorSet? = null

    // Animation Properties
    private var logoScale = 0f
    private var textAlpha = 0
    private var textTranslateY = 80f
    private var subtitleAlpha = 0
    private var iconsAlpha = 0
    private var iconsMotionProgress = 0f
    private var versionAlpha = 0

    init {
        logoPaint.style = Paint.Style.FILL
        erasePaint.color = Color.rgb(5, 15, 32)
        erasePaint.style = Paint.Style.FILL

        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.strokeJoin = Paint.Join.ROUND

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = true
        textPaint.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)

        titlePaint.textAlign = Paint.Align.LEFT
        titlePaint.isFakeBoldText = true
        titlePaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)

        subTextPaint.textAlign = Paint.Align.CENTER
        subTextPaint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        subTextPaint.letterSpacing = 0.12f

        iconPaint.color = Color.argb(215, 248, 252, 255)
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeCap = Paint.Cap.ROUND
        iconPaint.strokeJoin = Paint.Join.ROUND

        sportIconPaint.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        sportIconPaint.alpha = 0 // Will be animated

        imageLogo?.let { bitmap ->
            logoSource.set(
                (bitmap.width * 0.18f).toInt(),
                (bitmap.height * 0.29f).toInt(),
                (bitmap.width * 0.84f).toInt(),
                (bitmap.height * 0.53f).toInt()
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimations()
    }

    override fun onDetachedFromWindow() {
        animatorSet?.cancel()
        animatorSet = null
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val width = w.toFloat()
        val height = h.toFloat()
        val cx = width / 2f
        val cy = height / 2f

        backgroundGradient = LinearGradient(
            0f,
            0f,
            width,
            height,
            intArrayOf(Color.rgb(0, 3, 6), inkBlue, Color.rgb(0, 1, 4)),
            floatArrayOf(0f, 0.54f, 1f),
            Shader.TileMode.CLAMP
        )
        blueGlowGradient = RadialGradient(
            cx + width * 0.18f,
            cy - height * 0.22f,
            height * 0.55f,
            intArrayOf(Color.argb(48, 13, 54, 128), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        greenGlowGradient = RadialGradient(
            cx - width * 0.22f,
            cy + height * 0.2f,
            height * 0.38f,
            intArrayOf(Color.argb(24, 37, 255, 55), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        logoGlowGradient = RadialGradient(
            cx,
            height * 0.42f,
            min(width, height) * 0.36f,
            intArrayOf(
                Color.argb(118, 0, 126, 255),
                Color.argb(42, 0, 126, 255),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP
        )
        iconGlowGradient = RadialGradient(
            0f,
            0f,
            1f,
            intArrayOf(
                Color.argb(92, 190, 255, 20),
                Color.argb(34, 190, 255, 20),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.46f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    private fun startAnimations() {
        if (animatorSet?.isStarted == true) return

        val logoAnim = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            interpolator = OvershootInterpolator(1.5f)
            addUpdateListener {
                logoScale = it.animatedValue as Float
                postInvalidateOnAnimation()
            }
        }

        val textAnim = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800
            startDelay = 600
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                val fraction = it.animatedValue as Float
                textAlpha = (fraction * 255).toInt()
                textTranslateY = 80f * (1f - fraction)
                postInvalidateOnAnimation()
            }
        }

        val subtitleAnim = ValueAnimator.ofInt(0, 175).apply {
            duration = 800
            startDelay = 1000
            addUpdateListener {
                subtitleAlpha = it.animatedValue as Int
                postInvalidateOnAnimation()
            }
        }

        val bottomAnim = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800
            startDelay = 1400
            addUpdateListener {
                val fraction = it.animatedValue as Float
                iconsAlpha = (fraction * 235).toInt()
                iconsMotionProgress = fraction
                versionAlpha = (fraction * 165).toInt()
                postInvalidateOnAnimation()
            }
        }

        animatorSet = AnimatorSet().apply {
            playTogether(logoAnim, textAnim, subtitleAnim, bottomAnim)
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f
        val unit = min(w, h)

        drawBackground(canvas, w, h, cx, cy)
        
        if (logoScale > 0) {
            drawLogo(canvas, cx, h * 0.42f, unit)
        }
        
        if (textAlpha > 0 || subtitleAlpha > 0) {
            drawBrand(canvas, cx, h, unit)
        }
        
        if (iconsAlpha > 0) {
            drawBottomIcons(canvas, w, h, unit)
        }
        
        if (versionAlpha > 0) {
            drawVersion(canvas, cx, h, unit)
        }
    }

    private fun drawBackground(canvas: Canvas, w: Float, h: Float, cx: Float, cy: Float) {
        bgPaint.shader = backgroundGradient
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        bgPaint.shader = blueGlowGradient
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        bgPaint.shader = greenGlowGradient
        canvas.drawRect(0f, 0f, w, h, bgPaint)
        bgPaint.shader = null

        strokePaint.strokeWidth = 0.9f
        strokePaint.color = Color.argb(52, 37, 255, 55)
        canvas.drawLine(-w * 0.03f, h * 0.46f, w * 1.0f, h * 0.0f, strokePaint)
        canvas.drawLine(w * 0.17f, h * 0.93f, w * 1.02f, h * 0.54f, strokePaint)
    }

    private fun drawLogo(canvas: Canvas, cx: Float, cy: Float, unit: Float) {
        val bitmap = imageLogo ?: return

        // Apply scale to dimensions
        val logoWidth = unit * 0.58f * logoScale
        val logoHeight = (unit * 0.58f * logoSource.height() / logoSource.width()) * logoScale
        
        logoBounds.set(
            cx - logoWidth / 2f,
            cy - logoHeight / 2f,
            cx + logoWidth / 2f,
            cy + logoHeight / 2f
        )
        
        // Slightly fade in the logo as it scales up to avoid popping
        logoImagePaint.alpha = (logoScale.coerceIn(0f, 1f) * 255).toInt()

        drawLogoGlow(canvas, cx, cy, unit)
        
        canvas.drawBitmap(bitmap, logoSource, logoBounds, logoImagePaint)
    }

    private fun drawLogoGlow(canvas: Canvas, cx: Float, cy: Float, unit: Float) {
        val glowProgress = logoScale.coerceIn(0f, 1f)
        val glowRadius = unit * 0.36f

        logoGlowPaint.shader = logoGlowGradient
        logoGlowPaint.alpha = (glowProgress * 150).toInt()

        canvas.save()
        canvas.scale(0.82f + glowProgress * 0.18f, 0.72f + glowProgress * 0.28f, cx, cy)
        canvas.drawRect(
            cx - glowRadius,
            cy - glowRadius,
            cx + glowRadius,
            cy + glowRadius,
            logoGlowPaint
        )
        canvas.restore()

        logoGlowPaint.shader = null
    }

    private fun drawBrand(canvas: Canvas, cx: Float, h: Float, unit: Float) {
        if (textAlpha > 0) {
            drawStyledTitle(canvas, cx, h * 0.55f + textTranslateY, unit)
        }

        if (subtitleAlpha > 0) {
            subTextPaint.textSize = unit * 0.022f
            subTextPaint.color = Color.argb(subtitleAlpha, 190, 255, 20)
            subTextPaint.letterSpacing = 0.18f
            canvas.drawText("EVERY GAME, EVERY GLORY", cx, h * 0.585f + textTranslateY, subTextPaint)
        }
    }

    private fun drawStyledTitle(canvas: Canvas, cx: Float, baseline: Float, unit: Float) {
        titlePaint.textSize = unit * 0.078f
        titlePaint.letterSpacing = 0.01f
        // Only show shadow if text is mostly visible to prevent weird artifacts
        if (textAlpha > 128) {
            val shadowAlpha = ((textAlpha - 128) / 127f * 135).toInt()
            titlePaint.setShadowLayer(5f, 0f, 0f, Color.argb(shadowAlpha, 0, 120, 255))
        } else {
            titlePaint.clearShadowLayer()
        }

        val sports = "SPORTS "
        val x = "X"
        val treme = "TREME"
        val sportsWidth = titlePaint.measureText(sports)
        val xWidth = titlePaint.measureText(x)
        val tremeWidth = titlePaint.measureText(treme)
        var left = cx - (sportsWidth + xWidth + tremeWidth) / 2f

        titlePaint.color = Color.argb(textAlpha, 232, 241, 246)
        canvas.drawText(sports, left, baseline, titlePaint)
        left += sportsWidth

        titlePaint.textSize = unit * 0.096f
        titlePaint.color = Color.argb(textAlpha, 0, 113, 255)
        canvas.drawText(x, left, baseline, titlePaint)
        left += titlePaint.measureText(x) * 0.93f

        titlePaint.textSize = unit * 0.078f
        titlePaint.color = Color.argb(textAlpha, 0, 127, 255)
        canvas.drawText(treme, left, baseline, titlePaint)
        titlePaint.clearShadowLayer()
    }

    private fun drawBottomIcons(canvas: Canvas, w: Float, h: Float, unit: Float) {
        val y = h * 0.67f
        val size = unit * 0.095f
        val glowRadius = unit * 0.085f
        val xs = floatArrayOf(w * 0.18f, w * 0.39f, w * 0.61f, w * 0.82f)

        iconGlowPaint.shader = iconGlowGradient
        iconGlowPaint.alpha = (iconsAlpha * 0.72f).toInt()
        sportIconPaint.alpha = iconsAlpha

        sportLogos.forEachIndexed { index, bitmap ->
            bitmap ?: return@forEachIndexed
            val iconProgress = ((iconsMotionProgress - index * 0.08f) / 0.76f).coerceIn(0f, 1f)
            val easedProgress = 1f - (1f - iconProgress) * (1f - iconProgress)
            val floatOffset = sin((iconsMotionProgress * 2.4f + index * 0.42f) * Math.PI).toFloat()
            val iconY = y + (1f - easedProgress) * unit * 0.045f + floatOffset * unit * 0.008f
            val iconScale = 0.86f + easedProgress * 0.14f + floatOffset * 0.025f

            drawIconGlow(canvas, xs[index], iconY, glowRadius * (0.92f + easedProgress * 0.14f))

            val aspect = bitmap.width.toFloat() / bitmap.height.toFloat()
            val iconWidth = (if (aspect >= 1f) size else size * aspect) * iconScale
            val iconHeight = (if (aspect >= 1f) size / aspect else size) * iconScale
            iconBounds.set(
                xs[index] - iconWidth / 2f,
                iconY - iconHeight / 2f,
                xs[index] + iconWidth / 2f,
                iconY + iconHeight / 2f
            )
            canvas.drawBitmap(bitmap, null, iconBounds, sportIconPaint)
        }
        iconGlowPaint.shader = null
    }

    private fun drawIconGlow(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(radius, radius)
        canvas.drawCircle(0f, 0f, 1f, iconGlowPaint)
        canvas.restore()
    }

    private fun drawVersion(canvas: Canvas, cx: Float, h: Float, unit: Float) {
        subTextPaint.textSize = unit * 0.031f
        subTextPaint.color = Color.argb(versionAlpha, 248, 252, 255)
        subTextPaint.letterSpacing = 0.08f
        canvas.drawText("Version 1.0", cx, h * 0.84f, subTextPaint)
    }
}
