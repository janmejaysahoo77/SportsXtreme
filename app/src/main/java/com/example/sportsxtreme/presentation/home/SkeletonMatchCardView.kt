package com.example.sportsxtreme.presentation.home

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout

/**
 * Skeleton shimmer card that mirrors the [LiveMatchCardView] layout.
 *
 * Shows animated shimmer placeholders for every text region (league name,
 * status badge, team names, scores, overs, stats chips, batter/bowler chips,
 * and status note) while real data is loading.
 *
 * The shimmer is a diagonal gradient that sweeps left-to-right infinitely,
 * giving users a polished "content is coming" signal.
 */
class SkeletonMatchCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    // Bone colours — slightly lighter than the card background.
    private val boneBase = Color.rgb(18, 40, 68)
    private val boneHighlight = Color.rgb(30, 60, 96)
    private val cardBg = Color.rgb(8, 28, 58)
    private val primary = Color.rgb(193, 255, 0)
    private val cyan = Color.rgb(0, 210, 255)

    private val bones = mutableListOf<View>()
    private var shimmerAnimator: ValueAnimator? = null
    private var shimmerOffset = 0f

    init {
        clipToOutline = true
        background = GradientDrawable().apply {
            cornerRadius = dp(14).toFloat()
            setColor(cardBg)
        }
        elevation = dp(12).toFloat()
        addView(buildContent(), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    private fun buildContent(): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(11), dp(14), dp(12))

            // ── Header row: league name bone + status badge bone ──
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                // League name area
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(bone(dp(100), dp(12), dp(4)))
                    addView(bone(dp(70), dp(9), dp(4)).apply {
                        (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp(6)
                    })
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

                // Status badge bone
                addView(bone(dp(58), dp(28), dp(14)))
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))

            // ── Teams row: Team A panel | VS circle | Team B panel ──
            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER
                orientation = LinearLayout.HORIZONTAL

                addView(teamSkeletonPanel(true), LinearLayout.LayoutParams(0, dp(96), 1f))

                // VS circle placeholder
                addView(FrameLayout(context).apply {
                    addView(View(context).apply {
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setColor(Color.argb(40, Color.red(primary), Color.green(primary), Color.blue(primary)))
                        }
                    }, LayoutParams(dp(40), dp(40), Gravity.CENTER))
                }, LinearLayout.LayoutParams(dp(54), LinearLayout.LayoutParams.MATCH_PARENT))

                addView(teamSkeletonPanel(false), LinearLayout.LayoutParams(0, dp(96), 1f))
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(96)
            ).apply { topMargin = dp(10) })

            // ── Stats row: 3 chip bones ──
            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER
                addView(bone(dp(64), dp(28), dp(14)))
                addView(bone(dp(64), dp(28), dp(14)).apply {
                    (layoutParams as? LinearLayout.LayoutParams)?.leftMargin = dp(8)
                })
                addView(bone(dp(80), dp(28), dp(14)).apply {
                    (layoutParams as? LinearLayout.LayoutParams)?.leftMargin = dp(8)
                })
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(30)
            ).apply { topMargin = dp(9) })

            // ── Batter / Bowler row ──
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(bone(0, dp(24), dp(10)).apply {
                    layoutParams = LinearLayout.LayoutParams(0, dp(24), 1f)
                })
                addView(bone(0, dp(24), dp(10)).apply {
                    layoutParams = LinearLayout.LayoutParams(0, dp(24), 1f).apply {
                        leftMargin = dp(8)
                    }
                })
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(9) })

            // ── Status note bone ──
            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER
                addView(bone(dp(160), dp(10), dp(5)))
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) })
        }
    }

    private fun teamSkeletonPanel(isTeamA: Boolean): View {
        val iconAccent = if (isTeamA) primary else cyan
        return FrameLayout(context).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.argb(150, 23, 35, 47), Color.argb(160, 8, 15, 22))
            ).apply {
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), Color.argb(40, Color.red(iconAccent), Color.green(iconAccent), Color.blue(iconAccent)))
            }

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(6), dp(6), dp(6), dp(6))

                // Team avatar circle bone
                addView(View(context).apply {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.argb(30, Color.red(iconAccent), Color.green(iconAccent), Color.blue(iconAccent)))
                    }
                    bones.add(this)
                }, LinearLayout.LayoutParams(dp(32), dp(32)))

                // Team short name bone
                addView(bone(dp(40), dp(9), dp(4)).apply {
                    (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp(6)
                })

                // Score bone
                addView(bone(dp(56), dp(22), dp(5)).apply {
                    (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp(4)
                })

                // Overs bone
                addView(bone(dp(36), dp(8), dp(4)).apply {
                    (layoutParams as? LinearLayout.LayoutParams)?.topMargin = dp(3)
                })
            }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        }
    }

    /**
     * Creates a rounded-rectangle "bone" placeholder view and registers it
     * for shimmer animation.
     */
    private fun bone(width: Int, height: Int, cornerRadius: Int): View {
        return ShimmerBoneView(context, boneBase, boneHighlight, dp(cornerRadius)).also {
            bones.add(it)
            it.layoutParams = LinearLayout.LayoutParams(width, height)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startShimmer()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopShimmer()
    }

    private fun startShimmer() {
        if (shimmerAnimator?.isRunning == true) return
        shimmerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1200L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                shimmerOffset = anim.animatedValue as Float
                bones.forEach { bone ->
                    if (bone is ShimmerBoneView) {
                        bone.shimmerOffset = shimmerOffset
                        bone.invalidate()
                    }
                }
            }
            start()
        }
    }

    private fun stopShimmer() {
        shimmerAnimator?.cancel()
        shimmerAnimator = null
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    /**
     * A single bone placeholder that draws a rounded rectangle with a
     * sweeping shimmer gradient.
     */
    private class ShimmerBoneView(
        context: Context,
        private val baseColor: Int,
        private val highlightColor: Int,
        private val cornerRadiusPx: Int
    ) : View(context) {

        var shimmerOffset: Float = 0f

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rect = RectF()

        override fun onDraw(canvas: Canvas) {
            if (width <= 0 || height <= 0) return
            rect.set(0f, 0f, width.toFloat(), height.toFloat())

            // Shimmer gradient sweeps from left to right across the bone.
            val shimmerWidth = width * 2f
            val dx = shimmerOffset * shimmerWidth - width * 0.5f

            paint.shader = LinearGradient(
                dx, 0f,
                dx + width.toFloat(), height.toFloat(),
                intArrayOf(baseColor, highlightColor, baseColor),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )

            val r = cornerRadiusPx.toFloat()
            canvas.drawRoundRect(rect, r, r, paint)
        }
    }
}
