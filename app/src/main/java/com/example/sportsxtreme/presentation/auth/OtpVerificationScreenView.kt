package com.example.sportsxtreme.presentation.auth

import android.content.Context
import android.app.Activity
import android.content.Intent
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
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
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
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.common.WindowInsetsUtils
import com.example.sportsxtreme.data.di.AuthDependencies
import com.example.sportsxtreme.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

import com.example.sportsxtreme.presentation.components.AuthBackgroundView

class OtpVerificationScreenView @JvmOverloads constructor(
    context: Context,
    private val contact: String = "",
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val neonGreen = Color.rgb(193, 255, 0)
    private val electricBlue = Color.rgb(0, 127, 255)
    private val ink = Color.rgb(3, 6, 8)
    private val muted = Color.rgb(184, 197, 189)
    private val panelColor = Color.argb(186, 4, 7, 7)
    private val otpInputs = mutableListOf<EditText>()
    private lateinit var verifyButton: TextView
    private lateinit var errorView: TextView
    private lateinit var resendView: TextView
    private val authViewModel = AuthDependencies.authViewModel()
    private val otpScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var resendCount = 0
    private var isVerifying = false

    init {
        (context as? Activity)?.let { AuthDependencies.bindPhoneAuthActivity(it) }
        isFocusable = true
        isFocusableInTouchMode = true
        setBackgroundColor(Color.BLACK)
        addView(AuthBackgroundView(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(createContent(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        post {
            otpInputs.firstOrNull()?.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(otpInputs.firstOrNull(), InputMethodManager.SHOW_IMPLICIT)
            verifyAutomaticallyIfReady(context)
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
            setPadding(dp(22), dp(16), dp(22), dp(28))
            WindowInsetsUtils.applySystemBarsPadding(this, applyBottom = false)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        content.addView(topRail(context), fullWidthParams(bottom = dp(28), height = dp(42)))
        content.addView(AppLogoCoreView(context), LinearLayout.LayoutParams(dp(148), dp(148)).apply {
            bottomMargin = dp(24)
        })
        content.addView(title("VERIFY OTP", 31f, Color.WHITE).apply {
            setShadowLayer(3.5f * resources.displayMetrics.density, 2f, 2f, electricBlue)
            gravity = Gravity.CENTER
        }, fullWidthParams())
        content.addView(TextView(context).apply {
            text = if (contact.isBlank()) {
                "ENTER THE 6 DIGIT ACCESS CODE"
            } else {
                "CODE SENT TO ${contact.uppercase()}"
            }
            gravity = Gravity.CENTER
            setTextColor(muted)
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
        }, fullWidthParams(top = dp(10), bottom = dp(24)))
        content.addView(statusStrip(context), fullWidthParams(bottom = dp(20), height = dp(34)))
        content.addView(otpRow(context), fullWidthParams(bottom = dp(16), height = dp(58)))

        errorView = TextView(context).apply {
            visibility = View.INVISIBLE
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(255, 112, 112))
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            includeFontPadding = false
        }
        content.addView(errorView, fullWidthParams(bottom = dp(12), height = dp(18)))

        verifyButton = primaryButton(context)
        content.addView(verifyButton, fullWidthParams(bottom = dp(16), height = dp(52)))
        content.addView(resendRow(context), fullWidthParams(bottom = dp(20), height = dp(34)))
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
                text = " USER VERIFICATION"
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

            addView(dot(context, neonGreen), LinearLayout.LayoutParams(dp(8), dp(8)).apply {
                rightMargin = dp(8)
            })
            addView(TextView(context).apply {
                text = "SECURE SESSION ACTIVE"
                setTextColor(Color.rgb(226, 238, 229))
                textSize = 9f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
            })
        }
    }

    private fun otpRow(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            repeat(6) { index ->
                val input = otpInput(context, index)
                otpInputs += input
                addView(input, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                    if (index != 5) rightMargin = dp(7)
                })
            }
        }
    }

    private fun otpInput(context: Context, index: Int): EditText {
        return EditText(context).apply {
            gravity = Gravity.CENTER
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(1))
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.argb(86, 255, 255, 255))
            textSize = 22f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
            background = otpBackground(false)
            setPadding(0, 0, 0, 0)
            setOnFocusChangeListener { view, hasFocus ->
                view.background = otpBackground(hasFocus)
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    clearError()
                    if (s?.length == 1 && index < otpInputs.lastIndex) {
                        otpInputs[index + 1].requestFocus()
                    }
                    refreshVerifyState()
                }
            })
        }
    }

    private fun primaryButton(context: Context): TextView {
        return TextView(context).apply {
            verifyButton = this
            text = "CONTINUE"
            gravity = Gravity.CENTER
            isEnabled = true
            alpha = 1f
            setTextColor(Color.rgb(18, 26, 0))
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(7).toFloat()
                setColor(neonGreen)
            }
            setOnClickListener {
                verifyOtp(context)
            }
        }
    }

    private fun resendRow(context: Context): View {
        return LinearLayout(context).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
            addView(TextView(context).apply {
                text = "DIDN'T GET IT? "
                setTextColor(Color.rgb(155, 166, 157))
                textSize = 10.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
            })
            resendView = TextView(context).apply {
                text = "RESEND CODE"
                setTextColor(neonGreen)
                textSize = 10.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
                setOnClickListener {
                    resendOtp()
                }
            }
            addView(resendView)
        }
    }

    private fun backButton(context: Context): TextView {
        return TextView(context).apply {
            text = "BACK TO PHONE AUTH"
            gravity = Gravity.CENTER
            setTextColor(electricBlue)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
            setShadowLayer(2.5f * resources.displayMetrics.density, 0f, 0f, Color.argb(190, 0, 127, 255))
            setOnClickListener {
                val mainActivity = context as? MainActivity
                if (mainActivity != null) {
                    mainActivity.showPhoneAuthScreen()
                } else {
                    (context as? Activity)?.finish()
                }
            }
        }
    }

    private fun verifyOtp(context: Context) {
        if (isVerifying) return
        val code = otpInputs.joinToString(separator = "") { it.text.toString() }
        if (code.length != 6) {
            showError("Enter the complete 6 digit code")
            return
        }
        setVerifying(true)
        otpScope.launch {
            when (val result = authViewModel.verifyPhoneOtp(code)) {
                is Resource.Success -> {
                    showMessage("Verification complete.")
                    if (result.data == true) {
                        (context as? MainActivity)?.showSignupScreen(contact)
                    } else {
                        navigateAfterVerification(context)
                    }
                }
                is Resource.Error -> {
                    showError(result.message ?: "OTP verification failed")
                }
                is Resource.Loading -> Unit
            }
            setVerifying(false)
        }
    }

    private fun verifyAutomaticallyIfReady(context: Context) {
        val autoVerified = authViewModel.state.value.pendingPhoneSession?.isAutoVerified == true
        if (!autoVerified) return
        setVerifying(true)
        showMessage("Phone verified automatically.")
        otpScope.launch {
            when (val result = authViewModel.verifyPhoneOtp("")) {
                is Resource.Success -> {
                    if (result.data == true) {
                        (context as? MainActivity)?.showSignupScreen(contact)
                    } else {
                        navigateAfterVerification(context)
                    }
                }
                is Resource.Error -> showError(result.message ?: "Automatic verification failed")
                is Resource.Loading -> Unit
            }
            setVerifying(false)
        }
    }

    private fun resendOtp() {
        if (isVerifying) return
        setVerifying(true, label = "RESENDING...")
        otpScope.launch {
            when (val result = authViewModel.resendPhoneOtp()) {
                is Resource.Success -> {
                    resendCount += 1
                    resendView.text = "CODE RESENT ${resendCount}X"
                    clearOtp()
                    if (result.data?.isAutoVerified == true) {
                        showMessage("Phone verified automatically.")
                    } else {
                        showMessage("New code request queued.")
                    }
                }
                is Resource.Error -> showError(result.message ?: "Could not resend OTP")
                is Resource.Loading -> Unit
            }
            setVerifying(false)
        }
    }

    private fun navigateAfterVerification(context: Context) {
        postDelayed({
            val mainActivity = context as? MainActivity
            if (mainActivity != null) {
                mainActivity.showSportSelectionScreen()
            } else {
                context.startActivity(
                    Intent(context, MainActivity::class.java)
                        .putExtra(MainActivity.EXTRA_START_DESTINATION, MainActivity.DESTINATION_SPORT_SELECTION)
                )
                (context as? Activity)?.finish()
            }
        }, 420L)
    }

    private fun refreshVerifyState() {
        val ready = otpInputs.all { it.text.length == 1 }
        verifyButton.isEnabled = ready
        verifyButton.alpha = if (ready) 1f else 0.62f
    }

    private fun setVerifying(verifying: Boolean, label: String = "VERIFYING...") {
        isVerifying = verifying
        verifyButton.text = if (verifying) label else "VERIFY ACCOUNT  ->"
        verifyButton.isEnabled = !verifying && otpInputs.all { it.text.length == 1 }
        verifyButton.alpha = if (verifyButton.isEnabled) 1f else 0.62f
        resendView.isEnabled = !verifying
        resendView.alpha = if (verifying) 0.62f else 1f
    }

    private fun clearOtp() {
        otpInputs.forEach { it.text.clear() }
        otpInputs.firstOrNull()?.requestFocus()
        refreshVerifyState()
    }

    private fun clearError() {
        errorView.visibility = View.INVISIBLE
        errorView.text = ""
    }

    private fun showError(message: String) {
        errorView.setTextColor(Color.rgb(255, 112, 112))
        errorView.text = message
        errorView.visibility = View.VISIBLE
    }

    private fun showMessage(message: String) {
        errorView.setTextColor(neonGreen)
        errorView.text = message
        errorView.visibility = View.VISIBLE
    }

    override fun onDetachedFromWindow() {
        otpScope.cancel()
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

    private fun dot(context: Context, color: Int): View {
        return View(context).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
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

    private fun otpBackground(focused: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(6).toFloat()
            setColor(if (focused) Color.argb(222, 7, 13, 13) else panelColor)
            setStroke(dp(1), if (focused) neonGreen else Color.argb(58, 255, 255, 255))
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

    private class AppLogoCoreView(context: Context) : View(context) {
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
            val elapsed = android.os.SystemClock.uptimeMillis() - startTimeMs
            val pulse = (sin(elapsed / 360f) + 1f) / 2f
            val scannerPhase = (elapsed % 1800L) / 1800f
            val scannerProgress = if (scannerPhase < 0.5f) scannerPhase * 2f else (1f - scannerPhase) * 2f

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.4f * density
            paint.color = Color.argb((80 + pulse * 82).toInt(), 0, 127, 255)
            rect.set(cx - 60f * density, cy - 60f * density, cx + 60f * density, cy + 60f * density)
            canvas.drawRoundRect(rect, 28f * density, 28f * density, paint)

            paint.strokeWidth = 0.9f * density
            paint.color = Color.argb((110 + pulse * 96).toInt(), 193, 255, 0)
            rect.set(cx - 52f * density, cy - 52f * density, cx + 52f * density, cy + 52f * density)
            canvas.drawRoundRect(rect, 23f * density, 23f * density, paint)

            paint.color = Color.argb((120 + pulse * 76).toInt(), 193, 255, 0)
            rect.set(cx - 42f * density, cy - 42f * density, cx + 42f * density, cy + 42f * density)
            canvas.drawOval(rect, paint)

            val logoSize = min(w, h) * 0.52f
            val logoLeft = cx - logoSize / 2f
            val logoTop = cy - logoSize / 2f
            val logoRight = cx + logoSize / 2f
            val logoBottom = cy + logoSize / 2f
            appIcon?.let { bitmap ->
                rect.set(logoLeft, logoTop, logoRight, logoBottom)
                canvas.drawBitmap(bitmap, null, rect, bitmapPaint)
            }

            val scanY = logoTop + logoSize * scannerProgress
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                logoLeft,
                scanY - 10f * density,
                logoLeft,
                scanY + 10f * density,
                intArrayOf(Color.TRANSPARENT, Color.argb(74, 0, 127, 255), Color.argb(132, 193, 255, 0), Color.TRANSPARENT),
                floatArrayOf(0f, 0.36f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            rect.set(logoLeft - 8f * density, scanY - 10f * density, logoRight + 8f * density, scanY + 10f * density)
            canvas.drawRoundRect(rect, 10f * density, 10f * density, paint)
            paint.shader = null

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f * density
            paint.color = Color.argb(230, 193, 255, 0)
            canvas.drawLine(logoLeft - 10f * density, scanY, logoRight + 10f * density, scanY, paint)
            paint.strokeWidth = 0.8f * density
            paint.color = Color.argb(210, 112, 236, 255)
            canvas.drawLine(logoLeft - 5f * density, scanY - 4f * density, logoRight + 5f * density, scanY - 4f * density, paint)

            paint.strokeWidth = 1.5f * density
            paint.color = Color.argb(190, 0, 127, 255)
            rect.set(logoLeft - 8f * density, logoTop - 8f * density, logoRight + 8f * density, logoBottom + 8f * density)
            canvas.drawRoundRect(rect, 14f * density, 14f * density, paint)

            paint.strokeWidth = 1f * density
            paint.color = Color.argb(96, 255, 255, 255)
            canvas.drawLine(cx - 76f * density, cy, cx - 54f * density, cy, paint)
            canvas.drawLine(cx + 54f * density, cy, cx + 76f * density, cy, paint)
            canvas.drawLine(cx, cy - 76f * density, cx, cy - 54f * density, paint)
            canvas.drawLine(cx, cy + 54f * density, cx, cy + 76f * density, paint)

            postInvalidateOnAnimation()
        }
    }
}
