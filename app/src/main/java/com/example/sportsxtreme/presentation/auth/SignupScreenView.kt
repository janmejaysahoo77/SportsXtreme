package com.example.sportsxtreme.presentation.auth

import com.example.sportsxtreme.R
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.common.WindowInsetsUtils
import com.example.sportsxtreme.data.di.AuthDependencies
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
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.Patterns
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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SignupScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val verifiedPhoneNumber: String? = null
) : FrameLayout(context, attrs) {

    private val primaryFixed = Color.rgb(193, 255, 0)
    private val onPrimary = Color.rgb(24, 34, 0)
    private val splashTitleBlue = Color.rgb(0, 127, 255)
    private val panelColor = Color.argb(186, 4, 7, 7)

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var errorMessageView: TextView
    private lateinit var createAccountButton: TextView
    private val authViewModel = AuthDependencies.authViewModel()
    private val signupScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val credentialManager = CredentialManager.create(context)
    private var isSignupLoading = false

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        setBackgroundColor(Color.BLACK)
        addView(AuthBackgroundView(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
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
            setPadding(dp(22), dp(10), dp(22), dp(28))
            WindowInsetsUtils.applySystemBarsPadding(this, applyBottom = false)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        content.addView(AuthBrandingHeaderView(context), fullWidthParams(bottom = dp(18), height = dp(90)))

        content.addView(titleText("SIGN UP", 32f, Color.WHITE).apply {
            setShadowLayer(3.5f * density, 2f * density, 2f * density, splashTitleBlue)
        }, fullWidthParams())

        content.addView(TextView(context).apply {
            text = context.getString(R.string.str_join_the_elite_perfo)
            setTextColor(Color.rgb(218, 229, 210))
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            includeFontPadding = false
        }, fullWidthParams(top = dp(8), bottom = dp(18)))

        emailInput = addField(content, "EMAIL", "ATHLETE@DOMAIN.COM", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        passwordInput = addField(content, "PASSWORD", "********", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD).apply {
            transformationMethod = PasswordTransformationMethod.getInstance()
        }
        confirmPasswordInput = addField(content, "CONFIRM PASSWORD", "********", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD).apply {
            transformationMethod = PasswordTransformationMethod.getInstance()
        }

        errorMessageView = errorMessage(context)
        content.addView(errorMessageView, fullWidthParams(bottom = dp(8)))
        content.addView(primaryButton(context), fullWidthParams(top = dp(10), bottom = dp(16), height = dp(52)))
        if (verifiedPhoneNumber == null) {
            content.addView(divider(context), fullWidthParams(bottom = dp(16), height = dp(18)))
            content.addView(socialRow(context), fullWidthParams(bottom = dp(22), height = dp(44)))
        }
        content.addView(loginPrompt(context), fullWidthParams(bottom = dp(6), height = dp(28)))
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
            setSelectAllOnFocus(false)
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) = Unit
            })
            setOnFocusChangeListener { view, hasFocus ->
                view.background = fieldBackground(hasFocus)
            }
        }

        parent.addView(editText, fullWidthParams(bottom = dp(13), height = dp(42)))
        return editText
    }

    private fun primaryButton(context: Context): TextView {
        return TextView(context).apply {
            createAccountButton = this
            text = context.getString(R.string.str_create_account)
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
                handleSignup(context)
            }
        }
    }

    private fun errorMessage(context: Context): TextView {
        return TextView(context).apply {
            visibility = View.GONE
            setTextColor(Color.rgb(255, 112, 112))
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            includeFontPadding = false
        }
    }

    private fun handleSignup(context: Context) {
        if (isSignupLoading) return

        val email = emailInput.text.toString().trim()
        val name = email.substringBefore('@').replace(Regex("[._-]+"), " ").trim()
            .ifBlank { "SportsXtreme Athlete" }
        val phoneNumber = verifiedPhoneNumber.orEmpty()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        val validationError = validateSignup(email, password, confirmPassword)
        if (validationError != null) {
            showError(validationError)
            return
        }

        setLoading(true)
        signupScope.launch {
            val result = if (verifiedPhoneNumber != null) {
                authViewModel.completePhoneSignup(name, email, phoneNumber)
            } else {
                authViewModel.createEmailAccount(name, email, password, phoneNumber)
            }
            when (result) {
                is Resource.Success -> {
                    showError(null)
                    if (verifiedPhoneNumber != null) {
                        (context as? MainActivity)?.showSportSelectionScreen()
                    } else {
                        (context as? MainActivity)?.showPhoneAuthScreen(forSignupCompletion = true)
                    }
                }
                is Resource.Error -> {
                    showError(result.message ?: "Signup failed")
                }
                is Resource.Loading -> Unit
            }
            setLoading(false)
        }
    }

    private fun handleGoogleSignup(context: Context) {
        if (isSignupLoading) return

        val serverClientId = googleServerClientId(context)
        if (serverClientId.isNullOrBlank()) {
            showError("Google signup needs a Firebase web client ID. Add SHA keys in Firebase, download google-services.json again, then rebuild.")
            return
        }

        setLoading(true, googleLoading = true)
        signupScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    when (val authResult = authViewModel.signInWithGoogle(googleCredential.idToken)) {
                        is Resource.Success -> {
                            showError(null)
                            (context as? MainActivity)?.showPhoneAuthScreen(forSignupCompletion = true)
                        }
                        is Resource.Error -> showError(authResult.message ?: "Google signup failed")
                        is Resource.Loading -> Unit
                    }
                } else {
                    showError("Google signup did not return a valid account.")
                }
            } catch (error: GetCredentialCancellationException) {
                showError(null)
            } catch (error: GetCredentialException) {
                showError(error.message ?: "Google signup failed")
            } catch (error: GoogleIdTokenParsingException) {
                showError("Google signup response could not be read.")
            } finally {
                setLoading(false)
            }
        }
    }

    private fun validateSignup(email: String, password: String, confirmPassword: String): String? {
        return when {
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email"
            password.length < 6 -> "Password must be at least 6 characters"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    private fun googleServerClientId(context: Context): String? {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (resId != 0) context.getString(resId) else null
    }

    private fun setLoading(loading: Boolean, googleLoading: Boolean = false) {
        isSignupLoading = loading
        createAccountButton.isEnabled = !loading
        createAccountButton.alpha = if (loading) 0.72f else 1f
        createAccountButton.text = if (loading) "CREATING..." else context.getString(R.string.str_create_account)
        if (googleLoading) {
            createAccountButton.text = context.getString(R.string.str_create_account)
        }
    }

    private fun showError(message: String?) {
        errorMessageView.text = message.orEmpty()
        errorMessageView.visibility = if (message.isNullOrBlank()) View.GONE else View.VISIBLE
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
            val googleSignupButton = socialButton(context, "Sign Up With Google", R.drawable.googleicon).apply {
                setOnClickListener {
                    handleGoogleSignup(context)
                }
            }
            addView(googleSignupButton, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))
        }
    }

    private fun socialButton(context: Context, label: String, iconRes: Int?): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(24).toFloat()
                setColor(Color.rgb(26, 26, 26))
            }

            if (iconRes != null) {
                addView(ImageView(context).apply {
                    setImageResource(iconRes)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    contentDescription = "$label icon"
                }, LinearLayout.LayoutParams(dp(18), dp(18)).apply {
                    rightMargin = dp(10)
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
                textSize = 13f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                includeFontPadding = false
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun loginPrompt(context: Context): TextView {
        val text = SpannableString(context.getString(R.string.str_already_have_an_acco))
        val start = text.indexOf(context.getString(R.string.str_login_1))
        text.setSpan(ForegroundColorSpan(primaryFixed), start, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(StyleSpan(Typeface.BOLD_ITALIC), start, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return TextView(context).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(242, 244, 215))
            textSize = 11f
            includeFontPadding = false
            setOnClickListener {
                (context as? MainActivity)?.showLoginScreen()
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

    override fun onDetachedFromWindow() {
        signupScope.cancel()
        super.onDetachedFromWindow()
    }

}
