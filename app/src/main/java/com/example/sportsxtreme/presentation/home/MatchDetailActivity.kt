package com.example.sportsxtreme.presentation.home

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sportsxtreme.domain.model.LiveMatch
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Match Detail Screen — opened when a user taps a Live Match card on the Home
 * Screen. Renders the complete live scorecard and keeps updating in real time
 * via the Firestore SnapshotListener.
 */
@AndroidEntryPoint
class MatchDetailActivity : ComponentActivity() {

    private val viewModel: MatchDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContentView())
        observeMatch()
    }

    private fun buildContentView(): View {
        return FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(1, 5, 9))
            addView(ScrollView(this@MatchDetailActivity).apply {
                isVerticalScrollBarEnabled = false
                addView(LinearLayout(this@MatchDetailActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(14), dp(14), dp(14), dp(24))
                    addView(backButton())
                    loadingBar = ProgressBar(this@MatchDetailActivity).apply { visibility = View.GONE }
                    addView(loadingBar, LinearLayout.LayoutParams(dp(42), dp(42)).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                        topMargin = dp(28)
                    })
                    errorText = TextView(this@MatchDetailActivity).apply {
                        visibility = View.GONE
                        gravity = Gravity.CENTER
                        setTextColor(Color.rgb(178, 191, 205))
                        textSize = 13f
                    }
                    addView(errorText, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(24) })
                    detailContainer = LinearLayout(this@MatchDetailActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        visibility = View.GONE
                    }
                    addView(detailContainer, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) })
                }, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        }
    }

    private fun backButton(): View {
        return LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(8))
            isClickable = true
            isFocusable = true
            setOnClickListener { finish() }
            addView(TextView(this@MatchDetailActivity).apply {
                text = "<"
                setTextColor(Color.WHITE)
                textSize = 28f
                gravity = Gravity.CENTER
                includeFontPadding = false
                background = GradientDrawable().apply {
                    cornerRadius = dp(8).toFloat()
                    setColor(Color.rgb(7, 14, 18))
                    setStroke(dp(1), Color.argb(60, 255, 255, 255))
                }
            }, LinearLayout.LayoutParams(dp(40), dp(40)))
            addView(TextView(this@MatchDetailActivity).apply {
                text = "Live Match Details"
                setTextColor(Color.WHITE)
                textSize = 17f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = dp(12) })
        }
    }

    private lateinit var loadingBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var detailContainer: LinearLayout

    private fun observeMatch() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isLoading && state.match == null) {
                        loadingBar.visibility = View.VISIBLE
                        errorText.visibility = View.GONE
                        detailContainer.visibility = View.GONE
                    } else if (state.match != null) {
                        loadingBar.visibility = View.GONE
                        errorText.visibility = View.GONE
                        detailContainer.visibility = View.VISIBLE
                        renderMatch(state.match)
                    } else {
                        loadingBar.visibility = View.GONE
                        detailContainer.visibility = View.GONE
                        errorText.visibility = View.VISIBLE
                        errorText.text = state.errorMessage ?: "Match not found"
                    }
                }
            }
        }
    }

    /** Renders the complete live scorecard from the lightweight LiveMatch model. */
    private fun renderMatch(match: LiveMatch) {
        detailContainer.removeAllViews()

        detailContainer.addView(TextView(this).apply {
            text = match.tournamentName.ifBlank { "LIVE MATCH" }
            setTextColor(Color.rgb(204, 217, 229))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
        })

        detailContainer.addView(TextView(this).apply {
            text = when {
                match.isLive -> "\uD83D\uDFE2 LIVE"
                match.isCompleted -> "Completed"
                else -> "Upcoming"
            }
            setTextColor(if (match.isLive) Color.rgb(255, 62, 70) else Color.WHITE)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(4) })

        detailContainer.addView(scorePanel(match), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) })

        detailContainer.addView(statRow("CRR", String.format(Locale.US, "%.2f", match.currentRunRate)))
        detailContainer.addView(statRow("RRR", match.requiredRunRate?.let { String.format(Locale.US, "%.2f", it) } ?: "--"))
        detailContainer.addView(statRow("TARGET", match.target?.toString() ?: "--"))
        match.strikerName?.let { name ->
            detailContainer.addView(statRow("BAT", "$name  ${match.strikerRuns} (${match.strikerBalls})"))
        }
        match.bowlerName?.let { name ->
            detailContainer.addView(statRow("BOWL", "$name  ${match.bowlerOvers}-${match.bowlerRuns}-${match.bowlerWickets}"))
        }

        detailContainer.addView(TextView(this).apply {
            text = when {
                match.isLive && match.target != null && match.score < match.target -> "Need ${match.target - match.score} runs to win"
                match.isLive -> match.matchStatusNote ?: "Powerplay"
                match.isCompleted -> "Match Completed"
                match.isUpcoming -> "Starts at 4:30 PM"
                else -> ""
            }
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(255, 77, 94))
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) })
    }

    /** Large hero panel: team A score vs team B score + overs. */
    private fun scorePanel(match: LiveMatch): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = dp(14).toFloat()
                setColor(Color.rgb(8, 28, 58))
                setStroke(dp(1), Color.argb(70, 193, 255, 0))
            }
            setPadding(dp(14), dp(16), dp(14), dp(16))

            addView(LinearLayout(this@MatchDetailActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                addView(TextView(this@MatchDetailActivity).apply {
                    text = match.teamAShortName.ifBlank { match.teamAName }
                    setTextColor(Color.rgb(193, 255, 0))
                    textSize = 13f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                })
                addView(TextView(this@MatchDetailActivity).apply {
                    text = if (match.isLive || match.isCompleted) "${match.score}/${match.wickets}" else "—"
                    setTextColor(Color.WHITE)
                    textSize = 30f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                    includeFontPadding = false
                })
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

            addView(TextView(this@MatchDetailActivity).apply {
                text = "VS"
                setTextColor(Color.rgb(193, 255, 0))
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            })

            addView(LinearLayout(this@MatchDetailActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                addView(TextView(this@MatchDetailActivity).apply {
                    text = match.teamBShortName.ifBlank { match.teamBName }
                    setTextColor(Color.rgb(0, 210, 255))
                    textSize = 13f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                })
                addView(TextView(this@MatchDetailActivity).apply {
                    text = "OV ${match.overs}"
                    setTextColor(Color.rgb(125, 139, 154))
                    textSize = 11f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                })
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
    }

    private fun statRow(label: String, value: String): View {
        return LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                setColor(Color.rgb(7, 14, 18))
                setStroke(dp(1), Color.argb(40, 255, 255, 255))
            }
            setPadding(dp(14), dp(12), dp(14), dp(12))
            addView(TextView(this@MatchDetailActivity).apply {
                text = label
                setTextColor(Color.rgb(130, 145, 142))
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            })
            addView(TextView(this@MatchDetailActivity).apply {
                text = value
                gravity = Gravity.END
                setTextColor(Color.WHITE)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = dp(16) })
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val EXTRA_MATCH_ID = "extra_match_id"
    }
}