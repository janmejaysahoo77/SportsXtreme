package com.example.sportsxtreme.presentation.auth

import android.app.Activity
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
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.di.AuthDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class PhooneNumberAfterGoogleLogin : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthDependencies.bindPhoneAuthActivity(this)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.black)
        setContent {
            AndroidView(
                factory = { context ->
                    PhooneNumberAfterGoogleLoginView(context) { phoneNumber ->
                        showOtpVerificationScreen(phoneNumber)
                    }
                }
            )
        }
    }

    private fun showOtpVerificationScreen(phoneNumber: String) {
        setContent {
            AndroidView(
                factory = { context ->
                    OtpVerificationScreenView(context, phoneNumber)
                }
            )
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}

private class PhooneNumberAfterGoogleLoginView(
    context: Context,
    private val onNext: (String) -> Unit
) : FrameLayout(context) {

    private val neonGreen = Color.rgb(193, 255, 0)
    private val electricBlue = Color.rgb(0, 127, 255)
    private val muted = Color.rgb(185, 199, 190)
    private lateinit var phoneInput: EditText
    private lateinit var errorView: TextView
    private lateinit var nextButton: TextView
    private val authViewModel = AuthDependencies.authViewModel()
    private val phoneScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isSendingOtp = false

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        setBackgroundColor(Color.BLACK)
        addView(BackgroundLayer(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(createContent(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        post {
            phoneInput.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(phoneInput, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun createContent(context: Context): View {
        val scrollView = ScrollView(context).apply {
            setFillViewport(true)
            clipToPadding = false
        }

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(28), dp(22), dp(28))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        content.addView(topRail(context), fullWidthParams(bottom = dp(28), height = dp(42)))
        content.addView(PhoneSignalView(context), LinearLayout.LayoutParams(dp(148), dp(148)).apply {
            bottomMargin = dp(22)
        })
        content.addView(title("ADD PHONE", 31f, Color.WHITE).apply {
            gravity = Gravity.CENTER
            setShadowLayer(3.5f * resources.displayMetrics.density, 2f, 2f, electricBlue)
        }, fullWidthParams())
        content.addView(TextView(context).apply {
            text = "ONE MORE STEP TO SECURE YOUR GOOGLE SIGNUP"
            gravity = Gravity.CENTER
            setTextColor(muted)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
        }, fullWidthParams(top = dp(10), bottom = dp(24)))
        content.addView(statusStrip(context), fullWidthParams(bottom = dp(22), height = dp(34)))
        content.addView(label("PHONE NUMBER"), fullWidthParams(bottom = dp(7)))
        phoneInput = phoneField(context)
        content.addView(phoneInput, fullWidthParams(bottom = dp(12), height = dp(52)))

        errorView = TextView(context).apply {
            visibility = View.INVISIBLE
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(255, 112, 112))
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            includeFontPadding = false
        }
        content.addView(errorView, fullWidthParams(bottom = dp(12), height = dp(18)))

        nextButton = primaryButton(context)
        content.addView(nextButton, fullWidthParams(bottom = dp(16), height = dp(52)))
        content.addView(backButton(context), fullWidthParams(height = dp(30)))
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))

        scrollView.addView(content)
        return scrollView
    }

    private fun topRail(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = panelBackground(Color.argb(154, 4, 9, 12), Color.argb(76, 0, 127, 255), dp(10))
            addView(TextView(context).apply {
                text = "SPORTSXTREME"
                setTextColor(Color.WHITE)
                textSize = 13f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
            })
            addView(TextView(context).apply {
                text = " GOOGLE SIGNUP"
                setTextColor(neonGreen)
                textSize = 12f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
            })
        }
    }

    private fun statusStrip(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = panelBackground(Color.argb(170, 12, 17, 18), Color.argb(92, 193, 255, 0), dp(8))
            addView(dot(context), LinearLayout.LayoutParams(dp(8), dp(8)).apply {
                rightMargin = dp(8)
            })
            addView(TextView(context).apply {
                text = "PHONE OTP VERIFICATION REQUIRED"
                setTextColor(Color.rgb(226, 238, 229))
                textSize = 9f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
            })
        }
    }

    private fun phoneField(context: Context): EditText {
        return EditText(context).apply {
            hint = "+91 98765 43210"
            inputType = InputType.TYPE_CLASS_PHONE
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.rgb(82, 94, 96))
            textSize = 16f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
            setPadding(dp(14), 0, dp(14), 0)
            background = fieldBackground(false)
            setOnFocusChangeListener { view, hasFocus ->
                view.background = fieldBackground(hasFocus)
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    clearError()
                    refreshNextState()
                }
            })
        }
    }

    private fun primaryButton(context: Context): TextView {
        return TextView(context).apply {
            text = "NEXT  ->"
            gravity = Gravity.CENTER
            isEnabled = false
            alpha = 0.62f
            setTextColor(Color.rgb(18, 26, 0))
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(neonGreen, Color.rgb(112, 236, 255))
            ).apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(8).toFloat()
            }
            setOnClickListener {
                submitPhoneNumber()
            }
        }
    }

    private fun backButton(context: Context): TextView {
        return TextView(context).apply {
            text = "BACK TO SIGN UP"
            gravity = Gravity.CENTER
            setTextColor(electricBlue)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
            setShadowLayer(2.5f * resources.displayMetrics.density, 0f, 0f, Color.argb(190, 0, 127, 255))
            setOnClickListener {
                (context as? Activity)?.finish()
            }
        }
    }

    private fun submitPhoneNumber() {
        if (isSendingOtp) return
        val phoneNumber = phoneInput.text.toString().trim()
        if (!isValidPhoneNumber(phoneNumber)) {
            showError("Enter +91XXXXXXXXXX or a 10 digit phone number")
            return
        }
        val activity = context as? Activity
        if (activity == null) {
            showError("Phone verification needs an active screen.")
            return
        }
        AuthDependencies.bindPhoneAuthActivity(activity)
        setSending(true)
        phoneScope.launch {
            when (val result = authViewModel.sendPhoneOtp(phoneNumber)) {
                is Resource.Success -> {
                    clearError()
                    val session = result.data
                    if (session?.isAutoVerified == true) {
                        onNext(session.phoneNumber)
                    } else {
                        onNext(session?.phoneNumber ?: phoneNumber)
                    }
                }
                is Resource.Error -> showError(result.message ?: "Could not send OTP")
                is Resource.Loading -> Unit
            }
            setSending(false)
        }
    }

    private fun refreshNextState() {
        val ready = isValidPhoneNumber(phoneInput.text.toString())
        nextButton.isEnabled = ready
        nextButton.alpha = if (ready) 1f else 0.62f
    }

    private fun setSending(sending: Boolean) {
        isSendingOtp = sending
        nextButton.isEnabled = !sending && isValidPhoneNumber(phoneInput.text.toString())
        nextButton.alpha = if (nextButton.isEnabled) 1f else 0.62f
        nextButton.text = if (sending) "SENDING OTP..." else "NEXT  ->"
    }

    private fun isValidPhoneNumber(value: String): Boolean {
        val digits = value.count { it.isDigit() }
        return digits in 10..15 && PhoneNumberUtils.isGlobalPhoneNumber(value.filter { it.isDigit() || it == '+' })
    }

    private fun clearError() {
        errorView.visibility = View.INVISIBLE
        errorView.text = ""
    }

    private fun showError(message: String) {
        errorView.text = message
        errorView.visibility = View.VISIBLE
    }

    override fun onDetachedFromWindow() {
        phoneScope.cancel()
        super.onDetachedFromWindow()
    }

    private fun title(text: String, size: Float, color: Int): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(color)
            textSize = size
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
        }
    }

    private fun label(text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(neonGreen)
            textSize = 8.5f
            letterSpacing = 0.04f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
        }
    }

    private fun dot(context: Context): View {
        return View(context).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(neonGreen)
            }
        }
    }

    private fun fieldBackground(focused: Boolean): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.argb(235, 13, 20, 24), Color.argb(216, 2, 5, 7))
        ).apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(9).toFloat()
            setStroke(dp(if (focused) 2 else 1), if (focused) neonGreen else Color.argb(72, 255, 255, 255))
        }
    }

    private fun panelBackground(fill: Int, stroke: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(fill)
            setStroke(dp(1), stroke)
        }
    }

    private fun fullWidthParams(top: Int = 0, bottom: Int = 0, height: Int = LinearLayout.LayoutParams.WRAP_CONTENT): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height).apply {
            topMargin = top
            bottomMargin = bottom
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
    }

    private class PhoneSignalView(context: Context) : View(context) {
        private val appIcon: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.appicon)
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val rect = RectF()
        private var startTimeMs = android.os.SystemClock.uptimeMillis()

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            startTimeMs = android.os.SystemClock.uptimeMillis()
            postInvalidateOnAnimation()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val density = resources.displayMetrics.density
            val w = width.toFloat()
            val h = height.toFloat()
            val cx = w / 2f
            val cy = h / 2f
            val pulse = (sin((android.os.SystemClock.uptimeMillis() - startTimeMs) / 360f) + 1f) / 2f

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.4f * density
            paint.color = Color.argb((80 + pulse * 82).toInt(), 0, 127, 255)
            rect.set(cx - 60f * density, cy - 60f * density, cx + 60f * density, cy + 60f * density)
            canvas.drawRoundRect(rect, 28f * density, 28f * density, paint)

            paint.strokeWidth = 0.9f * density
            paint.color = Color.argb((110 + pulse * 96).toInt(), 193, 255, 0)
            rect.set(cx - 52f * density, cy - 52f * density, cx + 52f * density, cy + 52f * density)
            canvas.drawRoundRect(rect, 23f * density, 23f * density, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f * density
            paint.color = Color.argb(214, 193, 255, 0)
            rect.set(cx - 25f * density, cy - 38f * density, cx + 25f * density, cy + 38f * density)
            canvas.drawRoundRect(rect, 10f * density, 10f * density, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.argb(96, 112, 236, 255)
            rect.set(cx - 11f * density, cy + 28f * density, cx + 11f * density, cy + 30f * density)
            canvas.drawRoundRect(rect, 1f * density, 1f * density, paint)

            appIcon?.let { bitmap ->
                val logoSize = min(w, h) * 0.24f
                rect.set(cx - logoSize / 2f, cy - logoSize / 2f, cx + logoSize / 2f, cy + logoSize / 2f)
                canvas.drawBitmap(bitmap, null, rect, bitmapPaint)
            }

            postInvalidateOnAnimation()
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
                    intArrayOf(Color.argb(232, 0, 2, 4), Color.argb(226, 0, 8, 10), Color.BLACK),
                    floatArrayOf(0f, 0.58f, 1f),
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
                bitmapPaint.alpha = 88
                canvas.drawBitmap(bitmap, null, rect, bitmapPaint)
                bitmapPaint.alpha = 255
            }

            paint.shader = bgShader
            canvas.drawRect(0f, 0f, w, h, paint)
            paint.shader = null

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.75f * density
            val grid = 30f * density
            paint.color = Color.argb(34, 0, 127, 255)
            var x = (time * 10f * density) % grid - grid
            while (x < w + grid) {
                canvas.drawLine(x, 0f, x - w * 0.18f, h, paint)
                x += grid
            }
            paint.color = Color.argb(32, 193, 255, 0)
            var y = (time * 8f * density) % grid - grid
            while (y < h + grid) {
                canvas.drawLine(0f, y, w, y + h * 0.08f, paint)
                y += grid
            }

            paint.style = Paint.Style.FILL
            paint.color = Color.rgb(3, 6, 8)
            rect.set(w * 0.06f, h * 0.28f, w * 0.94f, min(h * 0.92f, h - 24f * density))
            paint.alpha = 48
            canvas.drawRoundRect(rect, 18f * density, 18f * density, paint)
            paint.alpha = 255

            postInvalidateOnAnimation()
        }
    }
}
