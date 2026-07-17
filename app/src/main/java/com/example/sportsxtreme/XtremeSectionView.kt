package com.example.sportsxtreme

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class XtremeSectionView(
    context: Context,
    private val title: String,
    private val bodyText: String,
    private val selectedMode: SectionMode,
    private val logoRes: Int? = null,
    private val useCartLogo: Boolean = false,
    private val bodyContent: ((Context) -> View)? = null
) : FrameLayout(context) {

    enum class SectionMode { SPORTS, MEDIA, CART }

    private val bg = Color.rgb(1, 5, 9)
    private val primary = Color.rgb(193, 255, 0)

    init {
        setBackgroundColor(bg)
        addView(createContent(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun createContent(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(topBar(context), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(FrameLayout(context).apply {
                val customBody = bodyContent?.invoke(context)
                if (customBody != null) {
                    addView(customBody, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
                } else {
                    addView(TextView(context).apply {
                        text = bodyText
                        gravity = Gravity.CENTER
                        setTextColor(Color.WHITE)
                        textSize = 28f
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                        includeFontPadding = false
                    }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
                }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        }
    }

    private fun topBar(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(18))
            background = GradientDrawable().apply {
                setColor(Color.rgb(6, 12, 17))
            }

            addView(sectionModeButtons(context), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)))

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                if (useCartLogo) {
                    addView(SectionCartLogoView(context).apply {
                        setTint(primary)
                    }, LinearLayout.LayoutParams(dp(36), dp(36)))
                } else {
                    logoRes?.let { resId ->
                        addView(ImageView(context).apply {
                            setImageResource(resId)
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            adjustViewBounds = true
                        }, LinearLayout.LayoutParams(dp(42), dp(36)))
                    }
                }

                addView(sectionBrandTitle(context), LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    leftMargin = dp(8)
                })

                addView(View(context), LinearLayout.LayoutParams(0, 1, 1f))
                addView(TopBarIconView(context, TopBarIconView.Icon.SEARCH), LinearLayout.LayoutParams(dp(21), dp(21)).apply {
                    leftMargin = dp(10)
                })
                if (selectedMode != SectionMode.CART) {
                    addView(TopBarIconView(context, TopBarIconView.Icon.BELL), LinearLayout.LayoutParams(dp(21), dp(21)).apply {
                        leftMargin = dp(10)
                    })
                    addView(TopBarIconView(context, TopBarIconView.Icon.MESSAGE), LinearLayout.LayoutParams(dp(21), dp(21)).apply {
                        leftMargin = dp(10)
                    })
                }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(42)).apply {
                topMargin = dp(20)
            })
        }
    }

    private fun sectionBrandTitle(context: Context): View {
        if (selectedMode != SectionMode.MEDIA) {
            return TextView(context).apply {
                text = title
                setTextColor(Color.WHITE)
                textSize = 18f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
                setShadowLayer(dp(2).toFloat(), 0f, 0f, Color.argb(105, 0, 120, 255))
            }
        }

        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(context).apply {
                text = "Xtreme"
                setTextColor(Color.rgb(0, 127, 255))
                textSize = 18f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
                setShadowLayer(dp(2).toFloat(), 0f, 0f, Color.argb(115, 0, 120, 255))
            })
            addView(TextView(context).apply {
                text = "Media"
                setTextColor(Color.rgb(232, 241, 246))
                textSize = 16.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
                setShadowLayer(dp(2).toFloat(), 0f, 0f, Color.argb(95, 0, 120, 255))
            })
        }
    }

    private fun sectionModeButtons(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            addView(sectionModeButton(
                context = context,
                label = context.getString(R.string.str_sportsxtreme),
                selected = selectedMode == SectionMode.SPORTS,
                imageRes = R.drawable.appicon2
            ).apply {
                setOnClickListener {
                    context.startActivity(Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    })
                    (context as? Activity)?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    (context as? Activity)?.finish()
                }
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))

            addView(sectionModeButton(
                context = context,
                label = context.getString(R.string.str_xtrememedia),
                selected = selectedMode == SectionMode.MEDIA,
                imageRes = R.drawable.xtrememediaicon
            ).apply {
                setOnClickListener {
                    if (selectedMode != SectionMode.MEDIA) {
                        context.startActivity(Intent(context, XtremeMediaActivity::class.java))
                        (context as? Activity)?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                        (context as? Activity)?.finish()
                    }
                }
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                leftMargin = dp(7)
            })

            addView(sectionModeButton(
                context = context,
                label = context.getString(R.string.str_xtremecart),
                selected = selectedMode == SectionMode.CART,
                useCartIcon = true
            ).apply {
                setOnClickListener {
                    if (selectedMode != SectionMode.CART) {
                        context.startActivity(Intent(context, ShoppingActivity::class.java))
                        (context as? Activity)?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        (context as? Activity)?.finish()
                    }
                }
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                leftMargin = dp(7)
            })
        }
    }

    private fun sectionModeButton(
        context: Context,
        label: String,
        selected: Boolean,
        imageRes: Int? = null,
        useCartIcon: Boolean = false
    ): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            setPadding(dp(3), 0, dp(3), 0)
            background = if (selected) {
                GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(Color.rgb(12, 30, 24), Color.rgb(4, 24, 50))
                ).apply {
                    cornerRadius = dp(10).toFloat()
                    setStroke(dp(1), primary)
                }
            } else {
                GradientDrawable().apply {
                    cornerRadius = dp(10).toFloat()
                    setColor(Color.WHITE)
                    setStroke(dp(1), Color.argb(85, 255, 255, 255))
                }
            }

            imageRes?.let { resId ->
                addView(ImageView(context).apply {
                    setImageResource(resId)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    adjustViewBounds = true
                }, LinearLayout.LayoutParams(dp(22), dp(22)).apply {
                    rightMargin = dp(4)
                })
            }

            if (useCartIcon) {
                addView(SectionCartLogoView(context).apply {
                    setTint(if (selected) primary else Color.rgb(6, 12, 17))
                }, LinearLayout.LayoutParams(dp(17), dp(17)).apply {
                    rightMargin = dp(4)
                })
            }

            addView(TextView(context).apply {
                text = label
                gravity = Gravity.CENTER
                setTextColor(if (selected) Color.WHITE else Color.rgb(6, 12, 17))
                textSize = 8.4f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
                maxLines = 1
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

    private class SectionCartLogoView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val path = Path()
        private var tint = Color.WHITE

        fun setTint(color: Int) {
            tint = color
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            val d = resources.displayMetrics.density
            val w = width.toFloat()
            val h = height.toFloat()
            val s = minOf(w, h)
            paint.color = tint
            paint.strokeWidth = 2.4f * d
            paint.style = Paint.Style.STROKE
            path.reset()
            path.moveTo(w * 0.16f, h * 0.24f)
            path.lineTo(w * 0.26f, h * 0.24f)
            path.lineTo(w * 0.36f, h * 0.66f)
            path.lineTo(w * 0.78f, h * 0.66f)
            path.lineTo(w * 0.86f, h * 0.38f)
            path.lineTo(w * 0.32f, h * 0.38f)
            canvas.drawPath(path, paint)
            paint.style = Paint.Style.FILL
            canvas.drawCircle(w * 0.43f, h * 0.82f, s * 0.055f, paint)
            canvas.drawCircle(w * 0.72f, h * 0.82f, s * 0.055f, paint)
        }
    }

    private class TopBarIconView(context: Context, private val icon: Icon) : View(context) {
        enum class Icon { MENU, SEARCH, BELL, MESSAGE }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val path = Path()
        private val rect = RectF()

        override fun onDraw(canvas: Canvas) {
            val d = resources.displayMetrics.density
            val w = width.toFloat()
            val h = height.toFloat()
            val s = minOf(w, h)
            paint.strokeWidth = 1.9f * d
            paint.style = Paint.Style.STROKE

            when (icon) {
                Icon.MENU -> {
                    canvas.drawLine(w * 0.22f, h * 0.32f, w * 0.78f, h * 0.32f, paint)
                    canvas.drawLine(w * 0.22f, h * 0.5f, w * 0.78f, h * 0.5f, paint)
                    canvas.drawLine(w * 0.22f, h * 0.68f, w * 0.78f, h * 0.68f, paint)
                }
                Icon.SEARCH -> {
                    canvas.drawCircle(w * 0.45f, h * 0.43f, s * 0.2f, paint)
                    canvas.drawLine(w * 0.6f, h * 0.58f, w * 0.78f, h * 0.76f, paint)
                }
                Icon.BELL -> {
                    path.reset()
                    path.moveTo(w * 0.32f, h * 0.58f)
                    path.cubicTo(w * 0.34f, h * 0.36f, w * 0.38f, h * 0.25f, w * 0.5f, h * 0.25f)
                    path.cubicTo(w * 0.62f, h * 0.25f, w * 0.66f, h * 0.36f, w * 0.68f, h * 0.58f)
                    path.lineTo(w * 0.76f, h * 0.7f)
                    path.lineTo(w * 0.24f, h * 0.7f)
                    path.close()
                    canvas.drawPath(path, paint)
                    canvas.drawLine(w * 0.44f, h * 0.8f, w * 0.56f, h * 0.8f, paint)
                }
                Icon.MESSAGE -> {
                    rect.set(w * 0.22f, h * 0.28f, w * 0.78f, h * 0.65f)
                    canvas.drawRoundRect(rect, 2f * d, 2f * d, paint)
                    path.reset()
                    path.moveTo(w * 0.4f, h * 0.65f)
                    path.lineTo(w * 0.32f, h * 0.78f)
                    path.lineTo(w * 0.52f, h * 0.65f)
                    canvas.drawPath(path, paint)
                    canvas.drawLine(w * 0.34f, h * 0.42f, w * 0.66f, h * 0.42f, paint)
                    canvas.drawLine(w * 0.34f, h * 0.52f, w * 0.58f, h * 0.52f, paint)
                }
            }
        }
    }
}
