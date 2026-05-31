package com.example.sportsxtreme

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class XtremePlaceholderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val title: String = "",
    private val subtitle: String = ""
) : FrameLayout(context, attrs) {

    private val primary = Color.rgb(193, 255, 0)
    private val bg = Color.rgb(1, 5, 9)
    private val muted = Color.rgb(130, 145, 142)

    init {
        setBackgroundColor(bg)
        addView(createContent(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun createContent(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(24), dp(18), dp(18))

            addView(TextView(context).apply {
                text = context.getString(R.string.str_back_to_sportsxtreme)
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                setTextColor(Color.rgb(6, 12, 17))
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
                background = GradientDrawable().apply {
                    cornerRadius = dp(9).toFloat()
                    setColor(primary)
                }
                setOnClickListener {
                    (context as? MainActivity)?.showHomeScreen()
                }
            }, LinearLayout.LayoutParams(dp(118), dp(38)))

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(22), dp(28), dp(22), dp(28))
                background = GradientDrawable().apply {
                    cornerRadius = dp(10).toFloat()
                    setColor(Color.rgb(7, 15, 25))
                    setStroke(dp(1), Color.argb(55, 255, 255, 255))
                }

                addView(TextView(context).apply {
                    text = title
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    textSize = 28f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

                addView(TextView(context).apply {
                    text = subtitle
                    gravity = Gravity.CENTER
                    setTextColor(muted)
                    textSize = 13f
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(10)
                })
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply {
                topMargin = dp(18)
            })
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()
}
