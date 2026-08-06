package com.example.sportsxtreme.presentation.home

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.example.sportsxtreme.domain.model.LiveMatch
import java.util.Locale

/**
 * A single LIVE MATCH card used on the Home Screen.
 *
 * Renders the lightweight [LiveMatch] model using the original home-screen
 * scorecard treatment. The LIVE badge pulses subtly using an infinite alpha
 * animation.
 */
class LiveMatchCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    // SportsXtreme theme colors (mirrors HomeScreenView).
    private val primary = Color.rgb(193, 255, 0)
    private val cyan = Color.rgb(0, 210, 255)

    private lateinit var leagueText: TextView
    private lateinit var statusText: TextView
    private lateinit var liveDot: View
    private lateinit var teamAIcon: TextView
    private lateinit var teamBIcon: TextView
    private lateinit var teamAScore: TextView
    private lateinit var teamBScore: TextView
    private lateinit var teamAOvers: TextView
    private lateinit var teamBOvers: TextView
    private lateinit var crrText: TextView
    private lateinit var rrrText: TextView
    private lateinit var targetText: TextView
    private lateinit var batterText: TextView
    private lateinit var bowlerText: TextView
    private lateinit var noteText: TextView

    init {
        clipToOutline = true
        background = GradientDrawable().apply {
            cornerRadius = dp(14).toFloat()
            setColor(Color.rgb(8, 28, 58))
        }
        elevation = dp(12).toFloat()

        addView(ScorecardGlowOverlay(context), LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        ))
        addView(buildContent(), LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ))
    }

    private fun buildContent(): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(11), dp(14), dp(12))

            // Header: league name + status badge
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    leagueText = TextView(context).apply {
                        text = "LIVE MATCH"
                        setTextColor(Color.rgb(204, 217, 229))
                        textSize = 11f
                        letterSpacing = 0.08f
                        typeface = Typeface.DEFAULT_BOLD
                        includeFontPadding = false
                    }
                    addView(leagueText)
                    addView(TextView(context).apply {
                        text = "Friendly Match"
                        setTextColor(Color.rgb(106, 121, 137))
                        textSize = 8.2f
                        typeface = Typeface.DEFAULT_BOLD
                        includeFontPadding = false
                    }, LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dp(4)
                    })
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

                addView(statusBadge(), LinearLayout.LayoutParams(dp(58), dp(28)))
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

            // Teams row: Team A score | VS | Team B score
            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER
                orientation = LinearLayout.HORIZONTAL
                addView(teamPanel(isTeamA = true), LinearLayout.LayoutParams(0, dp(96), 1f))
                addView(FrameLayout(context).apply {
                    addView(TextView(context).apply {
                        text = "VS"
                        gravity = Gravity.CENTER
                        setTextColor(Color.rgb(7, 14, 20))
                        textSize = 10f
                        typeface = Typeface.DEFAULT_BOLD
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setColor(primary)
                        }
                    }, FrameLayout.LayoutParams(dp(40), dp(40), Gravity.CENTER))
                }, LinearLayout.LayoutParams(dp(54), LinearLayout.LayoutParams.MATCH_PARENT))
                addView(teamPanel(isTeamA = false), LinearLayout.LayoutParams(0, dp(96), 1f))
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(96)).apply {
                topMargin = dp(10)
            })

            // Stats row: CRR | RRR | TARGET
            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER
                crrText = statChip("CRR", "0.00")
                addView(crrText)
                rrrText = statChip("RRR", "--")
                addView(rrrText, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(32)).apply {
                    leftMargin = dp(8)
                })
                targetText = statChip("TARGET", "--")
                addView(targetText, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(32)).apply {
                    leftMargin = dp(8)
                })
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(30)).apply {
                topMargin = dp(9)
            })

            // Batter / Bowler row
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                batterText = playerChip("BAT", "—")
                batterText.visibility = View.GONE
                addView(batterText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                bowlerText = playerChip("BOWL", "—")
                bowlerText.visibility = View.GONE
                addView(bowlerText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(9)
            })

            // Status note (e.g. Need 14 runs from 16 balls / Innings Break)
            noteText = TextView(context).apply {
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(178, 191, 205))
                textSize = 9f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            }
            addView(noteText, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(8)
            })
        }
    }

    /** Status badge: 🟢 LIVE / Completed / Upcoming */
    private fun statusBadge(): View {
        return LinearLayout(context).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
            background = GradientDrawable().apply {
                cornerRadius = dp(14).toFloat()
                setColor(Color.TRANSPARENT)
            }
            liveDot = View(context).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.rgb(255, 62, 70))
                }
            }
            addView(liveDot, LinearLayout.LayoutParams(dp(7), dp(7)).apply {
                rightMargin = dp(5)
            })
            statusText = TextView(context).apply {
                text = "LIVE"
                setTextColor(Color.WHITE)
                textSize = 8.5f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            }
            addView(statusText)
        }
    }

    private fun teamPanel(isTeamA: Boolean): View {
        val iconAccent = if (isTeamA) primary else cyan
        return FrameLayout(context).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.argb(150, 23, 35, 47), Color.argb(160, 8, 15, 22))
            ).apply {
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), Color.argb(74, Color.red(iconAccent), Color.green(iconAccent), Color.blue(iconAccent)))
            }

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(6), dp(6), dp(6), dp(6))

                addView(TextView(context).apply {
                    text = if (isTeamA) "A" else "B"
                    gravity = Gravity.CENTER
                    setTextColor(iconAccent)
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.argb(42, Color.red(iconAccent), Color.green(iconAccent), Color.blue(iconAccent)))
                        setStroke(dp(1), Color.argb(140, Color.red(iconAccent), Color.green(iconAccent), Color.blue(iconAccent)))
                    }
                }, LinearLayout.LayoutParams(dp(32), dp(32)))

                if (isTeamA) {
                    teamAIcon = avatarText(iconAccent)
                    addView(teamAIcon, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(4) })
                    teamAScore = scoreText()
                    addView(teamAScore, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
                    teamAOvers = oversText()
                    addView(teamAOvers, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(1) })
                } else {
                    teamBIcon = avatarText(iconAccent)
                    addView(teamBIcon, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(4) })
                    teamBScore = scoreText()
                    addView(teamBScore, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
                    teamBOvers = oversText()
                    addView(teamBOvers, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(1) })
                }
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        }
    }

    private fun avatarText(accent: Int): TextView = TextView(context).apply {
        text = ""
        gravity = Gravity.CENTER
        setTextColor(accent)
        textSize = 8f
        typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = false
        maxLines = 1
    }

    private fun scoreText(): TextView = TextView(context).apply {
        text = "0/0"
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        textSize = 22f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
        includeFontPadding = false
    }

    private fun oversText(): TextView = TextView(context).apply {
        text = "0.0 OV"
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(125, 139, 154))
        textSize = 7.5f
        typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = false
    }

    private fun statChip(label: String, initial: String): TextView {
        return TextView(context).apply {
            text = "$label $initial"
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(34, 34, 34))
            textSize = 8f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
            setPadding(dp(9), 0, dp(9), 0)
            background = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                colors = intArrayOf(Color.WHITE, Color.rgb(226, 234, 240))
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
                setStroke(dp(1), Color.argb(150, 0, 0, 0))
            }
        }
    }

    private fun playerChip(label: String, initial: String): TextView {
        return TextView(context).apply {
            text = "$label  $initial"
            gravity = Gravity.START
            setTextColor(Color.rgb(178, 191, 205))
            textSize = 9f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
            setPadding(dp(6), dp(5), dp(6), dp(5))
            background = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                setColor(Color.argb(35, 0, 0, 0))
            }
        }
    }

    /** Binds a [LiveMatch] to this card and starts/stops the LIVE pulse. */
    fun bind(match: LiveMatch) {
        leagueText.text = match.tournamentName.ifBlank { "LIVE MATCH" }
        statusText.text = when {
            match.isLive -> "LIVE"
            match.isCompleted -> "Completed"
            else -> "Upcoming"
        }
        statusText.setTextColor(if (match.isLive) Color.rgb(255, 62, 70) else Color.WHITE)
        liveDot.visibility = if (match.isLive) View.VISIBLE else View.GONE

        teamAIcon.text = match.teamAShortName.ifBlank { match.teamAName }
        teamBIcon.text = match.teamBShortName.ifBlank { match.teamBName }
        val showScores = match.isLive || match.isCompleted
        teamAScore.text = "${match.score}/${match.wickets}"
        teamAScore.visibility = if (showScores) View.VISIBLE else View.GONE
        teamBScore.text = "${match.score}/${match.wickets}"
        teamBScore.visibility = if (showScores) View.VISIBLE else View.GONE
        teamAOvers.text = "${match.overs} OV"
        teamBOvers.text = "${match.overs} OV"

        crrText.text = String.format(Locale.US, "CRR %.2f", match.currentRunRate)
        rrrText.text = match.requiredRunRate?.let { String.format(Locale.US, "RRR %.2f", it) } ?: "RRR --"
        targetText.text = match.target?.let { "TARGET $it" } ?: "TARGET --"

        val batterLabel = match.strikerName?.let { "$it ${match.strikerRuns}(${match.strikerBalls})" } ?: "—"
        batterText.text = "BAT  $batterLabel"
        val bowlerLabel = match.bowlerName?.let { name ->
            "${name} ${match.bowlerOvers}-${match.bowlerRuns}-${match.bowlerWickets}"
        } ?: "—"
        bowlerText.text = "BOWL  $bowlerLabel"

        noteText.text = when {
            match.isLive && match.target != null && match.score < match.target ->
                "Need ${match.target - match.score} runs from ${remainingBallsText(match)}"
            match.isLive -> match.matchStatusNote ?: "Powerplay"
            match.isCompleted -> "Match Completed"
            match.isUpcoming -> "Starts at 4:30 PM"
            else -> ""
        }

        // Start the pulse only when LIVE.
        if (match.isLive) {
            startLivePulse()
        } else {
            stopLivePulse()
        }
    }

    private fun remainingBallsText(match: LiveMatch): String {
        val parts = match.overs.split(".")
        val completed = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val currentBall = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val total = (20 * 6) - (completed * 6 + currentBall)
        return "$total balls"
    }

    private var pulseAnimator: ObjectAnimator? = null

    private fun startLivePulse() {
        if (pulseAnimator?.isRunning == true) return
        liveDot.alpha = 1f
        pulseAnimator = ObjectAnimator.ofFloat(liveDot, View.ALPHA, 1f, 0.25f).apply {
            duration = 700L
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun stopLivePulse() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        liveDot.alpha = 1f
    }

    private class ScorecardGlowOverlay(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rect = RectF()
        private var radialShader: Shader? = null
        private var linearShader: Shader? = null

        override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
            super.onSizeChanged(width, height, oldWidth, oldHeight)
            if (width <= 0 || height <= 0) return

            val cardWidth = width.toFloat()
            val cardHeight = height.toFloat()
            radialShader = android.graphics.RadialGradient(
                cardWidth * 0.16f,
                cardHeight * 0.16f,
                cardWidth * 0.72f,
                intArrayOf(
                    Color.argb(96, 22, 96, 172),
                    Color.argb(36, 11, 58, 118),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.48f, 1f),
                Shader.TileMode.CLAMP
            )
            linearShader = LinearGradient(
                0f,
                0f,
                cardWidth * 0.52f,
                cardHeight * 0.34f,
                intArrayOf(Color.argb(58, 42, 116, 196), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )
        }

        override fun onDraw(canvas: Canvas) {
            if (width <= 0 || height <= 0) return

            val radius = 14f * resources.displayMetrics.density
            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            paint.shader = radialShader
            canvas.drawRoundRect(rect, radius, radius, paint)
            paint.shader = linearShader
            canvas.drawRoundRect(rect, radius, radius, paint)
            paint.shader = null
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
