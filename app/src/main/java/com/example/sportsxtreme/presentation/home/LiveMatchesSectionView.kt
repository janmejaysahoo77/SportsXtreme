package com.example.sportsxtreme.presentation.home

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sportsxtreme.domain.model.LiveMatch
import kotlinx.coroutines.launch

/**
 * Replaces the old hardcoded scorecard section on the Home Screen.
 *
 * Renders the "🏏 LIVE MATCHES" header, a vertical [RecyclerView] of live
 * match cards, and the loading / empty / offline states. All data flows from
 * [LiveMatchViewModel] which combines the Firestore SnapshotListener (real-time)
 * with the Room cache (offline fallback).
 */
class LiveMatchesSectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val primary = Color.rgb(193, 255, 0)
    private val muted = Color.rgb(130, 145, 142)
    private val emptyColor = Color.rgb(150, 165, 162)

    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholderContainer: FrameLayout
    private lateinit var placeholderTitle: TextView
    private lateinit var placeholderSubtitle: TextView
    private lateinit var loadingBar: ProgressBar
    private lateinit var offlineNote: TextView

    private var adapter: LiveMatchAdapter? = null
    private var viewModel: LiveMatchViewModel? = null

    init {
        orientation = VERTICAL
        buildContent()
    }

    private fun buildContent() {
        // Header: 🏏 LIVE MATCHES
        addView(LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(context).apply {
                text = "🏏 LIVE MATCHES"
                setTextColor(Color.WHITE)
                text = "Matches Near You"
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(TextView(context).apply {
                text = "View All"
                setTextColor(primary)
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
                setPadding(dp(10), dp(6), 0, dp(6))
                isClickable = true
                isFocusable = true
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(6)
        })

        // RecyclerView for the live match cards (vertical list).
        recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            isNestedScrollingEnabled = false
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            setHasFixedSize(false)
            clipToPadding = false
        }
        addView(
            recyclerView,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        )

        // Placeholder container shown while loading or when there are no matches.
        placeholderContainer = FrameLayout(context).apply {
            visibility = View.GONE
            addView(LinearLayout(context).apply {
                orientation = VERTICAL
                gravity = Gravity.CENTER

                loadingBar = ProgressBar(context).apply {
                    visibility = View.GONE
                }
                addView(loadingBar, LinearLayout.LayoutParams(dp(34), dp(34)))

                placeholderTitle = TextView(context).apply {
                    text = "No Live Matches Right Now"
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    textSize = 13f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                }
                addView(placeholderTitle, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(10)
                })

                placeholderSubtitle = TextView(context).apply {
                    text = "Check Upcoming Matches"
                    gravity = Gravity.CENTER
                    setTextColor(emptyColor)
                    textSize = 10f
                    includeFontPadding = false
                }
                addView(placeholderSubtitle, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(4)
                })
            }, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            })
        }
        addView(
            placeholderContainer,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(176))
        )

        // Offline indicator.
        offlineNote = TextView(context).apply {
            text = "Showing last synced scores (offline)"
            gravity = Gravity.CENTER
            setTextColor(Color.argb(160, 255, 214, 64))
            textSize = 9f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
            visibility = View.GONE
            background = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setColor(Color.argb(24, 255, 214, 64))
            }
            setPadding(dp(8), dp(6), dp(8), dp(6))
        }
        addView(offlineNote, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(6)
        })
    }

    /**
     * Attaches the [LiveMatchViewModel] and starts observing its StateFlow.
     *
     * The observation is scoped to the attached lifecycle so the Firestore
     * SnapshotListener lives only while the Home tab is visible.
     */
    fun bind(viewModel: LiveMatchViewModel) {
        if (this.viewModel === viewModel) return
        this.viewModel = viewModel

        adapter = LiveMatchAdapter { match -> onMatchClick(match) }
        recyclerView.adapter = adapter

        // The context is always MainActivity when the Home tab is shown.
        val lifecycleOwner = context as LifecycleOwner
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: LiveMatchesUiState) {
        val matches = state.matches

        // Offline note: show only when we still have cached matches but Firestore failed.
        offlineNote.visibility = if (state.isOffline && matches.isNotEmpty()) View.VISIBLE else View.GONE

        when {
            // Loading: show spinner only when we have nothing cached yet.
            state.isLoading && matches.isEmpty() -> {
                placeholderContainer.visibility = View.VISIBLE
                loadingBar.visibility = View.VISIBLE
                placeholderTitle.text = "Loading Live Matches…"
                placeholderSubtitle.text = "Fetching the latest scores"
                recyclerView.visibility = View.GONE
            }

            // Empty state.
            matches.isEmpty() -> {
                placeholderContainer.visibility = View.VISIBLE
                loadingBar.visibility = View.GONE
                placeholderTitle.text = "No Live Matches Right Now"
                placeholderSubtitle.text = "Check Upcoming Matches"
                recyclerView.visibility = View.GONE
            }

            // Live list.
            else -> {
                placeholderContainer.visibility = View.GONE
                loadingBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter?.submitList(matches)
            }
        }
    }

    private fun onMatchClick(match: LiveMatch) {
        context.startActivity(
            android.content.Intent(context, com.example.sportsxtreme.presentation.scoring.ScorecardActivity::class.java)
        )
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
