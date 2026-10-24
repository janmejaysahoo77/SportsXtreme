package com.example.sportsxtreme.presentation.auth

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.common.WindowInsetsUtils
import com.example.sportsxtreme.data.di.AuthDependencies
import com.example.sportsxtreme.presentation.components.AuthBackgroundView
import com.example.sportsxtreme.presentation.phoneAuth.Country
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import kotlinx.coroutines.*
import java.security.MessageDigest
import java.util.regex.Pattern
import kotlin.math.min
import kotlin.math.sin

class OtpVerificationScreenView @JvmOverloads constructor(
    context: Context,
    private val contact: String = "",
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val neonGreen = Color.rgb(193, 255, 0)
    private val electricBlue = Color.rgb(0, 127, 255)
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

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                val extras = intent.extras
                val status = if (Build.VERSION.SDK_INT >= 33) {
                    extras?.getParcelable(SmsRetriever.EXTRA_STATUS, Status::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    extras?.getParcelable(SmsRetriever.EXTRA_STATUS)
                }

                if (status?.statusCode == CommonStatusCodes.SUCCESS) {
                    val message = extras?.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                    if (message != null) {
                        Log.d("OTP_AUTOFILL", "SMS retrieved via Retriever, processing extraction")
                        extractOtpAndFill(message)
                    } else {
                        Log.w("OTP_AUTOFILL", "SMS retrieved but message is null")
                    }
                } else if (status?.statusCode == CommonStatusCodes.TIMEOUT) {
                    Log.d("OTP_AUTOFILL", "SMS Retrieval timed out")
                }
            }
        }
    }


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
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d("OTP_AUTOFILL", "Screen attached. Starting SMS Retriever...")
        printAppHash(context)
        startSmsRetriever(context)

        ContextCompat.registerReceiver(
            context,
            smsReceiver,
            IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
            SmsRetriever.SEND_PERMISSION,
            null,
            ContextCompat.RECEIVER_EXPORTED
        )

        otpScope.launch {
            authViewModel.state.collect { state ->
                state.smsMessage?.let { message ->
                    Log.d("OTP_AUTOFILL", "SMS message received from shared state, processing extraction")
                    extractOtpAndFill(message)
                    authViewModel.clearSmsMessage()
                }

                state.pendingPhoneSession?.let { session ->
                    if (session.isAutoVerified) {
                        session.autoFilledCode?.let { code ->
                            Log.d("OTP_AUTOFILL", "Firebase auto-retrieved code available, filling inputs")
                            fillOtp(code)
                        } ?: run {
                            verifyAutomaticallyIfReady(context)
                        }
                    }
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        otpScope.cancel()
        try {
            context.unregisterReceiver(smsReceiver)
        } catch (_: Exception) { }
        super.onDetachedFromWindow()
    }


    private fun startSmsRetriever(context: Context) {
        val client = SmsRetriever.getClient(context)
        client.startSmsRetriever().addOnSuccessListener {
            Log.d("OTP_AUTOFILL", "SMS Retriever started successfully")
        }.addOnFailureListener {
            Log.e("OTP_AUTOFILL", "Failed to start SMS Retriever", it)
        }
    }

    private fun extractOtpAndFill(message: String) {
        // More professional regex to catch 6 digits anywhere in the message
        val pattern = Pattern.compile("(\\d{6})")
        val matcher = pattern.matcher(message)
        if (matcher.find()) {
            val otp = matcher.group(1)
            Log.d("OTP_AUTOFILL", "6-digit OTP extracted successfully from SMS")
            if (otp != null && otp.length == 6) {
                fillOtp(otp)
            }
        } else {
            Log.d("OTP_AUTOFILL", "No 6-digit OTP pattern matched in SMS message")
        }
    }

    private fun fillOtp(otp: String) {
        otp.forEachIndexed { index, char ->
            if (index < otpInputs.size) {
                otpInputs[index].setText(char.toString())
            }
        }
        if (otp.length == 6) {
            verifyOtp(context)
        }
    }

    private fun printAppHash(context: Context) {
        try {
            val packageName = context.packageName
            @Suppress("DEPRECATION")
            val signatures = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
            signatures?.forEach { signature ->
                val appInfo = "$packageName ${signature.toCharsString()}"
                val messageDigest = MessageDigest.getInstance("SHA-256")
                messageDigest.update(appInfo.toByteArray(Charsets.UTF_8))
                val hashSignature = messageDigest.digest().copyOfRange(0, 9)
                val hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING or Base64.NO_WRAP).substring(0, 11)
                
                Log.i("OTP_AUTOFILL", "---------------------------------------------------")
                Log.i("OTP_AUTOFILL", "YOUR APP HASH: $hash")
                Log.i("OTP_AUTOFILL", "FORMAT: <#> Your SportsXtreme OTP is 123456. $hash")
                Log.i("OTP_AUTOFILL", "---------------------------------------------------")
            }
        } catch (e: Exception) {
            Log.e("OTP_AUTOFILL", "Error generating App Hash", e)
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
                "Enter a 6-digit verification code sent to your mobile number."
            } else {
                "We sent a 6-digit verification code to\n${maskedPhoneNumber(contact)}"
            }
            gravity = Gravity.CENTER
            setTextColor(muted)
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            includeFontPadding = false
        }, fullWidthParams(top = dp(8), bottom = dp(20)))
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

    private fun maskedPhoneNumber(phoneNumber: String): String {
        val digits = phoneNumber.filter(Char::isDigit)
        val country = Country.ALL
            .sortedByDescending { it.dialCode.length }
            .firstOrNull { digits.startsWith(it.dialCode.removePrefix("+")) }
        val dialCode = country?.dialCode ?: "+${digits.take(2)}"
        val localNumber = digits.drop(dialCode.count(Char::isDigit))
        val maskLength = (localNumber.length - 4).coerceAtLeast(4)
        return "$dialCode ${"*".repeat(maskLength)}${localNumber.takeLast(4)}"
    }

    private fun topRail(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = panelBackground(Color.argb(154, 4, 9, 12), Color.argb(76, 0, 127, 255), dp(10))

            addView(TextView(context).apply {
                text = "SPORTS"
                setTextColor(Color.rgb(236, 243, 238))
                textSize = 13f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
            })
            addView(TextView(context).apply {
                text = "XTREME"
                setTextColor(electricBlue)
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
                text = "Enter Verification Code"
                setTextColor(Color.rgb(226, 238, 229))
                textSize = 13f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
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
        return OtpCodeEditText(context, index).apply {
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
            setOnKeyListener { _, keyCode, event ->
                keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN &&
                    moveFocusBackOnBackspace(index)
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

    private fun moveFocusBackOnBackspace(index: Int): Boolean {
        val currentInput = otpInputs.getOrNull(index) ?: return false
        if (index == 0 || !currentInput.text.isNullOrEmpty()) return false

        val previousInput = otpInputs[index - 1]
        previousInput.text.clear()
        previousInput.requestFocus()
        previousInput.setSelection(previousInput.text.length)
        clearError()
        refreshVerifyState()
        return true
    }

    private inner class OtpCodeEditText(context: Context, private val index: Int) : EditText(context) {
        override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
            val inputConnection = super.onCreateInputConnection(outAttrs) ?: return null
            return object : InputConnectionWrapper(inputConnection, false) {
                override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
                    if (beforeLength > 0 && afterLength == 0 && moveFocusBackOnBackspace(index)) return true
                    return super.deleteSurroundingText(beforeLength, afterLength)
                }

                override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
                    if (beforeLength > 0 && afterLength == 0 && moveFocusBackOnBackspace(index)) return true
                    return super.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
                }

                override fun sendKeyEvent(event: KeyEvent): Boolean {
                    if (event.keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN &&
                        moveFocusBackOnBackspace(index)
                    ) return true
                    return super.sendKeyEvent(event)
                }
            }
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
                text = "Didn't receive the code? "
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
            text = "← Change mobile number"
            gravity = Gravity.CENTER
            setTextColor(electricBlue)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
            includeFontPadding = false
            setShadowLayer(2.5f * resources.displayMetrics.density, 0f, 0f, Color.argb(190, 0, 127, 255))
            setOnClickListener {
                val mainActivity = context as? MainActivity
                if (mainActivity != null) {
                    mainActivity.showPhoneAuthScreen(
                        forSignupCompletion = mainActivity.isSignupPhoneVerificationPending()
                    )
                } else {
                    (context as? Activity)?.finish()
                }
            }
        }
    }

    private fun verifyOtp(context: Context) {
        if (isVerifying) return
        val code = otpInputs.joinToString(separator = "") { it.text.toString() }
        Log.d("OTP_SYSTEM", "[UI] verifyOtp triggered with code length: ${code.length}")
        if (code.length != 6) {
            Log.w("OTP_SYSTEM", "[UI] verifyOtp validation failed: Incomplete OTP code (length=${code.length})")
            showError("Enter the complete 6 digit code")
            return
        }
        setVerifying(true)
        otpScope.launch {
            Log.d("OTP_SYSTEM", "[UI] Calling authViewModel.verifyPhoneOtp...")
            when (val result = authViewModel.verifyPhoneOtp(code)) {
                is Resource.Success -> {
                    Log.i("OTP_SYSTEM", "[UI] verifyOtp SUCCESS! isNewUser=${result.data}")
                    showMessage("Verification complete.")
                    val mainActivity = context as? MainActivity
                    if (mainActivity?.isSignupPhoneVerificationPending() == true) {
                        mainActivity.showHomeScreen()
                    } else if (result.data == true) {
                        (context as? MainActivity)?.showSignupScreen(contact)
                    } else {
                        navigateAfterVerification(context)
                    }
                }
                is Resource.Error -> {
                    Log.e("OTP_SYSTEM", "[UI] verifyOtp FAILED: ${result.message}")
                    showError(result.message ?: "OTP verification failed")
                }
                is Resource.Loading -> Unit
            }
            setVerifying(false)
        }
    }

    private fun verifyAutomaticallyIfReady(context: Context) {
        val autoVerified = authViewModel.state.value.pendingPhoneSession?.isAutoVerified == true
        Log.d("OTP_SYSTEM", "[UI] verifyAutomaticallyIfReady check: autoVerified=$autoVerified")
        if (!autoVerified) return
        setVerifying(true)
        showMessage("Phone verified automatically.")
        otpScope.launch {
            Log.d("OTP_SYSTEM", "[UI] Calling authViewModel.verifyPhoneOtp for auto-verified session...")
            when (val result = authViewModel.verifyPhoneOtp("")) {
                is Resource.Success -> {
                    Log.i("OTP_SYSTEM", "[UI] verifyAutomaticallyIfReady SUCCESS! isNewUser=${result.data}")
                    val mainActivity = context as? MainActivity
                    if (mainActivity?.isSignupPhoneVerificationPending() == true) {
                        mainActivity.showHomeScreen()
                    } else if (result.data == true) {
                        (context as? MainActivity)?.showSignupScreen(contact)
                    } else {
                        navigateAfterVerification(context)
                    }
                }
                is Resource.Error -> {
                    Log.e("OTP_SYSTEM", "[UI] verifyAutomaticallyIfReady FAILED: ${result.message}")
                    showError(result.message ?: "Automatic verification failed")
                }
                is Resource.Loading -> Unit
            }
            setVerifying(false)
        }
    }

    private fun resendOtp() {
        if (isVerifying) return
        Log.d("OTP_SYSTEM", "[UI] resendOtp triggered by user")
        setVerifying(true, label = "RESENDING...")
        otpScope.launch {
            when (val result = authViewModel.resendPhoneOtp()) {
                is Resource.Success -> {
                    resendCount += 1
                    Log.i("OTP_SYSTEM", "[UI] resendOtp SUCCESS! resendCount=$resendCount, isAutoVerified=${result.data?.isAutoVerified}")
                    resendView.text = "CODE RESENT ${resendCount}X"
                    clearOtp()
                    if (result.data?.isAutoVerified == true) {
                        showMessage("Phone verified automatically.")
                    } else {
                        showMessage("New code request queued.")
                    }
                }
                is Resource.Error -> {
                    Log.e("OTP_SYSTEM", "[UI] resendOtp FAILED: ${result.message}")
                    showError(result.message ?: "Could not resend OTP")
                }
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
        private val appIcon = BitmapFactory.decodeResource(resources, R.drawable.appicon)
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val bounds = RectF()
        private var startedAt = android.os.SystemClock.uptimeMillis()

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            startedAt = android.os.SystemClock.uptimeMillis()
            postInvalidateOnAnimation()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val density = resources.displayMetrics.density
            val cx = width / 2f
            val cy = height / 2f
            val pulse = (sin((android.os.SystemClock.uptimeMillis() - startedAt) / 360f) + 1f) / 2f
            val logoSize = min(width, height) * 0.52f
            val left = cx - logoSize / 2f
            val top = cy - logoSize / 2f
            val right = cx + logoSize / 2f
            val bottom = cy + logoSize / 2f
            val phase = ((android.os.SystemClock.uptimeMillis() - startedAt) % 1800L) / 1800f
            val scanProgress = if (phase < 0.5f) phase * 2f else (1f - phase) * 2f

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.4f * density
            paint.color = Color.argb((80 + pulse * 82).toInt(), 0, 127, 255)
            bounds.set(cx - 60f * density, cy - 60f * density, cx + 60f * density, cy + 60f * density)
            canvas.drawRoundRect(bounds, 28f * density, 28f * density, paint)
            paint.strokeWidth = 0.9f * density
            paint.color = Color.argb((110 + pulse * 96).toInt(), 193, 255, 0)
            bounds.set(cx - 52f * density, cy - 52f * density, cx + 52f * density, cy + 52f * density)
            canvas.drawRoundRect(bounds, 23f * density, 23f * density, paint)
            paint.color = Color.argb((120 + pulse * 76).toInt(), 193, 255, 0)
            bounds.set(cx - 42f * density, cy - 42f * density, cx + 42f * density, cy + 42f * density)
            canvas.drawOval(bounds, paint)

            bounds.set(left, top, right, bottom)
            appIcon?.let { canvas.drawBitmap(it, null, bounds, bitmapPaint) }

            val scanY = top + logoSize * scanProgress
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(
                left, scanY - 10f * density, left, scanY + 10f * density,
                intArrayOf(Color.TRANSPARENT, Color.argb(74, 0, 127, 255), Color.argb(132, 193, 255, 0), Color.TRANSPARENT),
                floatArrayOf(0f, 0.36f, 0.5f, 1f), Shader.TileMode.CLAMP
            )
            bounds.set(left - 8f * density, scanY - 10f * density, right + 8f * density, scanY + 10f * density)
            canvas.drawRoundRect(bounds, 10f * density, 10f * density, paint)
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f * density
            paint.color = Color.argb(230, 193, 255, 0)
            canvas.drawLine(left - 10f * density, scanY, right + 10f * density, scanY, paint)
            postInvalidateOnAnimation()
        }
    }

}
