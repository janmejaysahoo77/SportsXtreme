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
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class OnboardingScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Particle(
        var x: Float,
        var y: Float,
        val radius: Float,
        val speedX: Float,
        val speedY: Float,
        val alpha: Int
    )

    private data class SportCard(
        val name: String,
        val icon: Bitmap?
    )

    private val background = Color.rgb(0, 0, 0)
    private val surfaceHigh = Color.rgb(42, 42, 42)
    private val onSurfaceVariant = Color.rgb(195, 202, 172)
    private val primaryFixed = Color.rgb(193, 255, 0)
    private val secondary = Color.rgb(0, 240, 255)
    private val onPrimary = Color.rgb(38, 53, 0)
    private val splashTitleBlue = Color.rgb(0, 127, 255)

    private val bgTexture: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.black)
    private val appIcon: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.appicon)
    private val batsmanImage: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.batsman_onboarding2)
    private val netsBallImage: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.netsball_onboarding2)
    private val stadiumImage: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.stadiumphoto_onboarding4)
    private val footballTileImage: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.footbalimage_onboarding4)
    private val kabaddiTileImage: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.kabaddiimage_onboarding4)
    private val prizeTileImage: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.prizeimage_onboarding4)
    private val proTileImage: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.proimage_onboarding4)
    private val sports = listOf(
        SportCard("Cricket", BitmapFactory.decodeResource(resources, R.drawable.cricketlogo)),
        SportCard("Football", BitmapFactory.decodeResource(resources, R.drawable.footballlogo)),
        SportCard("Kabaddi", BitmapFactory.decodeResource(resources, R.drawable.kabaddilogo)),
        SportCard("Badminton", BitmapFactory.decodeResource(resources, R.drawable.badmintonlogo))
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val appIconSource = Rect()
    private val rect = RectF()
    private val nextButtonRect = RectF()
    private val previousButtonRect = RectF()
    private val bodyText = SpannableString(
        "Organize or join Cricket, Football, Kabaddi, and Badminton tournaments in your local community. Your neighborhood arena awaits."
    )
    private val particles = mutableListOf<Particle>()
    private var topGradient: LinearGradient? = null
    private var bottomGradient: LinearGradient? = null
    private var heroGlow: RadialGradient? = null
    private var bodyLayout: StaticLayout? = null
    private var bodyLayoutWidth = 0
    private var ob2ParagraphLayout: StaticLayout? = null
    private var ob2Feature1Layout: StaticLayout? = null
    private var ob2Feature2Layout: StaticLayout? = null
    private var ob3ParagraphLayout: StaticLayout? = null
    private var ob4ParagraphLayout: StaticLayout? = null
    private var lastLayoutWidth = 0
    private var time = 0f
    private var buttonPressed = false
    private var previousPressed = false
    private var step = 1
    private var oldStep = 0
    private var transitionStartMs = 0L
    private val transitionDurationMs = 350L
    private var isDrawingOldStep = false
    private var startTimeMs = android.os.SystemClock.uptimeMillis()
    private var globalStartTimeMs = startTimeMs
    private var lastFrameMs = startTimeMs

    init {
        isClickable = true
        contentDescription = "SportsXtreme onboarding screen one"
        listOf("Cricket", "Football", "Kabaddi", "Badminton").forEach { sport ->
            val start = bodyText.indexOf(sport)
            val end = start + sport.length
            bodyText.setSpan(ForegroundColorSpan(secondary), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            bodyText.setSpan(StyleSpan(Typeface.BOLD_ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        appIcon?.let { bitmap ->
            appIconSource.set(
                (bitmap.width * 0.2f).toInt(),
                (bitmap.height * 0.34f).toInt(),
                (bitmap.width * 0.84f).toInt(),
                (bitmap.height * 0.62f).toInt()
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startTimeMs = android.os.SystemClock.uptimeMillis()
        globalStartTimeMs = startTimeMs
        lastFrameMs = startTimeMs
        postInvalidateOnAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        topGradient = LinearGradient(
            0f,
            0f,
            0f,
            h.toFloat(),
            intArrayOf(Color.TRANSPARENT, Color.argb(96, 0, 0, 0), background),
            floatArrayOf(0f, 0.42f, 0.82f),
            Shader.TileMode.CLAMP
        )
        bottomGradient = LinearGradient(
            0f,
            h - 128f * resources.displayMetrics.density,
            0f,
            h.toFloat(),
            intArrayOf(Color.TRANSPARENT, Color.argb(218, 0, 0, 0), background),
            floatArrayOf(0f, 0.46f, 1f),
            Shader.TileMode.CLAMP
        )
        heroGlow = RadialGradient(
            w * 0.7f,
            h * 0.26f,
            min(w, h) * 0.72f,
            intArrayOf(Color.argb(80, 0, 240, 255), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )

        particles.clear()
        repeat(32) {
            particles += Particle(
                x = Random.nextFloat() * max(w, 1),
                y = Random.nextFloat() * max(h, 1),
                radius = 0.7f + Random.nextFloat() * 1.4f,
                speedX = -0.12f + Random.nextFloat() * 0.24f,
                speedY = -0.1f + Random.nextFloat() * 0.2f,
                alpha = 34 + Random.nextInt(90)
            )
        }
        bodyLayout = null
        bodyLayoutWidth = 0
        ob2ParagraphLayout = null
        ob2Feature1Layout = null
        ob2Feature2Layout = null
        ob3ParagraphLayout = null
        ob4ParagraphLayout = null
        lastLayoutWidth = 0
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = android.os.SystemClock.uptimeMillis()
        val dt = ((now - lastFrameMs).coerceAtMost(33L).coerceAtLeast(0L)) / 16.666f
        lastFrameMs = now
        time = (now - globalStartTimeMs) / 1000f

        val w = width.toFloat()
        val h = height.toFloat()
        val density = resources.displayMetrics.density
        val margin = 20f * density

        drawBackground(canvas, w, h, density)
        drawHeader(canvas, w, margin, density)
        
        val transitionProgress = ((now - transitionStartMs).toFloat() / transitionDurationMs).coerceIn(0f, 1f)
        val easeOut = 1f - (1f - transitionProgress) * (1f - transitionProgress) * (1f - transitionProgress)

        if (oldStep > 0 && transitionProgress < 1f) {
            val direction = if (step > oldStep) -1f else 1f
            
            val oldOffsetX = easeOut * w * direction
            canvas.save()
            canvas.translate(oldOffsetX, 0f)
            isDrawingOldStep = true
            drawStepContent(canvas, oldStep, w, h, margin, density)
            canvas.restore()
            
            val newOffsetX = (1f - easeOut) * w * -direction
            canvas.save()
            canvas.translate(newOffsetX, 0f)
            isDrawingOldStep = false
            drawStepContent(canvas, step, w, h, margin, density)
            canvas.restore()
        } else {
            isDrawingOldStep = false
            drawStepContent(canvas, step, w, h, margin, density)
        }

        drawBottomBar(canvas, w, h, margin, density)
        drawParticles(canvas, w, h, dt)

        postInvalidateOnAnimation()
    }

    private fun drawStepContent(canvas: Canvas, stepToDraw: Int, w: Float, h: Float, margin: Float, density: Float) {
        when (stepToDraw) {
            1 -> drawContent(canvas, w, h, margin, density)
            2 -> drawOnboardingTwo(canvas, w, h, margin, density)
            3 -> drawOnboardingThree(canvas, w, h, margin, density)
            else -> drawOnboardingFour(canvas, w, h, margin, density)
        }
    }

    private fun drawBackground(canvas: Canvas, w: Float, h: Float, density: Float) {
        canvas.drawColor(background)

        val texture = if (step == 4) stadiumImage ?: bgTexture else bgTexture
        texture?.let { bitmap ->
            val scale = max(w / bitmap.width, h / bitmap.height)
            val bw = bitmap.width * scale
            val bh = bitmap.height * scale
            rect.set((w - bw) / 2f, (h - bh) / 2f, (w + bw) / 2f, (h + bh) / 2f)
            paint.alpha = if (step == 4) 150 else 190
            canvas.drawBitmap(bitmap, null, rect, paint)
            paint.alpha = 255
        }
        if (step == 4) {
            paint.shader = LinearGradient(
                0f,
                h * 0.24f,
                0f,
                h,
                intArrayOf(Color.argb(80, 0, 0, 0), Color.argb(238, 0, 0, 0)),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, w, h, paint)
            paint.shader = null
        }
        drawLargeBackgroundLogo(canvas, w, h, density)
        paint.shader = heroGlow
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
        drawArenaLines(canvas, w, h, density, step == 1)
        paint.shader = topGradient
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
    }

    private fun drawLargeBackgroundLogo(canvas: Canvas, w: Float, h: Float, density: Float) {
        val bitmap = appIcon ?: return
        if (appIconSource.isEmpty) return

        val logoWidth = min(w * 0.9f, 430f * density)
        val logoHeight = logoWidth * appIconSource.height() / appIconSource.width()
        val centerX = w * 0.58f + sin(time * 0.35f) * 4f * density
        val centerY = h * 0.3f + cos(time * 0.28f) * 3f * density

        rect.set(
            centerX - logoWidth / 2f,
            centerY - logoHeight / 2f,
            centerX + logoWidth / 2f,
            centerY + logoHeight / 2f
        )
        bitmapPaint.alpha = 34
        canvas.drawBitmap(bitmap, appIconSource, rect, bitmapPaint)
        bitmapPaint.alpha = 255
    }

    private fun drawArenaLines(canvas: Canvas, w: Float, h: Float, density: Float, drawAccentLines: Boolean) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.1f * density
        paint.color = Color.argb(32, 193, 255, 0)
        val grid = 40f * density
        var x = (time * 8f * density) % grid - grid
        while (x < w + grid) {
            canvas.drawLine(x, 0f, x - w * 0.12f, h, paint)
            x += grid
        }
        var y = (time * 6f * density) % grid - grid
        while (y < h + grid) {
            canvas.drawLine(0f, y, w, y + h * 0.08f, paint)
            y += grid
        }

        if (drawAccentLines) {
            paint.color = Color.argb(70, 0, 240, 255)
            paint.strokeWidth = 1.6f * density
            canvas.drawLine(-w * 0.1f, h * 0.52f, w * 0.95f, h * 0.12f, paint)
            canvas.drawLine(w * 0.15f, h * 0.92f, w * 1.05f, h * 0.57f, paint)
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawHeader(canvas: Canvas, w: Float, margin: Float, density: Float) {
        val phase = phase(40L, 520L)
        val y = 36f * density - (1f - phase) * 10f * density
        drawTopAppIcon(canvas, margin, y, density, phase)

        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 11f * density
        textPaint.letterSpacing = 0.1f
        val stepLabel = "STEP $step OF 4"
        val stepWidth = textPaint.measureText(stepLabel)
        val progressWidth = 64f * density
        val right = w - margin
        canvas.drawText(stepLabel, right - progressWidth - 14f * density - stepWidth, y - 6f * density, textPaint.apply {
            color = onSurfaceVariant
        })

        val progressLeft = right - progressWidth
        rect.set(progressLeft, y - 14f * density, right, y - 8f * density)
        paint.color = surfaceHigh
        canvas.drawRoundRect(rect, 3f * density, 3f * density, paint)
        rect.right = progressLeft + progressWidth * (step / 4f)
        paint.color = Color.argb((255 * phase).toInt(), 193, 255, 0)
        canvas.drawRoundRect(rect, 3f * density, 3f * density, paint)
        textPaint.letterSpacing = 0f
        textPaint.alpha = 255
    }

    private fun drawTopAppIcon(canvas: Canvas, left: Float, baselineY: Float, density: Float, phase: Float) {
        val bitmap = appIcon ?: return
        if (appIconSource.isEmpty) return

        val iconWidth = 50f * density
        val iconHeight = iconWidth * appIconSource.height() / appIconSource.width()
        val top = baselineY - iconHeight + 4f * density + sin(time * 1.4f) * 1.5f * density
        rect.set(left, top, left + iconWidth, top + iconHeight)

        drawTopLogoGlow(canvas, rect.centerX(), rect.centerY(), density, phase)

        bitmapPaint.alpha = (255 * phase).toInt()
        canvas.drawBitmap(bitmap, appIconSource, rect, bitmapPaint)
        bitmapPaint.alpha = 255
    }

    private fun drawTopLogoGlow(canvas: Canvas, cx: Float, cy: Float, density: Float, phase: Float) {
        val pulse = 0.82f + ((sin(time * 2.2f) + 1f) * 0.09f)
        val outerRadius = 64f * density * pulse
        val middleRadius = 44f * density * pulse
        val innerRadius = 26f * density * pulse

        paint.color = Color.argb((18 * phase).toInt(), 0, 127, 255)
        canvas.drawCircle(cx, cy, outerRadius, paint)
        paint.color = Color.argb((34 * phase).toInt(), 0, 127, 255)
        canvas.drawCircle(cx, cy, middleRadius, paint)
        paint.color = Color.argb((48 * phase).toInt(), 0, 127, 255)
        canvas.drawCircle(cx, cy, innerRadius, paint)
    }

    private fun drawContent(canvas: Canvas, w: Float, h: Float, margin: Float, density: Float) {
        val bottomBarHeight = 118f * density
        val contentBottom = h - bottomBarHeight
        val contentTop = max(180f * density, contentBottom - 374f * density)

        val chipPhase = phase(120L, 560L)
        val welcomePhase = phase(80L, 560L)
        drawWelcomeCopy(
            canvas,
            margin,
            contentTop - 128f * density + (1f - welcomePhase) * 12f * density,
            density,
            welcomePhase
        )
        drawStatusChip(
            canvas,
            margin,
            contentTop + (1f - chipPhase) * 18f * density + sin(time * 1.8f) * 1.4f * density,
            density,
            chipPhase
        )

        val titlePhase = phase(240L, 680L)
        val titleScale = scaleFromSmall(240L, 680L)
        val titleY = contentTop + 86f * density + (1f - titlePhase) * 15f * density + sin(time * 1.05f) * 1.6f * density
        textPaint.alpha = (titlePhase * 255).toInt()
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = min(34f * density, (w - margin * 2f) / 9.2f)
        textPaint.color = Color.WHITE
        drawSkewedText(canvas, "HOST LOCAL", margin, titleY, textPaint, titleScale)
        textPaint.color = primaryFixed
        drawSkewedText(canvas, "BATTLES", margin, titleY + 40f * density, textPaint, titleScale)
        textPaint.alpha = 255

        val bodyPhase = phase(430L, 620L)
        drawBodyText(
            canvas,
            margin,
            contentTop + 154f * density + (1f - bodyPhase) * 12f * density,
            min(w - margin * 2f, 340f * density),
            density,
            bodyPhase
        )
        drawSportCards(canvas, w, contentTop + 258f * density, margin, density)
    }

    private fun drawWelcomeCopy(canvas: Canvas, left: Float, top: Float, density: Float, phase: Float) {
        val alpha = (255 * phase).toInt()
        textPaint.alpha = alpha
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 35f * density
        textPaint.letterSpacing = 0.01f
        textPaint.color = Color.WHITE
        val lead = "Welcome to"
        canvas.drawText(lead, left, top, textPaint)

        val nextLineY = top + 38f * density
        var x = left
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        val sports = "Sports"
        val xtreme = "Xtreme"
        textPaint.color = Color.WHITE
        canvas.drawText(sports, x, nextLineY, textPaint)
        x += textPaint.measureText(sports)
        textPaint.color = splashTitleBlue
        canvas.drawText(xtreme, x, nextLineY, textPaint)

        textPaint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
        textPaint.textSize = 10.4f * density
        textPaint.letterSpacing = 0.16f
        textPaint.color = primaryFixed
        canvas.drawText("EVERY GAME,EVERY GLORY", left, nextLineY + 28f * density, textPaint)
        textPaint.letterSpacing = 0f
        textPaint.alpha = 255
    }

    private fun drawSkewedText(canvas: Canvas, text: String, left: Float, baselineY: Float, textPaint: TextPaint, scale: Float) {
        canvas.save()
        canvas.translate(left, baselineY)
        canvas.scale(scale, scale, 0f, 0f)
        canvas.skew(-0.08f, 0f)
        canvas.drawText(text, 0f, 0f, textPaint)
        canvas.restore()
    }

    private fun drawStatusChip(canvas: Canvas, left: Float, top: Float, density: Float, alphaPhase: Float) {
        val alpha = (alphaPhase * 255).toInt()
        rect.set(left, top, left + 134f * density, top + 31f * density)
        paint.color = Color.argb((22 * alphaPhase).toInt(), 0, 240, 255)
        canvas.drawRoundRect(rect, 16f * density, 16f * density, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * density
        paint.color = Color.argb((86 * alphaPhase).toInt(), 0, 240, 255)
        canvas.drawRoundRect(rect, 16f * density, 16f * density, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.argb(((150 + sin(time * 3f) * 80) * alphaPhase).toInt(), 0, 238, 252)
        canvas.drawCircle(left + 17f * density, top + 15.5f * density, 4f * density, paint)

        textPaint.alpha = alpha
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 10.5f * density
        textPaint.letterSpacing = 0.14f
        textPaint.color = secondary
        canvas.drawText("LOCAL ARENA", left + 30f * density, top + 20f * density, textPaint)
        textPaint.letterSpacing = 0f
        textPaint.alpha = 255
    }

    private fun drawBodyText(canvas: Canvas, left: Float, top: Float, width: Float, density: Float, alphaPhase: Float) {
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 15.5f * density
        textPaint.color = onSurfaceVariant
        val roundedWidth = width.toInt()
        val layout = if (bodyLayout == null || bodyLayoutWidth != roundedWidth) {
            bodyLayoutWidth = roundedWidth
            StaticLayout.Builder.obtain(bodyText, 0, bodyText.length, textPaint, roundedWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(3f * density, 1f)
                .setIncludePad(false)
                .build()
                .also { bodyLayout = it }
        } else {
            bodyLayout
        } ?: return

        canvas.save()
        canvas.translate(left, top)
        canvas.saveLayerAlpha(0f, 0f, width, layout.height.toFloat(), (alphaPhase * 255).toInt())
        layout.draw(canvas)
        canvas.restore()
        canvas.restore()
    }

    private fun drawSportCards(canvas: Canvas, w: Float, top: Float, margin: Float, density: Float) {
        val gap = 8f * density
        val cardWidth = (w - margin * 2f - gap * 3f) / 4f
        val cardHeight = 76f * density

        sports.forEachIndexed { index, sport ->
            val phase = phase(560L + index * 90L, 600L)
            val scale = scaleFromSmall(560L + index * 90L, 600L)
            val left = margin + index * (cardWidth + gap)
            val floatY = (1f - phase) * 14f * density
            rect.set(left, top + floatY, left + cardWidth, top + cardHeight + floatY)

            canvas.save()
            canvas.scale(scale, scale, rect.centerX(), rect.centerY())
            paint.color = Color.argb((154 * phase).toInt(), 8, 8, 8)
            canvas.drawRoundRect(rect, 8f * density, 8f * density, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f * density
            paint.color = Color.argb((34 * phase).toInt(), 255, 255, 255)
            canvas.drawRoundRect(rect, 8f * density, 8f * density, paint)
            paint.style = Paint.Style.FILL

            sport.icon?.let { bitmap ->
                val iconPop = 0.78f + 0.22f * scaleFromSmall(680L + index * 90L, 520L)
                val iconSize = min(28f * density, cardWidth * 0.42f) * iconPop
                val iconTop = top + 13f * density + floatY
                rect.set(
                    left + cardWidth / 2f - iconSize / 2f,
                    iconTop,
                    left + cardWidth / 2f + iconSize / 2f,
                    iconTop + iconSize
                )
                paint.alpha = (235 * phase).toInt()
                canvas.drawBitmap(bitmap, null, rect, paint)
                paint.alpha = 255
            }

            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textPaint.textSize = 7.5f * density
            textPaint.letterSpacing = -0.02f
            textPaint.color = onSurfaceVariant
            textPaint.alpha = (phase * 255).toInt()
            val labelWidth = textPaint.measureText(sport.name.uppercase())
            canvas.drawText(sport.name.uppercase(), left + cardWidth / 2f - labelWidth / 2f, top + 62f * density + floatY, textPaint)
            textPaint.letterSpacing = 0f
            textPaint.alpha = 255
            canvas.restore()
        }
    }

    private fun drawOnboardingTwo(canvas: Canvas, w: Float, h: Float, margin: Float, density: Float) {
        val phase = phase(80L, 520L)
        val top = 68f * density
        val cardRadius = 8f * density

        val heroTop = top + (1f - phase) * 22f * density
        val heroBounds = RectF(margin, heroTop, w - margin, heroTop + 120f * density)
        drawGlassPanel(canvas, heroBounds, cardRadius, phase)
        drawDarkImage(canvas, batsmanImage, heroBounds, 142)

        textPaint.alpha = (255 * phase).toInt()
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 8.5f * density
        textPaint.color = primaryFixed
        canvas.drawText("COMMUNITY HIGHLIGHT", heroBounds.left + 12f * density, heroBounds.top + 18f * density, textPaint)
        textPaint.textSize = 18f * density
        textPaint.color = Color.WHITE
        canvas.drawText("Best Batsman", heroBounds.left + 12f * density, heroBounds.top + 38f * density, textPaint)
        textPaint.textSize = 36f * density
        textPaint.color = primaryFixed
        canvas.drawText("185.5", heroBounds.left + 12f * density, heroBounds.bottom - 23f * density, textPaint)
        drawTinyBars(canvas, heroBounds.right - 82f * density, heroBounds.bottom - 23f * density, density, phase)
        drawPill(canvas, heroBounds.right - 74f * density, heroBounds.top + 13f * density, "VIBE CHECK", density, phase)

        val gridTop = heroTop + 132f * density
        val halfGap = 10f * density
        val smallW = (w - margin * 2f - halfGap) / 2f
        val leftCard = RectF(margin, gridTop, margin + smallW, gridTop + 104f * density)
        drawGlassPanel(canvas, leftCard, cardRadius, phase)
        val imageRect = RectF(leftCard.left + 9f * density, leftCard.top + 9f * density, leftCard.right - 9f * density, leftCard.top + 64f * density)
        drawDarkImage(canvas, netsBallImage, imageRect, 96)
        drawPlayButton(canvas, imageRect.centerX(), imageRect.centerY(), density, phase)
        drawLabelValue(canvas, leftCard.left + 12f * density, leftCard.bottom - 29f * density, "HIGHLIGHT", "Top Bin Strike", density, phase)

        val rightCard = RectF(margin + smallW + halfGap, gridTop, w - margin, gridTop + 104f * density)
        drawGlassPanel(canvas, rightCard, cardRadius, phase)
        drawMvpIcon(canvas, rightCard.left + 24f * density, rightCard.top + 28f * density, density, phase)
        drawPill(canvas, rightCard.right - 78f * density, rightCard.top + 13f * density, "LOCAL MVP", density, phase)
        drawLabelValue(canvas, rightCard.left + 12f * density, rightCard.bottom - 29f * density, "HUSTLE RATING", "921", density, phase)

        val cardsBottom = gridTop + 104f * density
        val availableSpace = h - 128f * density - cardsBottom
        val targetSpace = 240f * density
        val dynamicGap = max(0f, (availableSpace - targetSpace) / 4f)

        val titleTop = cardsBottom + 22f * density + dynamicGap
        val titleScale = scaleFromSmall(360L, 620L)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 31f * density
        textPaint.alpha = (255 * phase).toInt()
        textPaint.color = Color.WHITE
        drawSkewedText(canvas, "TRACK LOCAL", margin, titleTop, textPaint, titleScale)
        textPaint.color = primaryFixed
        drawSkewedText(canvas, "LEGENDS", margin, titleTop + 34f * density, textPaint, titleScale)

        val copyTop = titleTop + 54f * density + dynamicGap
        val textWidth = w - margin * 2f
        val paragraphText = "See stats for local players and highlights from neighborhood games. Simple tracking for every host and player."
        
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 15.2f * density
        textPaint.color = Color.WHITE
        
        val roundedWidth = textWidth.toInt()
        if (ob2ParagraphLayout == null || lastLayoutWidth != roundedWidth) {
            lastLayoutWidth = roundedWidth
            ob2ParagraphLayout = StaticLayout.Builder.obtain(paragraphText, 0, paragraphText.length, textPaint, roundedWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(3f * density, 1f)
                .setIncludePad(false)
                .build()
        }
        
        canvas.save()
        canvas.translate(margin, copyTop)
        canvas.saveLayerAlpha(0f, 0f, textWidth, ob2ParagraphLayout!!.height.toFloat(), (230 * phase).toInt())
        ob2ParagraphLayout!!.draw(canvas)
        canvas.restore()
        canvas.restore()

        val feature1Y = copyTop + ob2ParagraphLayout!!.height + 26f * density + dynamicGap
        ob2Feature1Layout = drawFeatureRow(canvas, margin, feature1Y, "Neighborhood Heatmaps", "Track where the most action happens on your local pitch.", density, phase, true, textWidth, ob2Feature1Layout)
        
        val feature2Y = feature1Y + ob2Feature1Layout!!.height + 34f * density + dynamicGap
        ob2Feature2Layout = drawFeatureRow(canvas, margin, feature2Y, "Community Boards", "Climb the local ranks and earn your spot among the park legends.", density, phase, false, textWidth, ob2Feature2Layout)
        textPaint.alpha = 255
    }

    private fun drawOnboardingThree(canvas: Canvas, w: Float, h: Float, margin: Float, density: Float) {
        val phase = phase(80L, 560L)
        val bracketHeight = min(244f * density, h * 0.34f)
        val copyWidth = (w - margin * 2f).toInt()
        val copy = "Tools to help you host better local tournaments. Plan brackets, track team participation, and manage your league with ease.You can score matches and do live stream with very ease."
        
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 14.2f * density
        if (ob3ParagraphLayout == null || lastLayoutWidth != copyWidth) {
            lastLayoutWidth = copyWidth
            ob3ParagraphLayout = StaticLayout.Builder.obtain(copy, 0, copy.length, textPaint, copyWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(3f * density, 1f)
                .setIncludePad(false)
                .build()
        }
        val textHeight = ob3ParagraphLayout!!.height.toFloat()

        val targetSpace = 100f * density + bracketHeight + 48f * density + 56f * density + textHeight
        val availableSpace = h - 128f * density
        val dynamicGap = max(0f, (availableSpace - targetSpace) / 3f)

        val bracketTop = 100f * density + dynamicGap + (1f - phase) * 22f * density
        val bracketBounds = RectF(margin, bracketTop, w - margin, bracketTop + bracketHeight)

        drawTournamentHalo(canvas, w, bracketBounds.centerY(), density, phase)
        drawAutoBracketingPill(canvas, w - margin - 120f * density, bracketTop - 36f * density, density, phase)
        drawTournamentBoard(canvas, bracketBounds, density, phase)
        drawRosterSyncBadge(canvas, margin - 8f * density, bracketBounds.bottom - 27f * density, density, phase)
        drawActiveManagementBar(canvas, margin + 55f * density, bracketBounds.bottom - 26f * density, w - margin, density, phase)

        val titleTop = bracketBounds.bottom + 48f * density + dynamicGap
        val titleScale = scaleFromSmall(320L, 620L)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 29f * density
        textPaint.alpha = (255 * phase).toInt()
        textPaint.color = Color.WHITE
        drawSkewedText(canvas, "TOURNAMENT", margin, titleTop, textPaint, titleScale)
        textPaint.color = primaryFixed
        drawSkewedText(canvas, "MANAGEMENT", margin, titleTop + 35f * density, textPaint, titleScale)

        val copyTop = titleTop + 56f * density + dynamicGap
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 14.2f * density
        textPaint.color = Color.WHITE
        textPaint.alpha = (235 * phase).toInt()
        canvas.save()
        canvas.translate(margin, copyTop)
        canvas.saveLayerAlpha(0f, 0f, copyWidth.toFloat(), textHeight, (235 * phase).toInt())
        ob3ParagraphLayout!!.draw(canvas)
        canvas.restore()
        canvas.restore()
        textPaint.alpha = 255
    }

    private fun drawTournamentHalo(canvas: Canvas, cx: Float, cy: Float, density: Float, phase: Float) {
        val radius = 124f * density
        paint.color = Color.argb((12 * phase).toInt(), 193, 255, 0)
        canvas.drawCircle(cx * 0.55f, cy + 28f * density, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * density
        paint.color = Color.argb((44 * phase).toInt(), 193, 255, 0)
        canvas.drawCircle(cx * 0.55f, cy + 28f * density, radius, paint)
        paint.color = Color.argb((28 * phase).toInt(), 0, 240, 255)
        canvas.drawCircle(cx * 0.55f, cy + 28f * density, radius * 0.58f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawAutoBracketingPill(canvas: Canvas, left: Float, top: Float, density: Float, phase: Float) {
        rect.set(left, top, left + 120f * density, top + 29f * density)
        paint.color = Color.argb((210 * phase).toInt(), 1, 8, 6)
        canvas.drawRoundRect(rect, 9f * density, 9f * density, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * density
        paint.color = Color.argb((235 * phase).toInt(), 193, 255, 0)
        canvas.drawRoundRect(rect, 9f * density, 9f * density, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.argb((255 * phase).toInt(), 193, 255, 0)
        canvas.drawCircle(left + 14f * density, top + 14.5f * density, 3.2f * density, paint)
        rect.set(left + 11f * density, top + 17f * density, left + 17f * density, top + 21f * density)
        canvas.drawRoundRect(rect, 2f * density, 2f * density, paint)

        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 8.8f * density
        textPaint.color = primaryFixed
        textPaint.alpha = (255 * phase).toInt()
        canvas.drawText("AUTO-BRACKETING", left + 25f * density, top + 18.5f * density, textPaint)
        textPaint.alpha = 255
    }

    private fun drawTournamentBoard(canvas: Canvas, bounds: RectF, density: Float, phase: Float) {
        drawGlassPanel(canvas, bounds, 9f * density, phase)
        paint.color = Color.argb((90 * phase).toInt(), 0, 0, 0)
        canvas.drawRoundRect(bounds, 9f * density, 9f * density, paint)

        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 7.4f * density
        textPaint.alpha = (255 * phase).toInt()
        textPaint.color = onSurfaceVariant
        paint.color = Color.argb((210 * phase).toInt(), 112, 190, 22)
        canvas.drawCircle(bounds.left + 20f * density, bounds.top + 24f * density, 3.3f * density, paint)
        canvas.drawText("QUARTER-FINALS GROUP A", bounds.left + 31f * density, bounds.top + 27f * density, textPaint)
        textPaint.color = primaryFixed
        canvas.drawText("12 TEAMS", bounds.right - 57f * density, bounds.top + 27f * density, textPaint)

        paint.strokeWidth = 1f * density
        paint.style = Paint.Style.STROKE
        paint.color = Color.argb((45 * phase).toInt(), 255, 255, 255)
        canvas.drawLine(bounds.left + 18f * density, bounds.top + 39f * density, bounds.right - 18f * density, bounds.top + 39f * density, paint)
        paint.style = Paint.Style.FILL

        val rowLeft = bounds.left + 18f * density
        val rowTop = bounds.top + 58f * density
        val rowW = 94f * density
        val rowH = 25f * density
        val rows = arrayOf("TITANS FC" to "2", "EAGLES UNITED" to "0", "STORM BLAZE" to "-", "REBEL KINGS" to "-")
        rows.forEachIndexed { index, item ->
            val y = rowTop + index * 31f * density
            drawTeamRow(canvas, rowLeft, y, rowW, rowH, item.first, item.second, index < 2, density, phase)
        }

        val resultLeft = bounds.right - 63f * density
        val qualifiedTop = rowTop + 20f * density
        val tbdTop = rowTop + 87f * density
        drawBracketConnector(
            canvas,
            rowLeft + rowW,
            rowTop + rowH / 2f,
            rowTop + 31f * density + rowH / 2f,
            resultLeft,
            qualifiedTop + 12f * density,
            density,
            phase,
            true
        )
        drawBracketConnector(
            canvas,
            rowLeft + rowW,
            rowTop + 2 * 31f * density + rowH / 2f,
            rowTop + 3 * 31f * density + rowH / 2f,
            resultLeft,
            tbdTop + 12f * density,
            density,
            phase,
            false
        )

        drawQualifiedBadge(canvas, resultLeft, qualifiedTop, density, phase)
        drawTbdBox(canvas, resultLeft, tbdTop, density, phase)
        textPaint.alpha = 255
    }

    private fun drawBracketConnector(
        canvas: Canvas,
        rowRight: Float,
        firstMidY: Float,
        secondMidY: Float,
        resultLeft: Float,
        resultMidY: Float,
        density: Float,
        phase: Float,
        active: Boolean
    ) {
        val startX = rowRight + 10f * density
        val joinX = rowRight + 23f * density
        val resultX = resultLeft - 10f * density
        val alpha = if (active) 230 else 92

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.SQUARE
        paint.strokeJoin = Paint.Join.MITER
        paint.strokeWidth = 2.1f * density
        paint.color = if (active) {
            Color.argb((alpha * phase).toInt(), 193, 255, 0)
        } else {
            Color.argb((alpha * phase).toInt(), 125, 135, 120)
        }

        canvas.drawLine(rowRight, firstMidY, startX, firstMidY, paint)
        canvas.drawLine(rowRight, secondMidY, startX, secondMidY, paint)
        canvas.drawLine(startX, firstMidY, joinX, firstMidY, paint)
        canvas.drawLine(startX, secondMidY, joinX, secondMidY, paint)
        canvas.drawLine(joinX, firstMidY, joinX, secondMidY, paint)
        canvas.drawLine(joinX, resultMidY, resultX, resultMidY, paint)
        canvas.drawLine(resultX, resultMidY, resultLeft, resultMidY, paint)

        if (active) {
            paint.strokeWidth = 4.2f * density
            paint.color = Color.argb((32 * phase).toInt(), 193, 255, 0)
            canvas.drawLine(joinX, resultMidY, resultLeft, resultMidY, paint)
        }

        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.FILL
    }

    private fun drawTeamRow(canvas: Canvas, left: Float, top: Float, width: Float, height: Float, name: String, score: String, active: Boolean, density: Float, phase: Float) {
        rect.set(left, top, left + width, top + height)
        paint.color = if (active) Color.argb((188 * phase).toInt(), 38, 42, 42) else Color.argb((92 * phase).toInt(), 34, 36, 36)
        canvas.drawRoundRect(rect, 3f * density, 3f * density, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.argb((28 * phase).toInt(), 255, 255, 255)
        canvas.drawRoundRect(rect, 3f * density, 3f * density, paint)
        paint.style = Paint.Style.FILL

        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 7.4f * density
        textPaint.color = if (active) Color.WHITE else Color.argb(170, 220, 224, 214)
        textPaint.alpha = (255 * phase).toInt()
        canvas.drawText(name, left + 7f * density, top + 15.8f * density, textPaint)
        textPaint.color = if (active && score == "2") primaryFixed else Color.WHITE
        canvas.drawText(score, left + width - 13f * density, top + 15.8f * density, textPaint)
    }

    private fun drawQualifiedBadge(canvas: Canvas, left: Float, top: Float, density: Float, phase: Float) {
        rect.set(left, top, left + 45f * density, top + 24f * density)
        paint.color = Color.argb((255 * phase).toInt(), 112, 190, 22)
        canvas.drawRoundRect(rect, 4f * density, 4f * density, paint)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 7.2f * density
        textPaint.color = Color.rgb(4, 24, 0)
        textPaint.alpha = (255 * phase).toInt()
        canvas.drawText("QUALIFIED", left + 5f * density, top + 15f * density, textPaint)
    }

    private fun drawTbdBox(canvas: Canvas, left: Float, top: Float, density: Float, phase: Float) {
        rect.set(left, top, left + 45f * density, top + 24f * density)
        paint.color = Color.argb((52 * phase).toInt(), 255, 255, 255)
        canvas.drawRoundRect(rect, 4f * density, 4f * density, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.argb((42 * phase).toInt(), 255, 255, 255)
        canvas.drawRoundRect(rect, 4f * density, 4f * density, paint)
        paint.style = Paint.Style.FILL
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 7.4f * density
        textPaint.color = Color.argb(175, 255, 255, 255)
        textPaint.alpha = (255 * phase).toInt()
        canvas.drawText("TBD", left + 15f * density, top + 15f * density, textPaint)
    }

    private fun drawRosterSyncBadge(canvas: Canvas, left: Float, top: Float, density: Float, phase: Float) {
        rect.set(left, top, left + 96f * density, top + 27f * density)
        paint.color = Color.argb((230 * phase).toInt(), 0, 11, 12)
        canvas.drawRoundRect(rect, 7f * density, 7f * density, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f * density
        paint.color = Color.argb((220 * phase).toInt(), 0, 240, 255)
        canvas.drawRoundRect(rect, 7f * density, 7f * density, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb((255 * phase).toInt(), 0, 240, 255)
        canvas.drawCircle(left + 12f * density, top + 14f * density, 3f * density, paint)
        canvas.drawRect(left + 7f * density, top + 16f * density, left + 17f * density, top + 19f * density, paint)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 8f * density
        textPaint.color = secondary
        textPaint.alpha = (255 * phase).toInt()
        canvas.drawText("ROSTER SYNC", left + 23f * density, top + 17f * density, textPaint)
        textPaint.alpha = 255
    }

    private fun drawActiveManagementBar(canvas: Canvas, left: Float, top: Float, right: Float, density: Float, phase: Float) {
        rect.set(left, top, right, top + 23f * density)
        paint.color = Color.argb((205 * phase).toInt(), 31, 31, 35)
        canvas.drawRoundRect(rect, 10f * density, 10f * density, paint)
        paint.color = Color.argb((255 * phase).toInt(), 193, 255, 0)
        canvas.drawCircle(left + 18f * density, top + 11.5f * density, 5f * density, paint)
        paint.color = Color.argb((220 * phase).toInt(), 0, 240, 255)
        canvas.drawCircle(left + 30f * density, top + 11.5f * density, 5f * density, paint)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 7.4f * density
        textPaint.color = Color.WHITE
        textPaint.alpha = (210 * phase).toInt()
        canvas.drawText("Active management...", right - 88f * density, top + 15f * density, textPaint)
        textPaint.alpha = 255
    }

    private fun drawOnboardingFour(canvas: Canvas, w: Float, h: Float, margin: Float, density: Float) {
        val phase = phase(80L, 560L)
        val contentBottom = h - 126f * density
        val tileGap = 10f * density
        val tileWidth = (w - margin * 2f - tileGap) / 2f
        val tileHeight = min(104f * density, tileWidth * 0.78f)
        val gridHeight = tileHeight * 2f + tileGap
        val copyWidth = (w - margin * 2f).toInt()
        val copy = "Join the sportsxtreme community of local hosts and players. Create your first tournament today."

        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 13.7f * density
        if (ob4ParagraphLayout == null || lastLayoutWidth != copyWidth) {
            lastLayoutWidth = copyWidth
            val copySpannable = SpannableString(copy)
            copySpannable.setSpan(StyleSpan(Typeface.BOLD_ITALIC), 9, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ob4ParagraphLayout = StaticLayout.Builder.obtain(copySpannable, 0, copySpannable.length, textPaint, copyWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(3f * density, 1f)
                .setIncludePad(false)
                .build()
        }

        val copyHeight = ob4ParagraphLayout!!.height.toFloat()
        val titleHeight = 72f * density
        val chipHeight = 25f * density
        val totalHeight = chipHeight + 32f * density + titleHeight + 70f * density + copyHeight + 20f * density + gridHeight
        val availableSpace = contentBottom - 76f * density
        val dynamicGap = max(0f, (availableSpace - totalHeight) / 4f)

        val chipTop = 76f * density + dynamicGap
        drawOnlineChip(canvas, margin, chipTop, density, phase)
        drawOnboardingFourTags(canvas, margin + 152f * density, chipTop, density, phase)

        val titleTop = chipTop + chipHeight + 32f * density + dynamicGap
        val titleScale = scaleFromSmall(260L, 620L)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 31f * density
        textPaint.alpha = (255 * phase).toInt()
        textPaint.color = Color.WHITE
        drawSkewedText(canvas, "START YOUR", margin, titleTop, textPaint, titleScale)
        textPaint.color = primaryFixed
        drawSkewedText(canvas, "LEAGUE", margin, titleTop + 37f * density, textPaint, titleScale)

        paint.color = Color.argb((255 * phase).toInt(), 0, 240, 255)
        rect.set(margin, titleTop + 49f * density, margin + 48f * density, titleTop + 53f * density)
        canvas.drawRoundRect(rect, 2f * density, 2f * density, paint)

        val copyTop = titleTop + 70f * density + dynamicGap
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 13.7f * density
        textPaint.color = Color.WHITE
        textPaint.alpha = (238 * phase).toInt()
        canvas.save()
        canvas.translate(margin, copyTop)
        canvas.saveLayerAlpha(0f, 0f, copyWidth.toFloat(), copyHeight, (238 * phase).toInt())
        ob4ParagraphLayout!!.draw(canvas)
        canvas.restore()
        canvas.restore()

        val gridTop = copyTop + copyHeight + 20f * density + dynamicGap
        drawOnboardingFourTile(canvas, footballTileImage, margin, gridTop, tileWidth, tileHeight, "LOCAL LEADERBOARD", "CITY #1", density, phase)
        drawOnboardingFourTile(canvas, kabaddiTileImage, margin + tileWidth + tileGap, gridTop, tileWidth, tileHeight, "COMMUNITY STATUS", "HOST TOOLS", density, phase)
        drawOnboardingFourTile(canvas, prizeTileImage, margin, gridTop + tileHeight + tileGap, tileWidth, tileHeight, "PERFORMANCE", "REWARDS", density, phase)
        drawOnboardingFourTile(canvas, proTileImage, margin + tileWidth + tileGap, gridTop + tileHeight + tileGap, tileWidth, tileHeight, "CAREER PATH", "GO PRO", density, phase)
        drawDecorativeHud(canvas, w, h, density, phase)
        textPaint.alpha = 255
    }

    private fun drawOnlineChip(canvas: Canvas, left: Float, top: Float, density: Float, phase: Float) {
        rect.set(left, top, left + 144f * density, top + 28f * density)
        paint.color = Color.argb((132 * phase).toInt(), 28, 31, 31)
        canvas.drawRoundRect(rect, 14f * density, 14f * density, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * density
        paint.color = Color.argb((48 * phase).toInt(), 255, 255, 255)
        canvas.drawRoundRect(rect, 14f * density, 14f * density, paint)
        paint.style = Paint.Style.FILL
        val pulse = ((sin(time * 4f) + 1f) * 0.5f)
        paint.color = Color.argb(((70 + pulse * 80) * phase).toInt(), 193, 255, 0)
        canvas.drawCircle(left + 17f * density, top + 14f * density, 8f * density, paint)
        paint.color = Color.argb((255 * phase).toInt(), 193, 255, 0)
        canvas.drawCircle(left + 17f * density, top + 14f * density, 4f * density, paint)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 8.7f * density
        textPaint.color = onSurfaceVariant
        textPaint.alpha = (255 * phase).toInt()
        canvas.drawText("1,284 PLAYERS ONLINE", left + 31f * density, top + 18f * density, textPaint)
    }

    private fun drawOnboardingFourTags(canvas: Canvas, left: Float, top: Float, density: Float, phase: Float) {
        drawTinyTag(canvas, left, top, "TOP HOST: DELHI", secondary, density, phase)
    }

    private fun drawTinyTag(canvas: Canvas, left: Float, top: Float, text: String, color: Int, density: Float, phase: Float) {
        val width = if (text.length > 18) 148f * density else 118f * density
        rect.set(left, top, left + width, top + 25f * density)
        paint.color = Color.argb((95 * phase).toInt(), 53, 53, 52)
        canvas.drawRoundRect(rect, 12f * density, 12f * density, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * density
        paint.color = Color.argb((88 * phase).toInt(), Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawRoundRect(rect, 12f * density, 12f * density, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.argb((255 * phase).toInt(), Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawCircle(left + 13f * density, top + 12.5f * density, 3.4f * density, paint)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 7.2f * density
        textPaint.color = color
        textPaint.alpha = (255 * phase).toInt()
        canvas.drawText(text, left + 23f * density, top + 16f * density, textPaint)
    }

    private fun drawOnboardingFourTile(canvas: Canvas, bitmap: Bitmap?, left: Float, top: Float, width: Float, height: Float, label: String, value: String, density: Float, phase: Float) {
        rect.set(left, top, left + width, top + height)
        canvas.save()
        canvas.clipRect(rect)
        bitmap?.let {
            val scale = max(width / it.width, height / it.height)
            val bw = it.width * scale
            val bh = it.height * scale
            val img = RectF(rect.centerX() - bw / 2f, rect.centerY() - bh / 2f, rect.centerX() + bw / 2f, rect.centerY() + bh / 2f)
            bitmapPaint.alpha = (255 * phase).toInt()
            canvas.drawBitmap(it, null, img, bitmapPaint)
            bitmapPaint.alpha = 255
        }
        paint.shader = LinearGradient(0f, top, 0f, top + height, Color.TRANSPARENT, Color.argb((230 * phase).toInt(), 0, 0, 0), Shader.TileMode.CLAMP)
        canvas.drawRect(rect, paint)
        paint.shader = null
        canvas.restore()

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * density
        paint.color = Color.argb((42 * phase).toInt(), 255, 255, 255)
        canvas.drawRoundRect(rect, 8f * density, 8f * density, paint)
        paint.style = Paint.Style.FILL

        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 7.8f * density
        textPaint.color = primaryFixed
        textPaint.alpha = (255 * phase).toInt()
        canvas.drawText(label, left + 10f * density, top + height - 32f * density, textPaint)
        textPaint.textSize = 14f * density
        textPaint.color = Color.WHITE
        canvas.drawText(value, left + 10f * density, top + height - 13f * density, textPaint)
    }

    private fun drawDecorativeHud(canvas: Canvas, w: Float, h: Float, density: Float, phase: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * density
        paint.color = Color.argb((48 * phase).toInt(), 0, 240, 255)
        rect.set(w - 82f * density, h - 276f * density, w + 22f * density, h - 128f * density)
        canvas.drawRoundRect(rect, 28f * density, 28f * density, paint)
        paint.color = Color.argb((42 * phase).toInt(), 195, 202, 172)
        canvas.drawLine(0f, h * 0.5f, 0f, h * 0.68f, paint)
        paint.style = Paint.Style.FILL
        repeat(3) { index ->
            paint.color = Color.argb(((160 - index * 42) * phase).toInt(), 193, 255, 0)
            canvas.drawCircle(8f * density, h * 0.53f + index * 28f * density, 2f * density, paint)
        }
    }

    private fun drawGlassPanel(canvas: Canvas, bounds: RectF, radius: Float, alphaPhase: Float) {
        paint.color = Color.argb((190 * alphaPhase).toInt(), 5, 7, 7)
        canvas.drawRoundRect(bounds, radius, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.argb((48 * alphaPhase).toInt(), 255, 255, 255)
        canvas.drawRoundRect(bounds, radius, radius, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawDarkImage(canvas: Canvas, bitmap: Bitmap?, bounds: RectF, darkAlpha: Int) {
        bitmap ?: return
        canvas.save()
        canvas.clipRect(bounds)
        val scale = max(bounds.width() / bitmap.width, bounds.height() / bitmap.height)
        val bw = bitmap.width * scale
        val bh = bitmap.height * scale
        val img = RectF(bounds.centerX() - bw / 2f, bounds.centerY() - bh / 2f, bounds.centerX() + bw / 2f, bounds.centerY() + bh / 2f)
        bitmapPaint.alpha = 255
        canvas.drawBitmap(bitmap, null, img, bitmapPaint)
        paint.color = Color.argb(darkAlpha, 0, 0, 0)
        canvas.drawRect(bounds, paint)
        canvas.restore()
    }

    private fun drawPill(canvas: Canvas, left: Float, top: Float, text: String, density: Float, alphaPhase: Float) {
        rect.set(left, top, left + 62f * density, top + 18f * density)
        paint.color = Color.argb((28 * alphaPhase).toInt(), 193, 255, 0)
        canvas.drawRoundRect(rect, 9f * density, 9f * density, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.argb((130 * alphaPhase).toInt(), 193, 255, 0)
        canvas.drawRoundRect(rect, 9f * density, 9f * density, paint)
        paint.style = Paint.Style.FILL
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 6.8f * density
        textPaint.color = primaryFixed
        textPaint.alpha = (255 * alphaPhase).toInt()
        canvas.drawText(text, left + 9f * density, top + 12.5f * density, textPaint)
    }

    private fun drawTinyBars(canvas: Canvas, left: Float, bottom: Float, density: Float, phase: Float) {
        paint.color = Color.argb((255 * phase).toInt(), 193, 255, 0)
        repeat(5) { index ->
            val barH = (10 + index * 7) * density * phase
            rect.set(left + index * 9f * density, bottom - barH, left + index * 9f * density + 6f * density, bottom)
            canvas.drawRect(rect, paint)
        }
    }

    private fun drawPlayButton(canvas: Canvas, cx: Float, cy: Float, density: Float, phase: Float) {
        paint.color = Color.argb((120 * phase).toInt(), 255, 255, 255)
        canvas.drawCircle(cx, cy, 16f * density, paint)
        paint.color = Color.argb((235 * phase).toInt(), 255, 255, 255)
        path.reset()
        path.moveTo(cx - 4f * density, cy - 8f * density)
        path.lineTo(cx + 8f * density, cy)
        path.lineTo(cx - 4f * density, cy + 8f * density)
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawLabelValue(canvas: Canvas, x: Float, baseline: Float, label: String, value: String, density: Float, phase: Float) {
        textPaint.alpha = (255 * phase).toInt()
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 8.2f * density
        textPaint.color = secondary
        canvas.drawText(label, x, baseline, textPaint)
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 12.4f * density
        textPaint.color = Color.WHITE
        canvas.drawText(value, x, baseline + 15f * density, textPaint)
    }

    private fun drawMvpIcon(canvas: Canvas, cx: Float, cy: Float, density: Float, phase: Float) {
        paint.color = Color.argb((255 * phase).toInt(), 0, 240, 255)
        canvas.drawCircle(cx - 7f * density, cy, 4f * density, paint)
        canvas.drawCircle(cx + 7f * density, cy, 4f * density, paint)
        canvas.drawCircle(cx, cy - 5f * density, 4.5f * density, paint)
        rect.set(cx - 15f * density, cy + 5f * density, cx + 15f * density, cy + 11f * density)
        canvas.drawRoundRect(rect, 5f * density, 5f * density, paint)
    }

    private fun drawFeatureRow(canvas: Canvas, x: Float, y: Float, title: String, desc: String, density: Float, phase: Float, pin: Boolean, width: Float, layoutCache: StaticLayout?): StaticLayout {
        rect.set(x, y - 20f * density, x + 32f * density, y + 12f * density)
        paint.color = Color.argb((28 * phase).toInt(), 193, 255, 0)
        canvas.drawRoundRect(rect, 6f * density, 6f * density, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f * density
        paint.color = if (pin) primaryFixed else secondary
        if (pin) {
            canvas.drawCircle(x + 16f * density, y - 5f * density, 6f * density, paint)
            canvas.drawLine(x + 16f * density, y + 1f * density, x + 16f * density, y + 8f * density, paint)
        } else {
            repeat(3) { i ->
                val l = x + (8 + i * 7) * density
                canvas.drawRect(l, y + (2 - i * 5) * density, l + 5f * density, y + 8f * density, paint)
            }
        }
        paint.style = Paint.Style.FILL
        textPaint.alpha = (255 * phase).toInt()
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textPaint.textSize = 15.2f * density
        textPaint.color = Color.WHITE
        canvas.drawText(title, x + 44f * density, y - 8f * density, textPaint)
        
        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textPaint.textSize = 12f * density
        
        val descWidth = (width - 44f * density).toInt()
        val layout = layoutCache ?: StaticLayout.Builder.obtain(desc, 0, desc.length, textPaint, descWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f * density, 1f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(x + 44f * density, y + 2f * density)
        canvas.saveLayerAlpha(0f, 0f, descWidth.toFloat(), layout.height.toFloat(), (255 * phase).toInt())
        layout.draw(canvas)
        canvas.restore()
        canvas.restore()
        
        return layout
    }

    private fun drawBottomBar(canvas: Canvas, w: Float, h: Float, margin: Float, density: Float) {
        paint.shader = bottomGradient
        canvas.drawRect(0f, h - 128f * density, w, h, paint)
        paint.shader = null

        val phase = phase(760L, 580L)
        if (step == 1) {
            val dotY = h - 58f * density
            var dotLeft = margin
            repeat(4) { index ->
                val active = index == step - 1
                val dotWidth = if (active) 32f * density else 8f * density
                rect.set(dotLeft, dotY, dotLeft + dotWidth, dotY + 4f * density)
                paint.color = if (active) {
                    Color.argb((255 * phase).toInt(), 193, 255, 0)
                } else {
                    Color.argb((255 * phase).toInt(), 42, 42, 42)
                }
                canvas.drawRoundRect(rect, 2f * density, 2f * density, paint)
                dotLeft += dotWidth + 8f * density
            }
        }

        val buttonWidth = if (step == 4) 188f * density else 136f * density
        val scale = (if (buttonPressed) 0.97f else 1f) * (0.92f + phase * 0.08f + sin(time * 1.6f) * 0.012f)
        val buttonOffsetY = (1f - phase) * 18f * density + sin(time * 1.4f) * 2f * density
        nextButtonRect.set(w - margin - buttonWidth, h - 84f * density + buttonOffsetY, w - margin, h - 26f * density + buttonOffsetY)

        if (step > 1) {
            drawPreviousButton(canvas, margin, h - 84f * density + buttonOffsetY, density, phase)
        } else {
            previousButtonRect.setEmpty()
        }

        canvas.save()
        canvas.scale(scale, scale, nextButtonRect.centerX(), nextButtonRect.centerY())
        paint.color = Color.argb((44 * phase).toInt(), 193, 255, 0)
        rect.set(nextButtonRect)
        rect.inset(-7f * density, -7f * density)
        canvas.drawRoundRect(rect, 18f * density, 18f * density, paint)
        paint.color = Color.argb((255 * phase).toInt(), 193, 255, 0)
        canvas.drawRoundRect(nextButtonRect, 12f * density, 12f * density, paint)

        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 18f * density
        textPaint.color = onPrimary
        textPaint.alpha = (255 * phase).toInt()
        val label = when (step) {
            1 -> "NEXT"
            4 -> "GET STARTED"
            else -> "Next"
        }
        val labelWidth = textPaint.measureText(label)
        val labelX = nextButtonRect.centerX() - labelWidth / 2f - 11f * density
        val labelY = nextButtonRect.centerY() + 7f * density
        canvas.drawText(label, labelX, labelY, textPaint)
        drawArrow(canvas, labelX + labelWidth + 18f * density + sin(time * 4f) * 1.5f * density, nextButtonRect.centerY(), density)
        canvas.restore()

        if (step == 4) {
            textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textPaint.textSize = 10f * density
            textPaint.color = Color.WHITE
            textPaint.alpha = (180 * phase).toInt()
            val footerText = "Proudly Made In India 🇮🇳"
            val footerWidth = textPaint.measureText(footerText)
            canvas.drawText(footerText, w / 2f - footerWidth / 2f, h - 110f * density + buttonOffsetY, textPaint)
        }

        textPaint.alpha = 255
    }

    private fun drawPreviousButton(canvas: Canvas, left: Float, top: Float, density: Float, phase: Float) {
        previousButtonRect.set(left, top, left + 96f * density, top + 58f * density)
        val scale = if (previousPressed) 0.97f else 1f
        canvas.save()
        canvas.scale(scale, scale, previousButtonRect.centerX(), previousButtonRect.centerY())
        paint.color = Color.argb((156 * phase).toInt(), 5, 7, 7)
        canvas.drawRoundRect(previousButtonRect, 12f * density, 12f * density, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f * density
        paint.color = Color.argb((80 * phase).toInt(), 193, 255, 0)
        canvas.drawRoundRect(previousButtonRect, 12f * density, 12f * density, paint)
        paint.style = Paint.Style.FILL

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = 2.4f * density
        paint.color = primaryFixed
        val cy = previousButtonRect.centerY()
        val ax = previousButtonRect.left + 26f * density
        canvas.drawLine(ax + 14f * density, cy, ax - 2f * density, cy, paint)
        path.reset()
        path.moveTo(ax + 4f * density, cy - 7f * density)
        path.lineTo(ax - 3f * density, cy)
        path.lineTo(ax + 4f * density, cy + 7f * density)
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL

        textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        textPaint.textSize = 14f * density
        textPaint.color = primaryFixed
        textPaint.alpha = (255 * phase).toInt()
        canvas.drawText("Prev", previousButtonRect.left + 44f * density, cy + 5f * density, textPaint)
        canvas.restore()
        textPaint.alpha = 255
    }

    private fun drawArrow(canvas: Canvas, cx: Float, cy: Float, density: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = 2.8f * density
        paint.color = onPrimary
        canvas.drawLine(cx - 9f * density, cy, cx + 9f * density, cy, paint)
        path.reset()
        path.moveTo(cx + 2f * density, cy - 7f * density)
        path.lineTo(cx + 9f * density, cy)
        path.lineTo(cx + 2f * density, cy + 7f * density)
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
    }

    private fun phase(delayMs: Long, durationMs: Long): Float {
        if (isDrawingOldStep) return 1f
        val elapsed = android.os.SystemClock.uptimeMillis() - startTimeMs - delayMs
        val raw = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
        return 1f - (1f - raw) * (1f - raw) * (1f - raw)
    }

    private fun scaleFromSmall(delayMs: Long, durationMs: Long): Float {
        if (isDrawingOldStep) return 1f
        val elapsed = android.os.SystemClock.uptimeMillis() - startTimeMs - delayMs
        val raw = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
        val eased = 1f - (1f - raw) * (1f - raw) * (1f - raw)
        val pop = sin(raw * Math.PI).toFloat() * 0.08f
        return 0.62f + eased * 0.38f + pop
    }

    private fun drawParticles(canvas: Canvas, w: Float, h: Float, dt: Float) {
        particles.forEachIndexed { index, particle ->
            particle.x += particle.speedX * dt
            particle.y += particle.speedY * dt
            if (particle.x < -8f || particle.x > w + 8f || particle.y < -8f || particle.y > h + 8f) {
                val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
                particle.x = w / 2f + cos(angle) * w * 0.56f
                particle.y = h / 2f + sin(angle) * h * 0.56f
            }
            val pulse = ((sin(time * 1.7f + index) + 1f) * 0.5f)
            paint.color = Color.argb((particle.alpha * (0.55f + pulse * 0.45f)).toInt(), 193, 255, 0)
            canvas.drawCircle(particle.x, particle.y, particle.radius, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                buttonPressed = nextButtonRect.contains(event.x, event.y)
                previousPressed = step > 1 && previousButtonRect.contains(event.x, event.y)
                if (buttonPressed || previousPressed) invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                val wasPressed = buttonPressed
                val wasPreviousPressed = previousPressed
                buttonPressed = false
                previousPressed = false
                invalidate()
                if (wasPreviousPressed && previousButtonRect.contains(event.x, event.y)) {
                    if (step > 1) {
                        oldStep = step
                        step -= 1
                        transitionStartMs = android.os.SystemClock.uptimeMillis()
                        startTimeMs = transitionStartMs
                        performClick()
                    }
                    return true
                }
                if (wasPressed && nextButtonRect.contains(event.x, event.y)) {
                    if (step < 4) {
                        oldStep = step
                        step += 1
                        transitionStartMs = android.os.SystemClock.uptimeMillis()
                        startTimeMs = transitionStartMs
                        invalidate()
                    } else {
                        (context as? MainActivity)?.showSignupScreen()
                    }
                    performClick()
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                buttonPressed = false
                previousPressed = false
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
