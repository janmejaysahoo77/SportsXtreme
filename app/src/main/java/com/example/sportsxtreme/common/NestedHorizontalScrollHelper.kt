package com.example.sportsxtreme.common

import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * Prevents a vertical parent (ScrollView, SwipeRefreshLayout, etc.) from stealing
 * horizontal drag gestures from horizontally scrollable children.
 */
object NestedHorizontalScrollHelper {

    fun installOnScrollView(view: View) {
        val touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop
        var initialX = 0f
        var initialY = 0f
        var directionLocked = false
        var scrollingHorizontally = false

        view.setOnTouchListener { target, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.x
                    initialY = event.y
                    directionLocked = false
                    scrollingHorizontally = false
                    target.parent?.requestDisallowInterceptTouchEvent(false)
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = abs(event.x - initialX)
                    val deltaY = abs(event.y - initialY)
                    if (!directionLocked && (deltaX > touchSlop || deltaY > touchSlop)) {
                        directionLocked = true
                        scrollingHorizontally = deltaX > deltaY
                    }
                    if (directionLocked) {
                        target.parent?.requestDisallowInterceptTouchEvent(scrollingHorizontally)
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    directionLocked = false
                    scrollingHorizontally = false
                    target.parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }

    fun installOnRecyclerView(recyclerView: RecyclerView) {
        val touchSlop = ViewConfiguration.get(recyclerView.context).scaledTouchSlop
        var initialX = 0f
        var initialY = 0f
        var directionLocked = false
        var scrollingHorizontally = false

        recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = event.x
                        initialY = event.y
                        directionLocked = false
                        scrollingHorizontally = false
                        rv.parent?.requestDisallowInterceptTouchEvent(false)
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = abs(event.x - initialX)
                        val deltaY = abs(event.y - initialY)
                        if (!directionLocked && (deltaX > touchSlop || deltaY > touchSlop)) {
                            directionLocked = true
                            scrollingHorizontally = deltaX > deltaY
                        }
                        if (directionLocked) {
                            rv.parent?.requestDisallowInterceptTouchEvent(scrollingHorizontally)
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        directionLocked = false
                        scrollingHorizontally = false
                        rv.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                }
                return false
            }
        })
    }
}
