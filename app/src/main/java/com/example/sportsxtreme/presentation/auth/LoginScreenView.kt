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
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.method.PasswordTransformationMethod
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import kotlin.math.max
import kotlin.math.min

class LoginScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val primaryFixed = Color.rgb(193, 255, 0)
    private val onPrimary = Color.rgb(24, 34, 0)
    private val splashTitleBlue = Color.rgb(0, 127, 255)
    private val panelColor = Color.argb(186, 4, 7, 7)

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        setBackgroundColor(Color.BLACK)
        addView(BackgroundLayer(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(createContent(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun createContent(context: Context): View {
        val density = resources.displayMetrics.density
        val scrollView = ScrollView(context).apply {
            setFillViewport(true)
            clipToPadding = false
        }

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(32), dp(22), dp(28))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        content.addView(LogoMarkView(context), LinearLayout.LayoutParams(dp(98), dp(58)).apply {
            bottomMargin = dp(4)
        })
        content.addView(BrandWordmarkView(context), LinearLayout.LayoutParams(dp(210), dp(28)).apply {
            bottomMargin = dp(34)
        })

        content.addView(titleText("LOGIN", 34f, Color.WHITE).apply {
            setShadowLayer(3.5f * density, 2f * density, 2f * density, splashTitleBlue)
        }, fullWidthParams())

        content.addView(TextView(context).apply {
            text = context.getString(R.string.str_welcome_back_to_the_)
            setTextColor(Color.rgb(218, 229, 210))
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            includeFontPadding = false
        }, fullWidthParams(top = dp(8), bottom = dp(22)))

        emailInput = addField(content, "EMAIL", "ATHLETE@DOMAIN.COM", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        passwordInput = addField(content, "PASSWORD", "********", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD).apply {
            transformationMethod = PasswordTransformationMethod.getInstance()
        }

        content.addView(forgotPassword(context), fullWidthParams(bottom = dp(18), height = dp(24)))
        content.addView(primaryButton(context), fullWidthParams(bottom = dp(18), height = dp(52)))
        content.addView(divider(context), fullWidthParams(bottom = dp(16), height = dp(18)))
        content.addView(socialRow(context), fullWidthParams(bottom = dp(26), height = dp(44)))
        content.addView(signupPrompt(context), fullWidthParams(bottom = dp(8), height = dp(28)))
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))

        scrollView.addView(content)
        return scrollView
    }

    private fun addField(parent: LinearLayout, label: String, hint: String, inputType: Int): EditText {
        parent.addView(TextView(context).apply {
            text = label
            setTextColor(primaryFixed)
            textSize = 8.5f
            letterSpacing = 0.04f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
        }, fullWidthParams(bottom = dp(6)))

        val editText = EditText(context).apply {
            this.hint = hint
            this.inputType = inputType
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(76, 86, 88))
            textSize = 13f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
            setPadding(dp(12), 0, dp(12), 0)
            background = fieldBackground(false)
            setOnFocusChangeListener { view, hasFocus ->
                view.background = fieldBackground(hasFocus)
            }
        }

        parent.addView(editText, fullWidthParams(bottom = dp(14), height = dp(42)))
        return editText
    }

    private fun forgotPassword(context: Context): TextView {
        return TextView(context).apply {
            text = context.getString(R.string.str_forgot_password)
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setTextColor(primaryFixed)
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
            setOnClickListener {
                Toast.makeText(context, "Forgot password tapped", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun primaryButton(context: Context): TextView {
        return TextView(context).apply {
            text = context.getString(R.string.str_login)
            gravity = Gravity.CENTER
            setTextColor(onPrimary)
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(7).toFloat()
                setColor(primaryFixed)
            }
            setOnClickListener {
                (context as? MainActivity)?.showSportSelectionScreen()
            }
        }
    }

    private fun divider(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            addView(lineView(context), LinearLayout.LayoutParams(0, dp(1), 1f))
            addView(TextView(context).apply {
                text = context.getString(R.string.str_or_continue_with)
                setTextColor(Color.rgb(108, 118, 101))
                textSize = 8f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
                gravity = Gravity.CENTER
                setPadding(dp(12), 0, dp(12), 0)
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT))
            addView(lineView(context), LinearLayout.LayoutParams(0, dp(1), 1f))
        }
    }

    private fun lineView(context: Context): View {
        return View(context).apply {
            setBackgroundColor(Color.argb(66, 193, 255, 0))
        }
    }

    private fun socialRow(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            addView(socialButton(context, "GOOGLE", R.drawable.googleicon), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                rightMargin = dp(6)
            })
            addView(socialButton(context, "APPLE", null), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                leftMargin = dp(6)
            })
        }
    }

    private fun socialButton(context: Context, label: String, iconRes: Int?): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(6).toFloat()
                setColor(Color.argb(224, 22, 22, 23))
                setStroke(dp(1), Color.argb(58, 255, 255, 255))
            }

            if (iconRes != null) {
                addView(ImageView(context).apply {
                    setImageResource(iconRes)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    contentDescription = "$label icon"
                }, LinearLayout.LayoutParams(dp(14), dp(14)).apply {
                    rightMargin = dp(8)
                })
            } else {
                addView(TextView(context).apply {
                    text = context.getString(R.string.str_a)
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    textSize = 9f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                }, LinearLayout.LayoutParams(dp(14), dp(14)).apply {
                    rightMargin = dp(8)
                })
            }

            addView(TextView(context).apply {
                text = label
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                textSize = 8f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun signupPrompt(context: Context): TextView {
        val text = SpannableString(context.getString(R.string.str_new_to_sportsxtreme_))
        val start = text.indexOf(context.getString(R.string.str_sign_up))
        text.setSpan(ForegroundColorSpan(primaryFixed), start, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(StyleSpan(Typeface.BOLD_ITALIC), start, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return TextView(context).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(242, 244, 215))
            textSize = 11f
            includeFontPadding = false
            setOnClickListener {
                (context as? MainActivity)?.showSignupScreen()
            }
        }
    }

    private fun titleText(text: String, size: Float, color: Int): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(color)
            textSize = size
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
        }
    }

    private fun fullWidthParams(top: Int = 0, bottom: Int = 0, height: Int = LinearLayout.LayoutParams.WRAP_CONTENT): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height).apply {
            topMargin = top
            bottomMargin = bottom
        }
    }

    private fun fieldBackground(focused: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(5).toFloat()
            setColor(if (focused) Color.argb(222, 7, 13, 13) else panelColor)
            setStroke(dp(1), if (focused) primaryFixed else Color.argb(58, 255, 255, 255))
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
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
                    intArrayOf(Color.argb(232, 0, 0, 0), Color.argb(218, 0, 5, 4), Color.BLACK),
                    floatArrayOf(0f, 0.56f, 1f), Shader.TileMode.CLAMP
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
            val wf = w.toFloat()
            val hf = h.toFloat()
            if (wf > 0 && hf > 0) {
                val cx = wf / 2f
                val cy = hf / 2f
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
            val w = width.toFloat()
            val h = height.toFloat()
            val cx = w / 2f
            val cy = h / 2f

            paint.shader = logoShader
            bounds.set(cx - 52f * density, cy - 6f * density, cx + 52f * density, cy + 6f * density)
            canvas.drawRoundRect(bounds, 6f * density, 6f * density, paint)
            paint.shader = null
            paint.color = Color.argb(172, 193, 255, 0)
            bounds.set(cx - 34f * density, cy + 20f * density, cx + 34f * density, cy + 22f * density)
            canvas.drawRoundRect(bounds, 1f * density, 1f * density, paint)

            val bitmap = appIcon ?: return
            if (source.isEmpty) return
            val iconWidth = min(w * 0.86f, 86f * density)
            val iconHeight = iconWidth * source.height() / source.width()
            bounds.set(cx - iconWidth / 2f, cy - iconHeight / 2f, cx + iconWidth / 2f, cy + iconHeight / 2f)
            canvas.drawBitmap(bitmap, source, bounds, bitmapPaint)
        }
    }

    private inner class BrandWordmarkView(context: Context) : View(context) {
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
            paint.color = splashTitleBlue
            canvas.drawText(x, left, baseline, paint)
            left += paint.measureText(x) * 0.93f
            canvas.drawText(treme, left, baseline, paint)
        }
    }
}
