package com.example.sportsxtreme.presentation.home

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sportsxtreme.domain.model.LiveMatch

/**
 * RecyclerView adapter for the LIVE MATCHES cards on the Home Screen.
 *
 * Uses [DiffUtil] so only changed cards are rebound when the Firestore
 * SnapshotListener pushes a score update — no full list redraw.
 */
class LiveMatchAdapter(
    private val onMatchClick: (LiveMatch) -> Unit
) : ListAdapter<LiveMatch, LiveMatchAdapter.LiveMatchViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveMatchViewHolder {
        val cardView = LiveMatchCardView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(dp(parent, 320), ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                rightMargin = dp(parent, 12)
            }
        }
        return LiveMatchViewHolder(
            cardView,
            onMatchClick
        )
    }

    override fun onBindViewHolder(holder: LiveMatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LiveMatchViewHolder(
        private val cardView: LiveMatchCardView,
        private val onMatchClick: (LiveMatch) -> Unit
    ) : RecyclerView.ViewHolder(cardView) {
        fun bind(match: LiveMatch) {
            cardView.bind(match)
            cardView.setOnClickListener { onMatchClick(match) }
        }
    }

    companion object {
        private fun dp(parent: ViewGroup, value: Int): Int =
            (value * parent.resources.displayMetrics.density).toInt()

        private val DIFF = object : DiffUtil.ItemCallback<LiveMatch>() {
            override fun areItemsTheSame(oldItem: LiveMatch, newItem: LiveMatch): Boolean {
                return oldItem.matchId == newItem.matchId
            }

            override fun areContentsTheSame(oldItem: LiveMatch, newItem: LiveMatch): Boolean {
                return oldItem == newItem
            }
        }
    }
}
