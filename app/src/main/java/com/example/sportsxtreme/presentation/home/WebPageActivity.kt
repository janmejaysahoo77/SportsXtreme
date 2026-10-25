package com.example.sportsxtreme.presentation.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView

/** Displays SportsXtreme website pages from the navigation drawer. */
class WebPageActivity : Activity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pageUrl = intent.getStringExtra(EXTRA_URL) ?: run {
            finish()
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(6, 18, 34))
        }
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(16), 0)
            setBackgroundColor(Color.rgb(8, 29, 52))
        }
        toolbar.addView(TextView(this).apply {
            text = "‹"
            textSize = 36f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            contentDescription = "Back"
            setOnClickListener { onBackPressed() }
        }, LinearLayout.LayoutParams(dp(48), dp(56)))
        toolbar.addView(TextView(this).apply {
            text = intent.getStringExtra(EXTRA_TITLE).orEmpty()
            textSize = 18f
            setTextColor(Color.WHITE)
            setSingleLine(true)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(toolbar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)))

        val webContent = FrameLayout(this)
        val loadingIndicator = ProgressBar(this).apply {
            isIndeterminate = true
            indeterminateTintList = android.content.res.ColorStateList.valueOf(Color.rgb(193, 255, 0))
        }
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    loadingIndicator.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    loadingIndicator.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: android.webkit.WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {
                    if (request?.isForMainFrame != false) {
                        loadingIndicator.visibility = View.GONE
                    }
                }
            }
            loadUrl(pageUrl)
        }
        webContent.addView(webView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        webContent.addView(loadingIndicator, FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER))
        root.addView(webContent, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val EXTRA_URL = "sportsxtreme.web_page.url"
        private const val EXTRA_TITLE = "sportsxtreme.web_page.title"
        private const val BASE_URL = "https://sportsxtremeweb.netlify.app"

        fun intent(context: Context, path: String): Intent {
            val title = when (path) {
                "/awards" -> "SportsXtreme Awards"
                "/contact" -> "Contact"
                "/about" -> "About Us"
                "/help-support" -> "Help & Support"
                "/faqs" -> "FAQs"
                "/terms", "/terms-of-service" -> "Terms of Service"
                "/app-privacy-policy" -> "Privacy Policy"
                else -> "SportsXtreme"
            }
            return Intent(context, WebPageActivity::class.java)
                .putExtra(EXTRA_URL, "$BASE_URL$path")
                .putExtra(EXTRA_TITLE, title)
        }
    }
}
