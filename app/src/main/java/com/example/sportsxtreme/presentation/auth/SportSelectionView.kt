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
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.max

class SportSelectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private data class SportItem(
        val name: String,
        val subtitle: String,
        val imageRes: Int
    )

    private val primaryFixed = Color.rgb(193, 255, 0)
    private var selectedSport: SportItem? = null
    private var selectedCard: FrameLayout? = null
    private var selectedCheck: TextView? = null
    private lateinit var chooseButtonView: TextView
    private val sports = listOf(
        SportItem("CRICKET", "BATS, BOWLS, GLORY", R.drawable.cricket_choosesports),
        SportItem("FOOTBALL", "PACE, PRESS, FINISH", R.drawable.football_choosesports),
        SportItem("BADMINTON", "REFLEX AND RALLY", R.drawable.badminton_choosesports),
        SportItem("VOLLEYBALL", "COURT CONTROL", R.drawable.volley_choosesports),
        SportItem("TENNIS", "SERVE THE EDGE", R.drawable.tennis_choosesports),
        SportItem("KABADDI", "RAID THE ARENA", R.drawable.kabaddi_choosesports),
        SportItem("TABLE TENNIS", "FAST HANDS", R.drawable.tabletennis_choosessports),
        SportItem("ESPORTS", "DIGITAL DOMINANCE", R.drawable.esports_choosesports)
    )

    init {
        setBackgroundColor(Color.BLACK)
        addView(BackgroundLayer(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(createContent(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun createContent(context: Context): View {
        val scrollView = ScrollView(context).apply {
            setFillViewport(true)
            clipToPadding = false
        }

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(20), 0, dp(22))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        content.addView(header(context), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            leftMargin = dp(14)
            rightMargin = dp(14)
            bottomMargin = dp(18)
        })

        val grid = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        sports.forEach { sport ->
            grid.addView(sportCard(context, sport), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(160)).apply {
                bottomMargin = dp(10)
            })
        }

        content.addView(grid)
        content.addView(chooseButton(context), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(50)).apply {
            leftMargin = dp(14)
            rightMargin = dp(14)
            topMargin = dp(2)
        })

        scrollView.addView(content)
        return scrollView
    }

    private fun header(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply {
                text = context.getString(R.string.str_choose_your_arena)
                setTextColor(primaryFixed)
                textSize = 19f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
                setShadowLayer(dp(2).toFloat(), 0f, 0f, Color.argb(150, 193, 255, 0))
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(TextView(context).apply {
                text = context.getString(R.string.str_select_the_battlefie)
                setTextColor(Color.rgb(124, 132, 126))
                textSize = 7.8f
                letterSpacing = 0.12f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(5)
            })
        }
    }

    private fun sportCard(context: Context, sport: SportItem): View {
        return FrameLayout(context).apply {
            clipToOutline = true
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 0f
                setColor(Color.rgb(6, 10, 11))
                setStroke(dp(1), Color.argb(56, 255, 255, 255))
            }
            foreground = cardStroke(false)
            isClickable = true
            isFocusable = true
            val check = selectedCheckView(context)

            addView(ImageView(context).apply {
                setImageResource(sport.imageRes)
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = sport.name
            }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

            addView(CardShadeView(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            addView(cardLabel(context, sport), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM).apply {
                leftMargin = dp(16)
                rightMargin = dp(16)
                bottomMargin = dp(16)
            })
            addView(check, LayoutParams(dp(27), dp(27), Gravity.TOP or Gravity.END).apply {
                topMargin = dp(16)
                rightMargin = dp(16)
            })

            setOnClickListener {
                selectSport(sport, this, check)
            }
        }
    }

    private fun selectedCheckView(context: Context): TextView {
        return TextView(context).apply {
            text = context.getString(R.string.str_u2713)
            visibility = View.GONE
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(9, 18, 5))
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(primaryFixed)
                setStroke(dp(1), Color.argb(190, 255, 255, 255))
            }
        }
    }

    private fun selectSport(sport: SportItem, card: FrameLayout, check: TextView) {
        selectedCard?.foreground = cardStroke(false)
        selectedCheck?.visibility = View.GONE

        selectedSport = sport
        selectedCard = card
        selectedCheck = check

        card.foreground = cardStroke(true)
        check.visibility = View.VISIBLE
        updateChooseButton(true)
    }

    private fun cardLabel(context: Context, sport: SportItem): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(context).apply {
                text = sport.name
                setTextColor(Color.WHITE)
                textSize = if (sport.name.length > 10) 18f else 24f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
                setShadowLayer(dp(3).toFloat(), dp(2).toFloat(), dp(2).toFloat(), Color.BLACK)
            })
            addView(TextView(context).apply {
                text = sport.subtitle
                setTextColor(Color.argb(178, 220, 228, 220))
                textSize = 10f
                letterSpacing = 0.09f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(4)
            })
        }
    }

    private fun chooseButton(context: Context): TextView {
        val text = SpannableString(context.getString(R.string.str_choose_sport))
        text.setSpan(ForegroundColorSpan(Color.rgb(18, 24, 18)), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(StyleSpan(Typeface.BOLD_ITALIC), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return TextView(context).apply {
            chooseButtonView = this
            this.text = text
            gravity = Gravity.CENTER
            textSize = 12f
            includeFontPadding = false
            setTextColor(Color.rgb(22, 24, 22))
            background = chooseButtonBackground(false)
            setOnClickListener {
                if (selectedSport != null) {
                    (context as? MainActivity)?.showHomeScreen()
                }
            }
        }
    }

    private fun updateChooseButton(active: Boolean) {
        chooseButtonView.background = chooseButtonBackground(active)
        chooseButtonView.setTextColor(if (active) Color.rgb(18, 24, 18) else Color.rgb(34, 38, 36))
    }

    private fun chooseButtonBackground(active: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(6).toFloat()
            setColor(if (active) primaryFixed else Color.rgb(83, 88, 86))
            setStroke(dp(1), if (active) Color.argb(170, 230, 255, 110) else Color.argb(70, 255, 255, 255))
        }
    }

    private fun cardStroke(selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 0f
            setColor(Color.TRANSPARENT)
            setStroke(
                if (selected) dp(2) else dp(1),
                if (selected) primaryFixed else Color.argb(38, 193, 255, 0)
            )
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
    }

    private class CardShadeView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var bgShader: Shader? = null

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            if (h > 0) {
                bgShader = LinearGradient(
                    0f, 0f, 0f, h.toFloat(),
                    intArrayOf(Color.argb(26, 255, 255, 255), Color.TRANSPARENT, Color.argb(220, 0, 0, 0)),
                    floatArrayOf(0f, 0.42f, 1f), Shader.TileMode.CLAMP
                )
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            paint.shader = bgShader
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.shader = null
            paint.color = Color.argb(54, 0, 0, 0)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }
    }

    private class BackgroundLayer(context: Context) : View(context) {
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
                    intArrayOf(Color.argb(230, 0, 0, 0), Color.argb(212, 0, 5, 4), Color.BLACK),
                    floatArrayOf(0f, 0.58f, 1f), Shader.TileMode.CLAMP
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
                bitmapPaint.alpha = 96
                canvas.drawBitmap(bitmap, null, rect, bitmapPaint)
                bitmapPaint.alpha = 255
            }

            paint.shader = bgShader
            canvas.drawRect(0f, 0f, w, h, paint)
            paint.shader = null

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.75f * density
            paint.color = Color.argb(28, 193, 255, 0)
            val grid = 34f * density
            var x = (time * 6f * density) % grid - grid
            while (x < w + grid) {
                canvas.drawLine(x, 0f, x - w * 0.1f, h, paint)
                x += grid
            }
            var y = (time * 4f * density) % grid - grid
            while (y < h + grid) {
                canvas.drawLine(0f, y, w, y + h * 0.06f, paint)
                y += grid
            }
            paint.color = Color.argb(32, 0, 127, 255)
            canvas.drawLine(-w * 0.12f, h * 0.48f, w * 0.95f, h * 0.14f, paint)
            paint.color = Color.argb(36, 193, 255, 0)
            canvas.drawLine(w * 0.16f, h * 0.94f, w * 1.08f, h * 0.62f, paint)
            paint.style = Paint.Style.FILL

            postInvalidateOnAnimation()
        }
    }
}
