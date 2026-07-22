package com.example.sportsxtreme.presentation.auth

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.di.AuthDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EmailVerificationScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val primaryFixed = Color.rgb(193, 255, 0)
    private val onPrimary = Color.rgb(24, 34, 0)
    private val panelColor = Color.argb(224, 12, 16, 16)
    private val authViewModel = AuthDependencies.authViewModel()
    private val screenScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var messageView: TextView
    private lateinit var verifiedButton: TextView
    private lateinit var resendButton: TextView
    private var isLoading = false
    private var resendCooldownSeconds = 0
    private var hasAutoCheckedAfterAttach = false

    init {
        setBackgroundColor(Color.BLACK)
        addView(createContent(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        postDelayed({
            autoCheckEmailVerification()
        }, AUTO_CHECK_DELAY_MS)
    }

    fun autoCheckEmailVerification() {
        if (!hasAutoCheckedAfterAttach) {
            hasAutoCheckedAfterAttach = true
            return
        }
        if (isLoading) return
        checkVerification(context, showUnverifiedMessage = false)
    }

    private fun createContent(context: Context): View {
        val scrollView = ScrollView(context).apply {
            setFillViewport(true)
            clipToPadding = false
        }
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(42), dp(24), dp(32))
        }

        content.addView(title("Verify your email"), fullWidthParams(bottom = dp(18)))
        content.addView(description(), fullWidthParams(bottom = dp(28)))
        messageView = message()
        content.addView(messageView, fullWidthParams(bottom = dp(18)))

        verifiedButton = primaryButton("I've Verified My Email") {
            checkVerification(context, showUnverifiedMessage = true)
        }
        content.addView(verifiedButton, fullWidthParams(bottom = dp(14), height = dp(52)))

        resendButton = secondaryButton("Resend Verification Email") {
            resendVerificationEmail()
        }
        content.addView(resendButton, fullWidthParams(bottom = dp(14), height = dp(50)))

        content.addView(secondaryButton("Back to Login") {
            (context as? MainActivity)?.showLoginScreen()
        }, fullWidthParams(height = dp(50)))

        scrollView.addView(content)
        return scrollView
    }

    private fun checkVerification(context: Context, showUnverifiedMessage: Boolean) {
        if (isLoading) return
        setLoading(true, "CHECKING...", "Resend Verification Email")
        screenScope.launch {
            when (val result = authViewModel.confirmEmailVerification()) {
                is Resource.Success -> {
                    showMessage(null)
                    (context as? MainActivity)?.showVerificationCompleteScreen()
                }
                is Resource.Error -> {
                    if (showUnverifiedMessage) {
                        showMessage(result.message ?: "Your email has not been verified yet.")
                    }
                }
                is Resource.Loading -> Unit
            }
            setLoading(false, "I've Verified My Email", "Resend Verification Email")
        }
    }

    private fun resendVerificationEmail() {
        if (isLoading || resendCooldownSeconds > 0) return
        setLoading(true, "I've Verified My Email", "SENDING...")
        screenScope.launch {
            when (val result = authViewModel.resendEmailVerification()) {
                is Resource.Success -> {
                    showMessage("Verification email sent. Please wait before requesting another link.")
                    startResendCooldown()
                }
                is Resource.Error -> showMessage(result.message ?: "Could not resend verification email")
                is Resource.Loading -> Unit
            }
            if (resendCooldownSeconds == 0) {
                setLoading(false, "I've Verified My Email", "Resend Verification Email")
            } else {
                verifiedButton.isEnabled = true
                verifiedButton.alpha = 1f
                verifiedButton.text = "I've Verified My Email"
                isLoading = false
            }
        }
    }

    private fun startResendCooldown() {
        resendCooldownSeconds = RESEND_COOLDOWN_SECONDS
        screenScope.launch {
            while (resendCooldownSeconds > 0) {
                resendButton.isEnabled = false
                resendButton.alpha = 0.72f
                resendButton.text = "Resend in ${resendCooldownSeconds}s"
                delay(1000L)
                resendCooldownSeconds--
            }
            resendButton.isEnabled = !isLoading
            resendButton.alpha = if (isLoading) 0.72f else 1f
            resendButton.text = "Resend Verification Email"
        }
    }

    private fun title(textValue: String): TextView {
        return TextView(context).apply {
            text = textValue
            setTextColor(Color.WHITE)
            textSize = 30f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
            gravity = Gravity.CENTER
        }
    }

    private fun description(): TextView {
        return TextView(context).apply {
            text = "We've sent a verification link to your email address.\nPlease open your email and click the verification link before continuing."
            setTextColor(Color.rgb(218, 229, 210))
            textSize = 15f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            includeFontPadding = true
            gravity = Gravity.CENTER
            setLineSpacing(4f, 1f)
        }
    }

    private fun message(): TextView {
        return TextView(context).apply {
            visibility = View.GONE
            setTextColor(Color.rgb(255, 210, 96))
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            includeFontPadding = false
            gravity = Gravity.CENTER
        }
    }

    private fun primaryButton(label: String, onClick: () -> Unit): TextView {
        return TextView(context).apply {
            text = label
            gravity = Gravity.CENTER
            setTextColor(onPrimary)
            textSize = 13f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(7).toFloat()
                setColor(primaryFixed)
            }
            setOnClickListener { onClick() }
        }
    }

    private fun secondaryButton(label: String, onClick: () -> Unit): TextView {
        return TextView(context).apply {
            text = label
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 13f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(7).toFloat()
                setColor(panelColor)
                setStroke(dp(1), Color.argb(78, 255, 255, 255))
            }
            setOnClickListener { onClick() }
        }
    }

    private fun showMessage(message: String?) {
        messageView.text = message.orEmpty()
        messageView.visibility = if (message.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    private fun setLoading(loading: Boolean, verifiedLabel: String, resendLabel: String) {
        isLoading = loading
        verifiedButton.isEnabled = !loading
        resendButton.isEnabled = !loading
        verifiedButton.alpha = if (loading) 0.72f else 1f
        resendButton.alpha = if (loading) 0.72f else 1f
        verifiedButton.text = verifiedLabel
        resendButton.text = resendLabel
    }

    private fun fullWidthParams(
        top: Int = 0,
        bottom: Int = 0,
        height: Int = LinearLayout.LayoutParams.WRAP_CONTENT
    ): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height).apply {
            topMargin = top
            bottomMargin = bottom
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density + 0.5f).toInt()
    }

    private companion object {
        const val AUTO_CHECK_DELAY_MS = 700L
        const val RESEND_COOLDOWN_SECONDS = 60
    }

    override fun onDetachedFromWindow() {
        screenScope.cancel()
        super.onDetachedFromWindow()
    }
}
