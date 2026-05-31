package com.example.sportsxtreme

import android.content.Intent
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.max

class HomeScreenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val topMode: TopMode = TopMode.SPORTS
) : FrameLayout(context, attrs) {

    enum class TopMode { SPORTS, MEDIA, CART }

    private data class NavItem(val label: String, val icon: NavIconView.Icon)

    private val primary = Color.rgb(193, 255, 0)
    private val cyan = Color.rgb(0, 210, 255)
    private val bg = Color.rgb(1, 5, 9)
    private val panel = Color.rgb(7, 14, 18)
    private val muted = Color.rgb(130, 145, 142)
    private val navItems = listOf(
        NavItem("Home", NavIconView.Icon.HOME),
        NavItem("Stats", NavIconView.Icon.BARS),
        NavItem("World", NavIconView.Icon.COMPASS),
        NavItem("Community", NavIconView.Icon.USERS),
        NavItem("Shop", NavIconView.Icon.CART)
    )
    private var selectedIndex = 0
    private lateinit var contentHolder: FrameLayout
    private lateinit var navRow: LinearLayout
    private val cachedTabs = mutableMapOf<Int, View>()

    init {
        setBackgroundColor(bg)
        addView(HomeBackgroundView(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(createShell(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun createShell(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            // Top padding removed to allow top bar to touch the top edge cleanly

            contentHolder = FrameLayout(context)
            addView(contentHolder, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

            navRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(8), dp(8), dp(6))
                background = GradientDrawable().apply {
                    setColor(Color.rgb(5, 9, 15))
                    setStroke(dp(1), Color.argb(60, 255, 255, 255))
                }
            }
            addView(navRow, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(72)))

            buildNav()
            showTab(0)
        }
    }

    private fun buildNav() {
        navRow.removeAllViews()
        navItems.forEachIndexed { index, item ->
            navRow.addView(navCell(item, index), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))
        }
    }

    private fun navCell(item: NavItem, index: Int): View {
        val active = index == selectedIndex
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            background = GradientDrawable().apply {
                cornerRadius = dp(9).toFloat()
                setColor(if (active) primary else Color.TRANSPARENT)
            }
            addView(NavIconView(context, item.icon).apply {
                setTint(if (active) Color.rgb(8, 16, 7) else Color.rgb(118, 127, 136))
            }, LinearLayout.LayoutParams(dp(22), dp(22)))
            addView(TextView(context).apply {
                text = item.label
                gravity = Gravity.CENTER
                setTextColor(if (active) Color.rgb(8, 16, 7) else muted)
                textSize = 8.5f
                includeFontPadding = false
                typeface = Typeface.create(Typeface.SANS_SERIF, if (active) Typeface.BOLD else Typeface.NORMAL)
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(5)
            })
            setOnClickListener { showTab(index) }
        }
    }

    private fun showTab(index: Int) {
        selectedIndex = index
        buildNav()
        contentHolder.removeAllViews()
        val view = cachedTabs.getOrPut(index) {
            if (index == 0) createHomeContent(context) else createComingSoon(context, navItems[index])
        }
        contentHolder.addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun refreshAfterResume() {
        post {
            invalidate()
        }
    }

    private fun createHomeContent(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            
            addView(topBar(context), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

            val scrollContainer = FrameLayout(context).apply {
                val scroll = ScrollView(context).apply {
                    clipToPadding = false
                    setPadding(0, 0, 0, dp(12))

                    val content = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(14), dp(6), dp(14), dp(14))
                    }

                    when (topMode) {
                        TopMode.SPORTS -> {
                            content.addView(locationRow(context))
                            content.addView(scoreCardsSection(context), blockParams(top = 6))
                            content.addView(proPassCardsSection(context), blockParams(top = 8))
                            content.addView(personalizeGearSection(context), blockParams(top = 10))
                            content.addView(sectionHeader(context, "Sports Feed", null), blockParams(top = 14))
                            content.addView(feedCard(context), blockParams(top = 8))
                        }
                        TopMode.MEDIA -> {
                            content.addView(simpleTopModeScreen(context, context.getString(R.string.str_xtrememedia)))
                        }
                        TopMode.CART -> {
                            content.addView(simpleTopModeScreen(context, context.getString(R.string.str_xtremecart)))
                        }
                    }

                    addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                }
                addView(scroll, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

                val shadowGlow = View(context).apply {
                    background = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(Color.argb(140, 0, 0, 0), Color.TRANSPARENT)
                    )
                }
                addView(shadowGlow, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(14)))
                
                val primaryGlow = View(context).apply {
                    background = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(Color.argb(45, 0, 126, 255), Color.TRANSPARENT)
                    )
                }
                addView(primaryGlow, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(6)))
            }
            addView(scrollContainer, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        }
    }

    private fun topBar(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL

            // Premium app bar style
            background = GradientDrawable().apply {
                setColor(Color.rgb(6, 12, 17)) // Solid dark color to separate from body
            }
            // Removed standard elevation to use custom gradient shadow/glow below
            setPadding(dp(16), dp(24), dp(16), dp(18)) // Taller top bar for the mode buttons

            addView(xtremeTopBarButtons(context), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)))

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                addView(TopIconView(context, TopIconView.Icon.MENU).apply {
                    setTint(Color.WHITE)
                }, LinearLayout.LayoutParams(dp(22), dp(22)))
                addView(sxLogo(context), LinearLayout.LayoutParams(dp(64), dp(36)).apply {
                    leftMargin = dp(12)
                })
                addView(topBarBrandTitle(context), LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    leftMargin = dp(7)
                })
                addView(View(context), LinearLayout.LayoutParams(0, 1, 1f))
                addView(TopIconView(context, TopIconView.Icon.SEARCH).apply {
                    setTint(Color.WHITE)
                }, LinearLayout.LayoutParams(dp(21), dp(21)).apply { leftMargin = dp(10) })
                addView(TopIconView(context, TopIconView.Icon.BELL).apply {
                    setTint(Color.WHITE)
                }, LinearLayout.LayoutParams(dp(21), dp(21)).apply { leftMargin = dp(10) })
                addView(TopIconView(context, TopIconView.Icon.MESSAGE).apply {
                    setTint(Color.WHITE)
                }, LinearLayout.LayoutParams(dp(21), dp(21)).apply { leftMargin = dp(10) })
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(42)).apply {
                topMargin = dp(20)
            })
        }
    }

    private fun topBarBrandTitle(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            addView(TextView(context).apply {
                text = "Sports"
                setTextColor(Color.rgb(232, 241, 246))
                textSize = 15f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
                setShadowLayer(dp(2).toFloat(), 0f, 0f, Color.argb(95, 0, 120, 255))
            })

            addView(TextView(context).apply {
                text = "Xtreme"
                setTextColor(Color.rgb(0, 127, 255))
                textSize = 16.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
                setShadowLayer(dp(2).toFloat(), 0f, 0f, Color.argb(115, 0, 120, 255))
            })
        }
    }

    private fun xtremeTopBarButtons(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            addView(xtremeModeButton(
                context = context,
                label = context.getString(R.string.str_sportsxtreme),
                selected = topMode == TopMode.SPORTS,
                imageRes = R.drawable.appicon2
            ).apply {
                setOnClickListener {
                    (context as? MainActivity)?.showHomeScreen()
                }
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))

            addView(xtremeModeButton(
                context = context,
                label = context.getString(R.string.str_xtrememedia),
                selected = topMode == TopMode.MEDIA,
                imageRes = R.drawable.xtrememediaicon
            ).apply {
                setOnClickListener {
                    (context as? MainActivity)?.showXtremeMediaScreen()
                }
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                leftMargin = dp(7)
            })

            addView(xtremeModeButton(
                context = context,
                label = context.getString(R.string.str_xtremecart),
                selected = topMode == TopMode.CART,
                navIcon = NavIconView.Icon.CART
            ).apply {
                setOnClickListener {
                    (context as? MainActivity)?.showXtremeCartScreen()
                }
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                leftMargin = dp(7)
            })
        }
    }

    private fun sxLogo(context: Context): View {
        return ImageView(context).apply {
            setImageResource(R.drawable.appicon)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    private fun locationRow(context: Context): View {
        return LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            setPadding(0, 0, 0, dp(4))
            addView(LocationPinView(context).apply {
                setTint(primary)
            }, LinearLayout.LayoutParams(dp(17), dp(17)))
            addView(TextView(context).apply {
                text = context.getString(R.string.str_madanpurbhubaneswar)
                setTextColor(Color.rgb(210, 224, 220))
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                leftMargin = dp(7)
            })
        }
    }

    private fun xtremeMediaCard(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = dp(7).toFloat()
                setColor(Color.rgb(7, 15, 25))
                setStroke(dp(1), Color.argb(45, 255, 255, 255))
            }
            setPadding(dp(8), dp(10), dp(8), dp(10))

            addView(xtremeModeButton(
                context = context,
                label = context.getString(R.string.str_sportsxtreme),
                selected = topMode == TopMode.SPORTS,
                imageRes = R.drawable.appicon2
            ).apply {
                setOnClickListener {
                    (context as? MainActivity)?.showHomeScreen()
                }
            }, LinearLayout.LayoutParams(0, dp(48), 1f))

            addView(xtremeModeButton(
                context = context,
                label = context.getString(R.string.str_xtrememedia),
                selected = topMode == TopMode.MEDIA,
                imageRes = R.drawable.xtrememediaicon
            ).apply {
                setOnClickListener {
                    (context as? MainActivity)?.showXtremeMediaScreen()
                }
            }, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                leftMargin = dp(7)
            })

            addView(xtremeModeButton(
                context = context,
                label = context.getString(R.string.str_xtremecart),
                selected = topMode == TopMode.CART,
                navIcon = NavIconView.Icon.CART
            ).apply {
                setOnClickListener {
                    (context as? MainActivity)?.showXtremeCartScreen()
                }
            }, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                leftMargin = dp(7)
            })
        }
    }

    private fun xtremeModeButton(
        context: Context,
        label: String,
        selected: Boolean,
        imageRes: Int? = null,
        navIcon: NavIconView.Icon? = null
    ): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
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
            setPadding(dp(3), 0, dp(3), 0)

            imageRes?.let { resId ->
                addView(ImageView(context).apply {
                    setImageResource(resId)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    adjustViewBounds = true
                }, LinearLayout.LayoutParams(dp(22), dp(22)).apply {
                    rightMargin = dp(4)
                })
            }

            navIcon?.let { icon ->
                addView(NavIconView(context, icon).apply {
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

    private fun simpleTopModeScreen(context: Context, title: String): View {
        return FrameLayout(context).apply {
            setPadding(0, dp(90), 0, 0)
            addView(TextView(context).apply {
                text = title
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                textSize = 26f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP))
        }
    }

    private fun premiumCard(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(13), dp(10), dp(13), dp(10))
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.rgb(17, 57, 33), Color.rgb(7, 19, 34))
            ).apply {
                cornerRadius = dp(7).toFloat()
                setStroke(dp(1), Color.argb(95, 0, 126, 255))
            }
            addView(FrameLayout(context).apply {
                background = GradientDrawable().apply {
                    cornerRadius = dp(8).toFloat()
                    setColor(Color.argb(95, 193, 255, 0))
                    setStroke(dp(1), Color.argb(120, 193, 255, 0))
                }
                addView(TopIconView(context, TopIconView.Icon.MIC).apply {
                    setTint(Color.rgb(3, 20, 16))
                }, FrameLayout.LayoutParams(dp(20), dp(20), Gravity.CENTER))
            }, LinearLayout.LayoutParams(dp(38), dp(38)))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(TextView(context).apply {
                    text = context.getString(R.string.str_upgrade_to_premium)
                    setTextColor(Color.WHITE)
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                })
                addView(TextView(context).apply {
                    text = context.getString(R.string.str_adfree_exclusive_con)
                    setTextColor(Color.rgb(210, 224, 220))
                    textSize = 8.8f
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(3)
                })
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = dp(10)
            })
            addView(TextView(context).apply {
                text = context.getString(R.string.str_go_pro)
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(6, 15, 6))
                textSize = 8f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
                background = GradientDrawable().apply {
                    cornerRadius = dp(14).toFloat()
                    setColor(primary)
                }
            }, LinearLayout.LayoutParams(dp(58), dp(26)))
        }
    }

    private fun scoreCardsSection(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(TextView(context).apply {
                    text = "Matches Near You"
                    setTextColor(Color.WHITE)
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                addView(TextView(context).apply {
                    text = context.getString(R.string.str_view_all)
                    setTextColor(primary)
                    textSize = 10f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                    isClickable = true
                    isFocusable = true
                    setPadding(dp(10), dp(6), 0, dp(6))
                    setOnClickListener {
                        context.startActivity(Intent(context, ViewAllScoreCardActivity::class.java))
                    }
                })
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(5)
            })

            addView(HorizontalScrollView(context).apply {
                isHorizontalScrollBarEnabled = false
                clipToPadding = false
                overScrollMode = View.OVER_SCROLL_NEVER
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 0, dp(14), dp(2))
                    addView(heroScoreCard(context), LinearLayout.LayoutParams(dp(320), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        rightMargin = dp(12)
                    })
                    addView(heroScoreCard(
                        context,
                        league = "CRICKET PREMIER CUP",
                        round = "Qualifier - Match 12",
                        leftName = "BBS",
                        leftScore = "96/2",
                        leftOvers = "11.3 OV",
                        rightName = "KDP",
                        rightScore = "94/7",
                        rightOvers = "15.0 OV",
                        target = "148",
                        rrr = "7.86",
                        win = "BBS 68%",
                        note = "BBS need 52 from 51 balls"
                    ), LinearLayout.LayoutParams(dp(320), LinearLayout.LayoutParams.WRAP_CONTENT))
                }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun heroScoreCard(
        context: Context,
        league: String = "VALORANT PRO LEAGUE",
        round: String = "Semi-final - Match 07",
        leftName: String = "NSC",
        leftScore: String = "142/4",
        leftOvers: String = "18.4 OV",
        rightName: String = "VCR",
        rightScore: String = "138/6",
        rightOvers: String = "18.1 OV",
        target: String = "156",
        rrr: String = "8.42",
        win: String = "NSC 61%",
        note: String = "VCR chose to bowl - Powerplay complete"
    ): View {
        return FrameLayout(context).apply {
            isClickable = true
            isFocusable = true
            setOnClickListener {
                context.startActivity(Intent(context, ScorecardActivity::class.java).apply {
                    putExtra(ScorecardActivity.EXTRA_LEAGUE, league)
                    putExtra(ScorecardActivity.EXTRA_ROUND, round)
                    putExtra(ScorecardActivity.EXTRA_LEFT_NAME, leftName)
                    putExtra(ScorecardActivity.EXTRA_LEFT_SCORE, leftScore)
                    putExtra(ScorecardActivity.EXTRA_LEFT_OVERS, leftOvers)
                    putExtra(ScorecardActivity.EXTRA_RIGHT_NAME, rightName)
                    putExtra(ScorecardActivity.EXTRA_RIGHT_SCORE, rightScore)
                    putExtra(ScorecardActivity.EXTRA_RIGHT_OVERS, rightOvers)
                    putExtra(ScorecardActivity.EXTRA_TARGET, target)
                    putExtra(ScorecardActivity.EXTRA_RRR, rrr)
                    putExtra(ScorecardActivity.EXTRA_WIN, win)
                    putExtra(ScorecardActivity.EXTRA_NOTE, note)
                })
            }
            elevation = dp(14).toFloat()
            translationZ = dp(8).toFloat()
            background = GradientDrawable().apply {
                cornerRadius = dp(14).toFloat()
                setColor(Color.rgb(8, 28, 58))
            }
            addView(ScoreCardGlowView(context), FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(11), dp(14), dp(12))

                addView(LinearLayout(context).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    addView(LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        addView(TextView(context).apply {
                            text = league
                            setTextColor(Color.rgb(204, 217, 229))
                            textSize = 10.5f
                            letterSpacing = 0.1f
                            typeface = Typeface.DEFAULT_BOLD
                            includeFontPadding = false
                        })
                        addView(TextView(context).apply {
                            text = round
                            setTextColor(Color.rgb(106, 121, 137))
                            textSize = 8.2f
                            typeface = Typeface.DEFAULT_BOLD
                            includeFontPadding = false
                        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                            topMargin = dp(4)
                        })
                    }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

                    addView(LiveBadgeView(context), LinearLayout.LayoutParams(dp(58), dp(28)))
                })

                addView(LinearLayout(context).apply {
                    gravity = Gravity.CENTER
                    orientation = LinearLayout.HORIZONTAL
                    addView(scoreTeam(context, leftName, leftScore, leftOvers, TopIconView.Icon.SHIELD, primary), LinearLayout.LayoutParams(0, dp(96), 1f))
                    addView(FrameLayout(context).apply {
                        addView(TextView(context).apply {
                            text = context.getString(R.string.str_vs)
                            gravity = Gravity.CENTER
                            setTextColor(Color.rgb(7, 14, 20))
                            textSize = 10f
                            letterSpacing = 0.08f
                            typeface = Typeface.DEFAULT_BOLD
                            includeFontPadding = false
                            background = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setColor(Color.rgb(193, 255, 0))
                                setStroke(dp(1), Color.argb(130, 255, 255, 255))
                            }
                        }, FrameLayout.LayoutParams(dp(40), dp(40), Gravity.CENTER))
                    }, LinearLayout.LayoutParams(dp(54), LinearLayout.LayoutParams.MATCH_PARENT))
                    addView(scoreTeam(context, rightName, rightScore, rightOvers, TopIconView.Icon.BOLT, cyan), LinearLayout.LayoutParams(0, dp(96), 1f))
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(96)).apply {
                    topMargin = dp(10)
                })

                addView(LinearLayout(context).apply {
                    gravity = Gravity.CENTER
                    addView(scoreStatChip(context, "TARGET", target))
                    addView(scoreStatChip(context, "RRR", rrr), LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(32)).apply {
                        leftMargin = dp(8)
                    })
                    addView(scoreStatChip(context, "WIN", win), LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(32)).apply {
                        leftMargin = dp(8)
                    })
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(30)).apply {
                    topMargin = dp(9)
                })

                addView(TextView(context).apply {
                    text = note
                    gravity = Gravity.CENTER
                    setTextColor(Color.rgb(178, 191, 205))
                    textSize = 9f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(8)
                })
            }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        }
    }

    private fun scoreTeam(context: Context, name: String, score: String, overs: String, icon: TopIconView.Icon, accent: Int): View {
        return FrameLayout(context).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.argb(150, 23, 35, 47), Color.argb(160, 8, 15, 22))
            ).apply {
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), Color.argb(74, Color.red(accent), Color.green(accent), Color.blue(accent)))
            }
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(7), dp(8), dp(7))
                addView(FrameLayout(context).apply {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.argb(42, Color.red(accent), Color.green(accent), Color.blue(accent)))
                        setStroke(dp(1), Color.argb(140, Color.red(accent), Color.green(accent), Color.blue(accent)))
                    }
                    addView(TopIconView(context, icon).apply {
                        setTint(accent)
                    }, FrameLayout.LayoutParams(dp(22), dp(22), Gravity.CENTER))
                }, LinearLayout.LayoutParams(dp(34), dp(34)))
                addView(TextView(context).apply {
                    text = name
                    gravity = Gravity.CENTER
                    setTextColor(accent)
                    textSize = 9f
                    letterSpacing = 0.08f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(5)
                })
                addView(TextView(context).apply {
                    text = score
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    textSize = 26f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
                addView(TextView(context).apply {
                    text = overs
                    gravity = Gravity.CENTER
                    setTextColor(Color.rgb(125, 139, 154))
                    textSize = 7.5f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(1)
                })
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        }
    }

    private fun scoreStatChip(context: Context, label: String, value: String): View {
        return LinearLayout(context).apply {
            gravity = Gravity.CENTER
            setPadding(dp(9), 0, dp(9), 0)
            background = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
                setStroke(dp(1), Color.argb(150, 0, 0, 0))
            }
            addView(TextView(context).apply {
                text = label
                setTextColor(Color.rgb(34, 34, 34))
                textSize = 7f
                letterSpacing = 0.08f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            })
            addView(TextView(context).apply {
                text = value
                setTextColor(Color.BLACK)
                textSize = 8.5f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                leftMargin = dp(5)
            })
        }
    }

    private fun proPassCardsSection(context: Context): View {
        val red = Color.rgb(255, 62, 70)
        val purple = Color.rgb(156, 82, 255)
        val gold = Color.rgb(255, 215, 0)
        return HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            clipToPadding = false
            overScrollMode = View.OVER_SCROLL_NEVER
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 0, dp(14), dp(2))
                addView(
                    proPassCard(
                        context = context,
                        accent = primary,
                        title = "PRO",
                        memberLabel = "PRO MEMBER",
                        oldPrice = "49",
                        price = context.getString(R.string.str_19),
                        priceSuffix = context.getString(R.string.str_month),
                        baseColor = Color.rgb(5, 8, 11),
                        features = listOf(
                            Triple(ProIconView.Icon.AD_BLOCK, "ZERO AD", "INTERRUPTIONS"),
                            Triple(ProIconView.Icon.FOUR_K, "4K ULTRA HDR", "STREAMING"),
                            Triple(ProIconView.Icon.STATS, "PREMIUM INSIDER", "STATS")
                        )
                    ),
                    LinearLayout.LayoutParams(dp(332), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        rightMargin = dp(12)
                    }
                )
                addView(
                    proPassCard(
                        context = context,
                        accent = red,
                        title = "ELITE",
                        memberLabel = context.getString(R.string.str_elite_member),
                        oldPrice = "99",
                        price = "\u20B949",
                        priceSuffix = "/mo",
                        baseColor = Color.rgb(17, 5, 7),
                        features = listOf(
                            Triple(ProIconView.Icon.AD_BLOCK, "1. NO INTERSTITIAL", "AD"),
                            Triple(ProIconView.Icon.MEDAL, "2. STORE", "DISCOUNT"),
                            Triple(ProIconView.Icon.FOUR_K, "3. WATCH LIVE", "STREAMING"),
                            Triple(ProIconView.Icon.STATS, "4. PREMIUM MATCH", "INSIGHTS"),
                            Triple(ProIconView.Icon.ARROW, "5. AFTER MATCH", "HIGHLIGHTS")
                        )
                    ),
                    LinearLayout.LayoutParams(dp(332), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        rightMargin = dp(12)
                    }
                )
                addView(
                    proPassCard(
                        context = context,
                        accent = purple,
                        title = "LEGEND",
                        memberLabel = "LEGEND MEMBER",
                        oldPrice = "299",
                        price = "\u20B9199",
                        priceSuffix = "/Month",
                        baseColor = Color.rgb(13, 6, 22),
                        features = listOf(
                            Triple(ProIconView.Icon.AD_BLOCK, "1. NOT A", "SINGLE AD"),
                            Triple(ProIconView.Icon.MEDAL, "2. STORE", "DISCOUNT"),
                            Triple(ProIconView.Icon.FOUR_K, "3. WATCH LIVE", "STREAMING"),
                            Triple(ProIconView.Icon.STATS, "4. PREMIUM MATCH", "INSIGHTS"),
                            Triple(ProIconView.Icon.ARROW, "5. AFTER MATCH", "HIGHLIGHTS"),
                            Triple(ProIconView.Icon.STATS, "6. MATCH PLANNING", "WITH AI COACH"),
                            Triple(ProIconView.Icon.MEDAL, "7. AI PERSONAL COACH", "FROM YOUR STATS"),
                            Triple(ProIconView.Icon.ARROW, "8. DRS/BALL/SPEED", "TRACKING")
                        )
                    ),
                    LinearLayout.LayoutParams(dp(620), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        rightMargin = dp(12)
                    }
                )
                addView(
                    proPassCard(
                        context = context,
                        accent = gold,
                        title = "HALL OF FAME",
                        passLabel = "",
                        memberLabel = "VIP MEMBER",
                        oldPrice = "",
                        price = "\u20B9499",
                        priceSuffix = "/1yr",
                        baseColor = Color.rgb(3, 3, 3),
                        features = listOf(
                            Triple(ProIconView.Icon.MEDAL, "1. VERIFIED", "BADGE"),
                            Triple(ProIconView.Icon.AD_BLOCK, "2. NO", "ADS"),
                            Triple(ProIconView.Icon.MEDAL, "3. OFFLINE EVENT", "INVITE + AWARD"),
                            Triple(ProIconView.Icon.FOUR_K, "4. LIVE", "STREAMING"),
                            Triple(ProIconView.Icon.MEDAL, "5. HUGE STORE", "DISCOUNT"),
                            Triple(ProIconView.Icon.STATS, "6. PREMIUM MATCH", "INSIGHTS"),
                            Triple(ProIconView.Icon.ARROW, "7. AFTER MATCH", "HIGHLIGHTS"),
                            Triple(ProIconView.Icon.MEDAL, "8. FREE JERSEY", "+ GOODIES"),
                            Triple(ProIconView.Icon.STATS, "9. MATCH PLANNING", "WITH AI COACH"),
                            Triple(ProIconView.Icon.MEDAL, "10. AI PERSONAL COACH", "FROM YOUR STATS"),
                            Triple(ProIconView.Icon.ARROW, "11. DRS/BALL/SPEED", "TRACKING")
                        )
                    ),
                    LinearLayout.LayoutParams(dp(920), LinearLayout.LayoutParams.WRAP_CONTENT)
                )
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun proPassCard(
        context: Context,
        accent: Int,
        title: String,
        passLabel: String = context.getString(R.string.str_pass),
        memberLabel: String,
        oldPrice: String,
        price: String,
        priceSuffix: String,
        baseColor: Int,
        features: List<Triple<ProIconView.Icon, String, String>>
    ): View {
        val isLegend = features.size > 5
        val isHallOfFame = accent == Color.rgb(255, 215, 0)
        val hallTextColor = Color.BLACK
        val hallDarkGold = Color.rgb(138, 106, 0)
        val cardHeight = dp(296)
        val shadowHeight = dp(286)
        return FrameLayout(context).apply {
            clipChildren = false
            clipToPadding = false

            addView(View(context).apply {
                background = GradientDrawable().apply {
                    cornerRadius = dp(21).toFloat()
                    setColor(if (isHallOfFame) Color.argb(190, 42, 32, 0) else Color.argb(165, 0, 0, 0))
                }
                translationY = dp(8).toFloat()
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, shadowHeight).apply {
                setMargins(dp(8), dp(8), dp(8), 0)
            })

            // The main card container
            val cardContainer = FrameLayout(context).apply {
                elevation = dp(if (isHallOfFame) 38 else 28).toFloat()
                translationZ = dp(if (isHallOfFame) 18 else 12).toFloat()
                outlineAmbientShadowColor = Color.BLACK
                outlineSpotShadowColor = Color.BLACK
                
                background = GradientDrawable().apply {
                    cornerRadius = dp(20).toFloat()
                    setColor(baseColor)
                    if (isHallOfFame) {
                        setStroke(dp(1), hallDarkGold)
                    }
                }
                clipToOutline = true

                addView(ProPassCardArtView(context, accent), FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                if (isHallOfFame) {
                    addView(PremiumRayView(context, 20, 0L), FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                }

                val content = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(if (isLegend) 20 else 18), dp(16), dp(if (isLegend) 20 else 18), dp(18))

                    // 1. Top Bar: Elite Badge
                    val headerRow = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        
                        val tagBox = LinearLayout(context).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(0, 0, dp(8), 0)
                            
                            addView(View(context).apply {
                                background = GradientDrawable().apply {
                                    cornerRadius = dp(4).toFloat()
                                    setColor(accent)
                                }
                            }, LinearLayout.LayoutParams(dp(5), dp(20)))
                            
                            addView(TextView(context).apply {
                                text = memberLabel
                                setTextColor(if (isHallOfFame) hallTextColor else accent)
                                textSize = 9.2f
                                letterSpacing = 0.16f
                                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                                includeFontPadding = false
                                if (isHallOfFame) {
                                    post {
                                        paint.shader = LinearGradient(
                                            0f,
                                            0f,
                                            width.toFloat().coerceAtLeast(1f),
                                            0f,
                                            intArrayOf(Color.BLACK, Color.rgb(185, 0, 20)),
                                            null,
                                            Shader.TileMode.CLAMP
                                        )
                                        invalidate()
                                    }
                                }
                            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                                leftMargin = dp(7)
                            })
                        }
                        addView(tagBox, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                        addView(FrameLayout(context).apply {
                            background = GradientDrawable().apply {
                                cornerRadius = dp(9).toFloat()
                                setColor(Color.argb(58, 255, 255, 255))
                                setStroke(1, if (isHallOfFame) Color.argb(170, 74, 58, 0) else Color.argb(150, Color.red(accent), Color.green(accent), Color.blue(accent)))
                            }
                            addView(ProIconView(context, ProIconView.Icon.MEDAL).apply {
                                setTint(if (isHallOfFame) hallTextColor else accent)
                            }, FrameLayout.LayoutParams(dp(18), dp(18), Gravity.CENTER))
                            addView(PremiumRayView(context, 9, 500L), FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                        }, LinearLayout.LayoutParams(dp(32), dp(32)))
                    }
                    addView(headerRow, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

                    val bodyRow = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL

                        addView(LinearLayout(context).apply {
                            orientation = LinearLayout.VERTICAL

                            addView(TextView(context).apply {
                                text = title
                                setTextColor(if (isHallOfFame) hallTextColor else Color.WHITE)
                                textSize = if (isHallOfFame) 30f else if (isLegend) 39f else if (title.length > 5) 33f else 39f
                                letterSpacing = -0.02f
                                typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC)
                                includeFontPadding = false
                                if (isHallOfFame) {
                                    setShadowLayer(dp(2).toFloat(), 0f, 1f, Color.argb(70, 255, 246, 190))
                                }
                            })
                            if (passLabel.isNotBlank()) {
                                addView(TextView(context).apply {
                                    text = passLabel
                                    setTextColor(accent)
                                    textSize = 39f
                                    letterSpacing = -0.02f
                                    typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC)
                                    includeFontPadding = false
                                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                                    topMargin = dp(-5)
                                })
                            }

                            addView(LinearLayout(context).apply {
                                orientation = LinearLayout.HORIZONTAL
                                gravity = Gravity.BOTTOM

                                if (oldPrice.isNotBlank()) {
                                    addView(TextView(context).apply {
                                        text = oldPrice
                                        setTextColor(Color.rgb(156, 166, 156))
                                        textSize = 18f
                                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                                        includeFontPadding = false
                                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                                        rightMargin = dp(7)
                                        bottomMargin = dp(6)
                                    })
                                }
                                addView(TextView(context).apply {
                                    text = price
                                    setTextColor(if (isHallOfFame) Color.BLACK else Color.WHITE)
                                    textSize = if (isHallOfFame) 38f else if (isLegend) 35f else if (price.length > 2) 31f else 35f
                                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                                    includeFontPadding = false
                                    if (isHallOfFame) {
                                        setShadowLayer(dp(2).toFloat(), 0f, 1f, Color.argb(95, 255, 246, 190))
                                    }
                                })
                                addView(TextView(context).apply {
                                    text = priceSuffix
                                    setTextColor(if (isHallOfFame) hallTextColor else Color.rgb(156, 166, 156))
                                    textSize = 7.2f
                                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                                    includeFontPadding = false
                                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                                    leftMargin = dp(4)
                                    bottomMargin = dp(6)
                                })
                            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                                topMargin = dp(6)
                            })
                        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, if (isLegend) 0.72f else 0.88f))

                        val featuresView = if (isLegend) {
                            LinearLayout(context).apply {
                                orientation = LinearLayout.HORIZONTAL
                                val columns = features.chunked(4)
                                columns.forEachIndexed { columnIndex, columnFeatures ->
                                    addView(LinearLayout(context).apply {
                                        orientation = LinearLayout.VERTICAL
                                        columnFeatures.forEachIndexed { index, feature ->
                                            addView(
                                                proFeatureLine(context, feature.first, feature.second, feature.third, accent),
                                                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                                                    if (index > 0) topMargin = dp(7)
                                                }
                                            )
                                        }
                                    }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                                        if (columnIndex > 0) leftMargin = dp(14)
                                    })
                                }
                            }
                        } else {
                            LinearLayout(context).apply {
                                orientation = LinearLayout.VERTICAL
                                features.forEachIndexed { index, feature ->
                                    addView(
                                        proFeatureLine(context, feature.first, feature.second, feature.third, accent),
                                        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                                            if (index > 0) topMargin = when {
                                                features.size > 3 -> dp(5)
                                                else -> dp(9)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        addView(featuresView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, if (isLegend) 1.58f else 1.12f).apply {
                            leftMargin = dp(if (isLegend) 16 else 12)
                        })
                    }
                    addView(bodyRow, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply { topMargin = dp(10) })

                    // 5. CTA Button
                    val btnContainer = FrameLayout(context).apply {
                        setPadding(0, 0, 0, dp(5))
                        addView(View(context).apply {
                            background = GradientDrawable().apply {
                                cornerRadius = dp(8).toFloat()
                                setColor(Color.argb(92, 0, 0, 0))
                            }
                        }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(34)).apply {
                            topMargin = dp(6)
                            leftMargin = dp(5)
                            rightMargin = dp(5)
                        })

                        // Actual Button
                        val btn = FrameLayout(context).apply {
                            background = GradientDrawable(
                                GradientDrawable.Orientation.TOP_BOTTOM,
                                intArrayOf(lightenColor(accent, 0.18f), accent)
                            ).apply {
                                cornerRadius = dp(8).toFloat()
                            }
                            addView(View(context).apply {
                                background = GradientDrawable(
                                    GradientDrawable.Orientation.TOP_BOTTOM,
                                    intArrayOf(Color.argb(96, 255, 255, 255), Color.TRANSPARENT)
                                ).apply {
                                    cornerRadius = dp(8).toFloat()
                                }
                            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(17)))
                            addView(LinearLayout(context).apply {
                                orientation = LinearLayout.HORIZONTAL
                                gravity = Gravity.CENTER
                                addView(TextView(context).apply {
                                    text = context.getString(R.string.str_activate_pro)
                                    setTextColor(if (accent == primary || isHallOfFame) Color.rgb(4, 9, 4) else Color.WHITE)
                                    textSize = 9.3f
                                    letterSpacing = 0.08f
                                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                                    includeFontPadding = false
                                })
                                addView(ProIconView(context, ProIconView.Icon.ARROW).apply {
                                    setTint(if (accent == primary || isHallOfFame) Color.rgb(4, 9, 4) else Color.WHITE)
                                }, LinearLayout.LayoutParams(dp(15), dp(15)).apply { leftMargin = dp(9) })
                            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                        }
                        addView(btn, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(38)))
                    }
                    addView(TextView(context).apply {
                        text = context.getString(R.string.str_cancel_anytime__no_h)
                        gravity = Gravity.CENTER
                        setTextColor(if (isHallOfFame) hallTextColor else accent)
                        textSize = 6.3f
                        letterSpacing = 0.06f
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        includeFontPadding = false
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
                    addView(btnContainer, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(7) })
                }
                addView(content, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            }

            addView(cardContainer, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, cardHeight))
        }
    }

    private fun proFeatureLine(context: Context, icon: ProIconView.Icon, title: String, subtitle: String, accent: Int): View {
        val compact = accent != primary
        val dense = accent == Color.rgb(156, 82, 255) || accent == Color.rgb(255, 215, 0)
        val isHallOfFame = accent == Color.rgb(255, 215, 0)
        val featureTextColor = if (isHallOfFame) Color.rgb(26, 20, 0) else Color.WHITE
        val featureTint = if (isHallOfFame) Color.rgb(74, 58, 0) else accent
        val iconSize = when {
            dense -> 26
            compact -> 24
            else -> 32
        }
        val glyphSize = when {
            dense -> 15
            compact -> 14
            else -> 18
        }
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            
            val iconBox = FrameLayout(context).apply {
                elevation = dp(4).toFloat()
                background = GradientDrawable().apply {
                    cornerRadius = dp(6).toFloat()
                    setColor(if (isHallOfFame) Color.argb(34, 0, 0, 0) else Color.argb(42, 255, 255, 255))
                    setStroke(1, if (isHallOfFame) Color.argb(70, 74, 58, 0) else Color.argb(58, Color.red(accent), Color.green(accent), Color.blue(accent)))
                }
                addView(View(context).apply {
                    background = GradientDrawable().apply {
                        gradientType = GradientDrawable.RADIAL_GRADIENT
                        gradientRadius = dp(26).toFloat()
                        colors = intArrayOf(
                            if (isHallOfFame) Color.argb(28, 255, 255, 255) else Color.argb(34, Color.red(accent), Color.green(accent), Color.blue(accent)),
                            Color.TRANSPARENT
                        )
                    }
                }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                addView(ProIconView(context, icon).apply {
                    setTint(featureTint)
                }, FrameLayout.LayoutParams(dp(glyphSize), dp(glyphSize), Gravity.CENTER))
            }
            addView(iconBox, LinearLayout.LayoutParams(dp(iconSize), dp(iconSize)))
            
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                if (compact) {
                    addView(TextView(context).apply {
                        text = "$title $subtitle"
                        setTextColor(featureTextColor)
                        textSize = if (accent == Color.rgb(255, 215, 0)) 8.7f else if (dense) 9.2f else 7.8f
                        letterSpacing = 0.04f
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        includeFontPadding = false
                        maxLines = 1
                    })
                } else {
                    addView(TextView(context).apply {
                        text = title
                        setTextColor(featureTextColor)
                        textSize = 10.4f
                        letterSpacing = 0.01f
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        includeFontPadding = false
                    })
                    addView(TextView(context).apply {
                        text = subtitle
                        setTextColor(featureTextColor)
                        textSize = 10.4f
                        letterSpacing = 0.01f
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        includeFontPadding = false
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        topMargin = dp(1)
                    })
                    addView(TextView(context).apply {
                        text = context.getString(R.string.str_realtime_player_anal)
                        setTextColor(Color.rgb(126, 136, 128))
                        textSize = 6.2f
                        letterSpacing = 0.04f
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        includeFontPadding = false
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        topMargin = dp(2)
                    })
                }
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = dp(if (dense) 10 else 12) })
        }
    }

    private fun sectionHeader(context: Context, title: String, end: String?): View {
        return LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            addView(TextView(context).apply {
                text = title
                setTextColor(Color.WHITE)
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            end?.let {
                addView(TextView(context).apply {
                    text = it
                    setTextColor(primary)
                    textSize = 10f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                })
            }
        }
    }

    private fun personalizeGearSection(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL

            addView(LinearLayout(context).apply {
                gravity = Gravity.BOTTOM
                addView(TextView(context).apply {
                    text = context.getString(R.string.str_personalize_yourngea)
                    setTextColor(Color.WHITE)
                    textSize = 24f
                    setLineSpacing(0f, 0.9f)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                addView(TextView(context).apply {
                    text = context.getString(R.string.str_viewnall)
                    gravity = Gravity.CENTER
                    setTextColor(primary)
                    textSize = 8.5f
                    letterSpacing = 0.12f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(dp(44), LinearLayout.LayoutParams.WRAP_CONTENT))
            })

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), dp(13), dp(12), dp(12))
                background = GradientDrawable().apply {
                    cornerRadius = dp(8).toFloat()
                    setColor(Color.rgb(17, 24, 35))
                    setStroke(1, Color.argb(44, 255, 255, 255))
                }

                addView(TextView(context).apply {
                    text = context.getString(R.string.str_siddheshwar_sahoo_ge)
                    setTextColor(Color.WHITE)
                    textSize = 10.2f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                    setLineSpacing(0f, 0.95f)
                    val limeStart = text.indexOf(context.getString(R.string.str_20))
                    if (limeStart >= 0) {
                        val spannable = android.text.SpannableString(text)
                        spannable.setSpan(android.text.style.ForegroundColorSpan(primary), limeStart, text.length, 0)
                        setText(spannable)
                    }
                })

                addView(HorizontalScrollView(context).apply {
                    isHorizontalScrollBarEnabled = false
                    clipToPadding = false
                    val row = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        addView(gearProductCard(context, R.drawable.jersey1, "Performance SleeveLess", null), LinearLayout.LayoutParams(dp(152), dp(252)).apply { rightMargin = dp(12) })
                        addView(gearProductCard(context, R.drawable.jersey2, "Pro Match Jersey", null), LinearLayout.LayoutParams(dp(152), dp(252)).apply { rightMargin = dp(2) })
                    }
                    addView(row)
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) })

                addView(TextView(context).apply {
                    text = context.getString(R.string.str_custom_name)
                    setTextColor(Color.WHITE)
                    textSize = 8.4f
                    letterSpacing = 0.12f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(18) })

                addView(FrameLayout(context).apply {
                    background = GradientDrawable().apply {
                        cornerRadius = dp(2).toFloat()
                        setColor(Color.rgb(1, 4, 8))
                    }
                    addView(EditText(context).apply {
                        setText("SIDDHESHWAR")
                        setTextColor(Color.WHITE)
                        setHintTextColor(Color.rgb(100, 110, 118))
                        hint = "ENTER NAME"
                        textSize = 13f
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                        gravity = Gravity.CENTER_VERTICAL
                        includeFontPadding = false
                        setPadding(dp(9), 0, dp(34), 0)
                        background = null
                        isSingleLine = true
                        setSelectAllOnFocus(false)
                    }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                    addView(GearActionIconView(context, GearActionIconView.Icon.EDIT).apply {
                        setTint(primary)
                    }, FrameLayout.LayoutParams(dp(18), dp(18), Gravity.END or Gravity.CENTER_VERTICAL).apply {
                        rightMargin = dp(9)
                    })
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(42)).apply { topMargin = dp(8) })

                addView(LinearLayout(context).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    addView(FrameLayout(context).apply {
                        background = GradientDrawable(
                            GradientDrawable.Orientation.LEFT_RIGHT,
                            intArrayOf(primary, Color.rgb(0, 108, 255))
                        ).apply {
                            cornerRadius = dp(6).toFloat()
                        }
                        addView(TextView(context).apply {
                            text = context.getString(R.string.str_apply_to_jersey)
                            gravity = Gravity.CENTER
                            setTextColor(Color.rgb(2, 8, 5))
                            textSize = 10.5f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                            includeFontPadding = false
                        }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                    }, LinearLayout.LayoutParams(0, dp(46), 1f))
                    addView(FrameLayout(context).apply {
                        background = GradientDrawable().apply {
                            cornerRadius = dp(6).toFloat()
                            setColor(Color.rgb(18, 25, 36))
                            setStroke(1, Color.argb(58, 255, 255, 255))
                        }
                        addView(GearActionIconView(context, GearActionIconView.Icon.SHARE).apply {
                            setTint(Color.WHITE)
                        }, FrameLayout.LayoutParams(dp(20), dp(20), Gravity.CENTER))
                    }, LinearLayout.LayoutParams(dp(46), dp(46)).apply { leftMargin = dp(8) })
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) })
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10) })
        }
    }

    private fun gearProductCard(context: Context, image: Int, title: String, stat: String?): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(9), dp(9), dp(9), dp(9))
            background = GradientDrawable().apply {
                cornerRadius = dp(5).toFloat()
                setColor(Color.rgb(10, 17, 28))
                setStroke(1, Color.argb(42, 255, 255, 255))
            }

            addView(FrameLayout(context).apply {
                background = GradientDrawable().apply {
                    cornerRadius = dp(3).toFloat()
                    setColor(Color.rgb(2, 7, 11))
                }
                addView(ImageView(context).apply {
                    setImageResource(image)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                stat?.let {
                    addView(TextView(context).apply {
                        text = it
                        gravity = Gravity.CENTER
                        setTextColor(primary)
                        textSize = 25f
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                        includeFontPadding = false
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setColor(Color.argb(132, 2, 9, 10))
                            setStroke(dp(1), primary)
                        }
                    }, FrameLayout.LayoutParams(dp(64), dp(64), Gravity.CENTER))
                }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(GearActionIconView(context, GearActionIconView.Icon.TAG).apply {
                    setTint(primary)
                }, LinearLayout.LayoutParams(dp(12), dp(12)))
                addView(TextView(context).apply {
                    text = title
                    setTextColor(Color.WHITE)
                    textSize = 9.2f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                    setSingleLine(true)
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = dp(6) })
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(9) })

            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    cornerRadius = dp(2).toFloat()
                    setColor(Color.rgb(4, 10, 16))
                    setStroke(1, Color.argb(74, 193, 255, 0))
                }
                addView(TextView(context).apply {
                    text = context.getString(R.string.str_get_it_now)
                    setTextColor(primary)
                    textSize = 8.4f
                    letterSpacing = 0.08f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                })
                addView(ProIconView(context, ProIconView.Icon.ARROW).apply {
                    setTint(primary)
                }, LinearLayout.LayoutParams(dp(12), dp(12)).apply { leftMargin = dp(9) })
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(30)).apply { topMargin = dp(8) })
        }
    }

    private fun feedCard(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(10))
            background = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setColor(Color.rgb(15, 22, 33))
                setStroke(1, Color.argb(48, 255, 255, 255))
            }

            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(TextView(context).apply {
                    text = context.getString(R.string.str_sx)
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                    background = GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        intArrayOf(primary, Color.rgb(0, 126, 255))
                    ).apply {
                        cornerRadius = dp(5).toFloat()
                    }
                }, LinearLayout.LayoutParams(dp(36), dp(36)))

                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(LinearLayout(context).apply {
                        gravity = Gravity.CENTER_VERTICAL
                        addView(TextView(context).apply {
                            text = context.getString(R.string.str_sportsxtreme)
                            setTextColor(Color.WHITE)
                            textSize = 10f
                            typeface = Typeface.DEFAULT_BOLD
                            includeFontPadding = false
                        })
                        addView(View(context).apply {
                            background = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setColor(primary)
                            }
                        }, LinearLayout.LayoutParams(dp(8), dp(8)).apply { leftMargin = dp(4) })
                    })
                    addView(TextView(context).apply {
                        text = context.getString(R.string.str_23_may_2024__1208_pm)
                        setTextColor(Color.rgb(135, 150, 158))
                        textSize = 7.2f
                        typeface = Typeface.DEFAULT_BOLD
                        includeFontPadding = false
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        topMargin = dp(3)
                    })
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = dp(9) })

                addView(FeedIconView(context, FeedIconView.Icon.MORE).apply {
                    setTint(Color.WHITE)
                }, LinearLayout.LayoutParams(dp(22), dp(22)))
            })

            addView(TextView(context).apply {
                text = context.getString(R.string.str_sailing_through_life)
                setTextColor(Color.WHITE)
                textSize = 22f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
                setLineSpacing(0f, 0.92f)
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(18) })

            addView(TextView(context).apply {
                text = context.getString(R.string.str_on_and_off_the_field)
                setTextColor(Color.WHITE)
                textSize = 10.3f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
                setLineSpacing(0f, 1.08f)
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(14) })

            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    cornerRadius = dp(15).toFloat()
                    setColor(Color.rgb(7, 15, 22))
                    setStroke(1, Color.argb(80, 193, 255, 0))
                }
                addView(TextView(context).apply {
                    text = context.getString(R.string.str_check_his_insights)
                    setTextColor(primary)
                    textSize = 8.5f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                })
                addView(ProIconView(context, ProIconView.Icon.ARROW).apply {
                    setTint(primary)
                }, LinearLayout.LayoutParams(dp(12), dp(12)).apply { leftMargin = dp(8) })
            }, LinearLayout.LayoutParams(dp(142), dp(32)).apply { topMargin = dp(16) })

            addView(ImageView(context).apply {
                setImageResource(R.drawable.feeduploadsample)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(260)).apply { topMargin = dp(18) })

            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(reactionBubble(context, Color.rgb(20, 100, 255), "C"))
                addView(reactionBubble(context, Color.rgb(255, 92, 35), "❤"), LinearLayout.LayoutParams(dp(20), dp(20)).apply { leftMargin = dp(-5) })
                addView(reactionBubble(context, Color.rgb(255, 132, 25), "⚡"), LinearLayout.LayoutParams(dp(20), dp(20)).apply { leftMargin = dp(-5) })
                addView(TextView(context).apply {
                    text = context.getString(R.string.str_150_reactions)
                    setTextColor(Color.WHITE)
                    textSize = 8.2f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = dp(8) })
                addView(TextView(context).apply {
                    text = context.getString(R.string.str_34_comments)
                    setTextColor(Color.WHITE)
                    textSize = 8.2f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                })
                addView(FeedIconView(context, FeedIconView.Icon.COMMENT).apply {
                    setTint(Color.WHITE)
                }, LinearLayout.LayoutParams(dp(13), dp(13)).apply { leftMargin = dp(5) })
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10) })

            addView(View(context).apply {
                setBackgroundColor(Color.argb(38, 255, 255, 255))
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply { topMargin = dp(12) })

            addView(LinearLayout(context).apply {
                gravity = Gravity.CENTER_VERTICAL
                addView(feedAction(context, FeedIconView.Icon.LIKE, "Like"), LinearLayout.LayoutParams(0, dp(42), 1f))
                addView(feedAction(context, FeedIconView.Icon.COMMENT, "Comment"), LinearLayout.LayoutParams(0, dp(42), 1f))
                addView(feedAction(context, FeedIconView.Icon.SHARE, "Share"), LinearLayout.LayoutParams(0, dp(42), 1f))
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun reactionBubble(context: Context, color: Int, label: String): TextView {
        return TextView(context).apply {
            text = label
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 8f
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
                setStroke(1, Color.WHITE)
            }
            layoutParams = LinearLayout.LayoutParams(dp(20), dp(20))
        }
    }

    private fun feedAction(context: Context, icon: FeedIconView.Icon, label: String): View {
        return LinearLayout(context).apply {
            gravity = Gravity.CENTER
            addView(FeedIconView(context, icon).apply {
                setTint(Color.WHITE)
            }, LinearLayout.LayoutParams(dp(15), dp(15)))
            addView(TextView(context).apply {
                text = label
                setTextColor(Color.WHITE)
                textSize = 8.2f
                typeface = Typeface.DEFAULT_BOLD
                includeFontPadding = false
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { leftMargin = dp(7) })
        }
    }

    private fun matchCard(context: Context): View {
        return FrameLayout(context).apply {
            background = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setColor(panel)
                setStroke(dp(1), Color.argb(55, 255, 255, 255))
            }
            addView(ImageView(context).apply {
                setImageResource(R.drawable.proimage_onboarding4)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }, LayoutParams(LayoutParams.MATCH_PARENT, dp(210)))
            addView(TextView(context).apply {
                text = context.getString(R.string.str_next_big_match)
                setTextColor(primary)
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(dp(12), 0, 0, dp(12))
                gravity = Gravity.BOTTOM
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(Color.TRANSPARENT, Color.argb(225, 0, 0, 0))
                )
            }, LayoutParams(LayoutParams.MATCH_PARENT, dp(210)))
        }
    }

    private fun createComingSoon(context: Context, item: NavItem): View {
        return FrameLayout(context).apply {
            setPadding(dp(22), dp(22), dp(22), dp(22))
            addView(glassPanel(context).apply {
                gravity = Gravity.CENTER
                setPadding(dp(22), dp(22), dp(22), dp(22))
                addView(NavIconView(context, item.icon).apply { setTint(primary) }, LinearLayout.LayoutParams(dp(40), dp(40)).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                })
                addView(TextView(context).apply {
                    text = item.label.uppercase()
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    textSize = 22f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(18) })
                addView(TextView(context).apply {
                    text = context.getString(R.string.str_this_bottom_nav_is_w)
                    gravity = Gravity.CENTER
                    setTextColor(muted)
                    textSize = 12f
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) })
            }, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        }
    }

    private fun glassPanel(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setColor(Color.argb(220, 4, 11, 15))
                setStroke(dp(1), Color.argb(56, 255, 255, 255))
            }
        }
    }

    private fun blockParams(top: Int = 0): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(top)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

    private fun lightenColor(color: Int, amount: Float): Int {
        val safeAmount = amount.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(color) + (255 - Color.red(color)) * safeAmount).toInt().coerceIn(0, 255),
            (Color.green(color) + (255 - Color.green(color)) * safeAmount).toInt().coerceIn(0, 255),
            (Color.blue(color) + (255 - Color.blue(color)) * safeAmount).toInt().coerceIn(0, 255)
        )
    }

    private class ScoreCardBackgroundView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val path = Path()
        private val rect = RectF()
        private var bgShader: Shader? = null
        private var glowShader1: Shader? = null
        private var glowShader2: Shader? = null

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            val wf = w.toFloat()
            val hf = h.toFloat()
            if (wf > 0 && hf > 0) {
                bgShader = LinearGradient(
                    0f, 0f, wf, hf,
                    intArrayOf(Color.rgb(14, 25, 35), Color.rgb(5, 10, 17), Color.rgb(11, 20, 24)),
                    floatArrayOf(0f, 0.48f, 1f), Shader.TileMode.CLAMP
                )
                glowShader1 = android.graphics.RadialGradient(
                    wf * 0.14f, hf * 0.15f, wf * 0.7f,
                    intArrayOf(Color.argb(92, 193, 255, 0), Color.argb(18, 193, 255, 0), Color.TRANSPARENT),
                    floatArrayOf(0f, 0.42f, 1f), Shader.TileMode.CLAMP
                )
                glowShader2 = android.graphics.RadialGradient(
                    wf * 0.9f, hf * 0.05f, wf * 0.72f,
                    intArrayOf(Color.argb(78, 0, 210, 255), Color.argb(16, 0, 210, 255), Color.TRANSPARENT),
                    floatArrayOf(0f, 0.44f, 1f), Shader.TileMode.CLAMP
                )
            }
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            if (w <= 0f || h <= 0f) return

            val d = resources.displayMetrics.density
            val radius = 14f * d
            rect.set(0f, 0f, w, h)

            paint.shader = bgShader
            canvas.drawRoundRect(rect, radius, radius, paint)

            paint.shader = glowShader1
            canvas.drawRoundRect(rect, radius, radius, paint)

            paint.shader = glowShader2
            canvas.drawRoundRect(rect, radius, radius, paint)
            paint.shader = null

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f * d
            paint.color = Color.argb(24, 255, 255, 255)
            val step = 28f * d
            var x = -w * 0.2f
            while (x < w * 1.2f) {
                canvas.drawLine(x, h * 0.18f, x + w * 0.24f, h * 0.88f, paint)
                x += step
            }

            paint.style = Paint.Style.FILL
            paint.color = Color.argb(42, 193, 255, 0)
            path.reset()
            path.moveTo(w * 0.04f, h)
            path.lineTo(w * 0.18f, h)
            path.lineTo(w * 0.27f, h * 0.72f)
            path.lineTo(w * 0.13f, h * 0.72f)
            path.close()
            canvas.drawPath(path, paint)

            paint.color = Color.argb(34, 0, 210, 255)
            path.reset()
            path.moveTo(w * 0.86f, 0f)
            path.lineTo(w, 0f)
            path.lineTo(w, h * 0.28f)
            path.lineTo(w * 0.76f, h * 0.14f)
            path.close()
            canvas.drawPath(path, paint)
            paint.style = Paint.Style.FILL
        }
    }

    private class LiveBadgeView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var visible = true
        private var animator: android.animation.ValueAnimator? = null

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            startBlink()
        }

        override fun onWindowVisibilityChanged(visibility: Int) {
            super.onWindowVisibilityChanged(visibility)
            if (visibility == VISIBLE) {
                startBlink()
            } else {
                stopBlink()
            }
        }

        private fun startBlink() {
            if (animator?.isStarted == true) return
            animator = android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 760L
                repeatCount = android.animation.ValueAnimator.INFINITE
                repeatMode = android.animation.ValueAnimator.REVERSE
                interpolator = android.view.animation.LinearInterpolator()
                addUpdateListener {
                    visible = (it.animatedValue as Float) < 0.52f
                    invalidate()
                }
                start()
            }
        }

        override fun onDetachedFromWindow() {
            stopBlink()
            super.onDetachedFromWindow()
        }

        private fun stopBlink() {
            animator?.cancel()
            animator = null
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            if (w <= 0f || h <= 0f) return

            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textSize = 10f * resources.displayMetrics.scaledDensity
            paint.textAlign = Paint.Align.RIGHT
            paint.color = if (visible) Color.rgb(255, 38, 54) else Color.TRANSPARENT
            paint.style = Paint.Style.FILL
            canvas.drawCircle(h * 0.24f, h * 0.5f, 3.4f * resources.displayMetrics.density, paint)
            val baseline = h * 0.5f - (paint.descent() + paint.ascent()) * 0.5f
            canvas.drawText("LIVE", w, baseline, paint)
        }
    }

    private class LocationPinView(context: Context) : View(context) {
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
            val w = width.toFloat()
            val h = height.toFloat()
            val d = resources.displayMetrics.density
            paint.color = tint
            paint.strokeWidth = 1.7f * d
            paint.style = Paint.Style.STROKE

            path.reset()
            path.moveTo(w * 0.5f, h * 0.9f)
            path.cubicTo(w * 0.22f, h * 0.58f, w * 0.2f, h * 0.38f, w * 0.2f, h * 0.32f)
            path.cubicTo(w * 0.2f, h * 0.14f, w * 0.34f, h * 0.08f, w * 0.5f, h * 0.08f)
            path.cubicTo(w * 0.66f, h * 0.08f, w * 0.8f, h * 0.14f, w * 0.8f, h * 0.32f)
            path.cubicTo(w * 0.8f, h * 0.38f, w * 0.78f, h * 0.58f, w * 0.5f, h * 0.9f)
            canvas.drawPath(path, paint)
            canvas.drawCircle(w * 0.5f, h * 0.32f, w * 0.11f, paint)
        }
    }

    private class HomeBackgroundView(context: Context) : View(context) {
        private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.argb(24, 193, 255, 0)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            if (w > 0 && h > 0) {
                bgPaint.shader = LinearGradient(
                    0f, 0f, w.toFloat(), h.toFloat(),
                    // Dialed down the green glow intensity from 180 to 120, but kept it very visible
                    intArrayOf(Color.argb(120, 0, 64, 92), Color.TRANSPARENT, Color.argb(120, 193, 255, 0)),
                    floatArrayOf(0f, 0.45f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            canvas.drawColor(Color.rgb(0, 3, 7))
            
            // Draw background glow safely without mutating paint shader during onDraw
            canvas.drawRect(0f, 0f, w, h, bgPaint)

            // Draw grid pattern safely with isolated paint object
            gridPaint.strokeWidth = max(1f, resources.displayMetrics.density)
            val grid = 38f * resources.displayMetrics.density
            var x = -grid
            while (x < w + grid) {
                canvas.drawLine(x, 0f, x - w * 0.08f, h, gridPaint)
                x += grid
            }
        }
    }

    private class ScoreCardGlowView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rect = RectF()
        private var radialShader: Shader? = null
        private var linearShader: Shader? = null

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            val wf = w.toFloat()
            val hf = h.toFloat()
            if (wf > 0 && hf > 0) {
                radialShader = android.graphics.RadialGradient(
                    wf * 0.16f, hf * 0.16f, wf * 0.72f,
                    intArrayOf(Color.argb(96, 22, 96, 172), Color.argb(36, 11, 58, 118), Color.TRANSPARENT),
                    floatArrayOf(0f, 0.48f, 1f), Shader.TileMode.CLAMP
                )
                linearShader = LinearGradient(
                    0f, 0f, wf * 0.52f, hf * 0.34f,
                    intArrayOf(Color.argb(58, 42, 116, 196), Color.TRANSPARENT),
                    null, Shader.TileMode.CLAMP
                )
            }
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            if (w <= 0f || h <= 0f) return

            val d = resources.displayMetrics.density
            rect.set(0f, 0f, w, h)

            paint.shader = radialShader
            canvas.drawRoundRect(rect, 14f * d, 14f * d, paint)

            paint.shader = linearShader
            canvas.drawRoundRect(rect, 14f * d, 14f * d, paint)
            paint.shader = null
        }
    }

    private class ProPassCardArtView(context: Context, private val accent: Int) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val path = Path()
        private val rect = RectF()
        private var glowBitmap: android.graphics.Bitmap? = null
        private var bgShader: Shader? = null
        private val isGold = accent == Color.rgb(255, 215, 0)

        override fun drawableStateChanged() {
            super.drawableStateChanged()
            if (isGold) {
                refreshCardArt(width, height)
            }
        }

        init {
            // LAYER_TYPE_SOFTWARE removed to prevent vanishing on resume
        }

        override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
            super.onWindowFocusChanged(hasWindowFocus)
            if (hasWindowFocus) {
                refreshCardArt(width, height)
                invalidate()
            }
        }

        override fun onVisibilityAggregated(isVisible: Boolean) {
            super.onVisibilityAggregated(isVisible)
            if (isVisible) {
                refreshCardArt(width, height)
                invalidate()
            }
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            refreshCardArt(w, h)
        }

        private fun refreshCardArt(w: Int, h: Int) {
            val wf = w.toFloat()
            val hf = h.toFloat()
            if (wf > 0 && hf > 0) {
                bgShader = LinearGradient(
                    0f, 0f, wf, hf,
                    if (isGold) {
                        intArrayOf(
                            Color.rgb(138, 106, 0),
                            Color.rgb(212, 175, 55),
                            Color.rgb(255, 215, 0),
                            Color.rgb(138, 106, 0),
                            Color.rgb(201, 162, 39)
                        )
                    } else {
                        intArrayOf(
                            Color.rgb(
                                (Color.red(accent) * 0.22f).toInt().coerceAtLeast(7),
                                (Color.green(accent) * 0.14f).toInt().coerceAtLeast(5),
                                (Color.blue(accent) * 0.14f).toInt().coerceAtLeast(7)
                            ),
                            Color.rgb(7, 10, 12),
                            Color.rgb(4, 6, 8)
                        )
                    },
                    if (isGold) floatArrayOf(0f, 0.32f, 0.55f, 0.78f, 1f) else floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            rebuildGlowBitmap(w, h)
        }

        override fun onDetachedFromWindow() {
            glowBitmap?.recycle()
            glowBitmap = null
            super.onDetachedFromWindow()
        }

        private fun rebuildGlowBitmap(w: Int, h: Int) {
            glowBitmap?.recycle()
            glowBitmap = null
            if (w <= 0 || h <= 0) return

            val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
            val bitmapCanvas = Canvas(bitmap)
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val radius = 20f * resources.displayMetrics.density
            val bounds = RectF(0f, 0f, w.toFloat(), h.toFloat())

            glowPaint.shader = android.graphics.RadialGradient(
                w * 0.2f,
                h * 0.1f,
                w * 0.7f,
                intArrayOf(
                    Color.argb(if (isGold) 88 else 42, Color.red(accent), Color.green(accent), Color.blue(accent)),
                    Color.argb(if (isGold) 26 else 12, Color.red(accent), Color.green(accent), Color.blue(accent)),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.44f, 1f),
                Shader.TileMode.CLAMP
            )
            bitmapCanvas.drawRoundRect(bounds, radius, radius, glowPaint)

            if (isGold) {
                glowPaint.shader = android.graphics.RadialGradient(
                    w * 0.72f,
                    h * 0.08f,
                    w * 0.48f,
                    intArrayOf(
                        Color.argb(64, 255, 246, 196),
                        Color.argb(28, 255, 215, 0),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.44f, 1f),
                    Shader.TileMode.CLAMP
                )
                bitmapCanvas.drawRoundRect(bounds, radius, radius, glowPaint)
            }

            glowPaint.shader = LinearGradient(
                0f,
                0f,
                w * 0.62f,
                h * 0.28f,
                intArrayOf(
                    Color.argb(if (isGold) 58 else 38, 255, 246, 190),
                    Color.argb(if (isGold) 18 else 8, Color.red(accent), Color.green(accent), Color.blue(accent)),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.52f, 1f),
                Shader.TileMode.CLAMP
            )
            bitmapCanvas.drawRoundRect(bounds, radius, radius, glowPaint)
            glowPaint.shader = null

            glowBitmap = bitmap
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            val d = resources.displayMetrics.density
            val radius = 20f * d
            if (w <= 0f || h <= 0f) return

            paint.reset()
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL

            rect.set(0f, 0f, w, h)
            paint.color = if (isGold) Color.rgb(138, 106, 0) else Color.rgb(4, 6, 8)
            canvas.drawRoundRect(rect, radius, radius, paint)

            if (bgShader == null) {
                refreshCardArt(width, height)
            }
            paint.shader = bgShader
            canvas.drawRoundRect(rect, radius, radius, paint)
            paint.shader = null

            if (glowBitmap == null) {
                rebuildGlowBitmap(width, height)
            }
            glowBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }

            if (isGold) {
                drawHallOfFameWatermark(canvas, w, h, d)

                paint.shader = LinearGradient(
                    0f,
                    0f,
                    0f,
                    h * 0.5f,
                    intArrayOf(
                        Color.argb(72, 255, 250, 220),
                        Color.argb(24, 255, 215, 0),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.38f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(RectF(1.5f * d, 1.5f * d, w - 1.5f * d, h * 0.48f), radius, radius, paint)
                paint.shader = null

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.2f * d
                paint.shader = LinearGradient(
                    0f,
                    0f,
                    w,
                    0f,
                    intArrayOf(
                        Color.argb(155, 74, 58, 0),
                        Color.argb(210, 138, 106, 0),
                        Color.argb(145, 255, 215, 0),
                        Color.argb(190, 74, 58, 0)
                    ),
                    floatArrayOf(0f, 0.34f, 0.68f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(RectF(1f * d, 1f * d, w - 1f * d, h - 1f * d), radius, radius, paint)
                paint.shader = null
                paint.style = Paint.Style.FILL
            }

            paint.style = Paint.Style.FILL
            paint.color = if (isGold) Color.argb(50, 0, 0, 0) else Color.argb(42, Color.red(accent), Color.green(accent), Color.blue(accent))
            path.reset()
            path.moveTo(w * 0.76f, h * 0.78f)
            path.lineTo(w * 0.86f, h * 0.78f)
            path.lineTo(w * 0.76f, h)
            path.lineTo(w * 0.66f, h)
            path.close()
            canvas.drawPath(path, paint)

            paint.color = if (isGold) Color.argb(34, 0, 0, 0) else Color.argb(28, 255, 255, 255)
            path.reset()
            path.moveTo(w * 0.9f, h * 0.74f)
            path.lineTo(w, h * 0.72f)
            path.lineTo(w * 0.91f, h)
            path.lineTo(w * 0.81f, h)
            path.close()
            canvas.drawPath(path, paint)

            paint.color = if (isGold) Color.argb(20, 0, 0, 0) else Color.argb(18, Color.red(accent), Color.green(accent), Color.blue(accent))
            repeat(4) { index ->
                val left = w * (0.57f + index * 0.08f)
                path.reset()
                path.moveTo(left, h * 0.82f)
                path.lineTo(left + w * 0.045f, h * 0.82f)
                path.lineTo(left - w * 0.025f, h)
                path.lineTo(left - w * 0.07f, h)
                path.close()
                canvas.drawPath(path, paint)
            }

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f * d
            paint.color = if (isGold) Color.argb(22, 0, 0, 0) else Color.argb(16, 255, 255, 255)
            repeat(3) { index ->
                val y = h * (0.82f + index * 0.045f)
                canvas.drawLine(w * 0.58f, y, w * 0.96f, y - h * 0.13f, paint)
            }
            paint.reset()
            paint.isAntiAlias = true
        }

        private fun drawHallOfFameWatermark(canvas: Canvas, w: Float, h: Float, d: Float) {
            val cx = w * 0.79f
            val cy = h * 0.48f
            val unit = minOf(w, h) * 0.16f

            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = 2.2f * d
            paint.color = Color.argb(22, 74, 58, 0)

            repeat(7) { index ->
                val offset = index * 0.065f
                canvas.drawArc(
                    RectF(cx - unit * (1.55f + offset), cy - unit * 1.45f, cx - unit * 0.18f, cy + unit * 1.6f),
                    210f + index * 2f,
                    16f,
                    false,
                    paint
                )
                canvas.drawArc(
                    RectF(cx + unit * 0.18f, cy - unit * 1.45f, cx + unit * (1.55f + offset), cy + unit * 1.6f),
                    -46f - index * 2f,
                    16f,
                    false,
                    paint
                )
            }

            paint.style = Paint.Style.FILL
            paint.color = Color.argb(18, 74, 58, 0)

            rect.set(cx - unit * 0.42f, cy - unit * 0.72f, cx + unit * 0.42f, cy + unit * 0.18f)
            canvas.drawRoundRect(rect, unit * 0.12f, unit * 0.12f, paint)

            path.reset()
            path.moveTo(cx - unit * 0.58f, cy - unit * 0.48f)
            path.cubicTo(cx - unit * 1.05f, cy - unit * 0.58f, cx - unit * 1.02f, cy + unit * 0.05f, cx - unit * 0.38f, cy + unit * 0.02f)
            path.lineTo(cx - unit * 0.38f, cy - unit * 0.18f)
            path.cubicTo(cx - unit * 0.72f, cy - unit * 0.14f, cx - unit * 0.72f, cy - unit * 0.42f, cx - unit * 0.48f, cy - unit * 0.36f)
            path.close()
            canvas.drawPath(path, paint)

            path.reset()
            path.moveTo(cx + unit * 0.58f, cy - unit * 0.48f)
            path.cubicTo(cx + unit * 1.05f, cy - unit * 0.58f, cx + unit * 1.02f, cy + unit * 0.05f, cx + unit * 0.38f, cy + unit * 0.02f)
            path.lineTo(cx + unit * 0.38f, cy - unit * 0.18f)
            path.cubicTo(cx + unit * 0.72f, cy - unit * 0.14f, cx + unit * 0.72f, cy - unit * 0.42f, cx + unit * 0.48f, cy - unit * 0.36f)
            path.close()
            canvas.drawPath(path, paint)

            rect.set(cx - unit * 0.12f, cy + unit * 0.14f, cx + unit * 0.12f, cy + unit * 0.6f)
            canvas.drawRoundRect(rect, unit * 0.04f, unit * 0.04f, paint)
            rect.set(cx - unit * 0.42f, cy + unit * 0.56f, cx + unit * 0.42f, cy + unit * 0.76f)
            canvas.drawRoundRect(rect, unit * 0.06f, unit * 0.06f, paint)
            rect.set(cx - unit * 0.62f, cy + unit * 0.76f, cx + unit * 0.62f, cy + unit * 0.94f)
            canvas.drawRoundRect(rect, unit * 0.05f, unit * 0.05f, paint)
        }
    }

    private class PremiumRayView(context: Context, private val cornerRadiusDp: Int = 0, private val startDelayMs: Long = 0) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val clipPath = Path()
        private val rayPath = Path()
        private val rect = RectF()
        private val shaderMatrix = android.graphics.Matrix()
        private var rayShader: Shader? = null
        private var progress = 0f
        private var animator: android.animation.ValueAnimator? = null

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            val wf = w.toFloat()
            val hf = h.toFloat()
            if (wf > 0 && hf > 0) {
                val d = resources.displayMetrics.density
                val diagonal = Math.sqrt((wf * wf + hf * hf).toDouble()).toFloat()
                val band = max(10f * d, minOf(wf, hf) * 0.42f)
                val ux = wf / diagonal
                val uy = hf / diagonal
                val halfBand = band * 0.5f

                rayShader = LinearGradient(
                    -ux * halfBand,
                    -uy * halfBand,
                    ux * halfBand,
                    uy * halfBand,
                    intArrayOf(
                        Color.TRANSPARENT,
                        Color.argb(76, 255, 198, 42),
                        Color.argb(172, 255, 248, 196),
                        Color.argb(76, 255, 198, 42),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.28f, 0.5f, 0.72f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            startRay()
        }

        override fun onWindowVisibilityChanged(visibility: Int) {
            super.onWindowVisibilityChanged(visibility)
            if (visibility == VISIBLE) {
                startRay()
            } else {
                stopRay()
            }
        }

        override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
            super.onWindowFocusChanged(hasWindowFocus)
            if (hasWindowFocus) {
                startRay()
            } else {
                stopRay()
            }
        }

        private fun startRay() {
            if (animator?.isRunning == true) return
            stopRay()
            animator = android.animation.ValueAnimator.ofFloat(-0.45f, 1.45f).apply {
                duration = 4600L
                startDelay = startDelayMs
                repeatCount = android.animation.ValueAnimator.INFINITE
                repeatMode = android.animation.ValueAnimator.RESTART
                interpolator = android.view.animation.LinearInterpolator()
                addUpdateListener {
                    progress = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        }

        override fun onDetachedFromWindow() {
            stopRay()
            super.onDetachedFromWindow()
        }

        private fun stopRay() {
            animator?.cancel()
            animator = null
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            if (w <= 0f || h <= 0f) return

            val d = resources.displayMetrics.density
            val radius = cornerRadiusDp * d
            clipPath.reset()
            rect.set(0f, 0f, w, h)
            clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW)

            val diagonal = Math.sqrt((w * w + h * h).toDouble()).toFloat()
            val band = max(10f * d, minOf(w, h) * 0.42f)
            val ux = w / diagonal
            val uy = h / diagonal
            val nx = -uy
            val ny = ux
            val centerX = -w * 0.28f + w * 1.56f * progress
            val centerY = -h * 0.28f + h * 1.56f * progress
            val halfLength = diagonal * 0.82f
            val halfBand = band * 0.5f

            rayPath.reset()
            rayPath.moveTo(centerX - nx * halfLength - ux * halfBand, centerY - ny * halfLength - uy * halfBand)
            rayPath.lineTo(centerX + nx * halfLength - ux * halfBand, centerY + ny * halfLength - uy * halfBand)
            rayPath.lineTo(centerX + nx * halfLength + ux * halfBand, centerY + ny * halfLength + uy * halfBand)
            rayPath.lineTo(centerX - nx * halfLength + ux * halfBand, centerY - ny * halfLength + uy * halfBand)
            rayPath.close()

            shaderMatrix.setTranslate(centerX, centerY)
            rayShader?.setLocalMatrix(shaderMatrix)
            paint.shader = rayShader

            val save = canvas.save()
            canvas.clipPath(clipPath)
            canvas.drawPath(rayPath, paint)
            canvas.restoreToCount(save)
            paint.shader = null
        }
    }

    private class GearActionIconView(context: Context, private val icon: Icon) : View(context) {
        enum class Icon { EDIT, SHARE, TAG }

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
            val w = width.toFloat()
            val h = height.toFloat()
            val d = resources.displayMetrics.density
            paint.color = tint
            paint.strokeWidth = 1.7f * d
            paint.style = Paint.Style.STROKE

            when (icon) {
                Icon.EDIT -> {
                    canvas.drawLine(w * 0.25f, h * 0.72f, w * 0.72f, h * 0.25f, paint)
                    canvas.drawLine(w * 0.65f, h * 0.18f, w * 0.8f, h * 0.33f, paint)
                    canvas.drawLine(w * 0.22f, h * 0.78f, w * 0.34f, h * 0.74f, paint)
                    paint.style = Paint.Style.FILL
                    path.reset()
                    path.moveTo(w * 0.2f, h * 0.8f)
                    path.lineTo(w * 0.28f, h * 0.63f)
                    path.lineTo(w * 0.37f, h * 0.72f)
                    path.close()
                    canvas.drawPath(path, paint)
                }
                Icon.SHARE -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(w * 0.28f, h * 0.5f, w * 0.08f, paint)
                    canvas.drawCircle(w * 0.72f, h * 0.27f, w * 0.08f, paint)
                    canvas.drawCircle(w * 0.72f, h * 0.73f, w * 0.08f, paint)
                    paint.style = Paint.Style.STROKE
                    canvas.drawLine(w * 0.35f, h * 0.47f, w * 0.65f, h * 0.31f, paint)
                    canvas.drawLine(w * 0.35f, h * 0.53f, w * 0.65f, h * 0.69f, paint)
                }
                Icon.TAG -> {
                    path.reset()
                    path.moveTo(w * 0.24f, h * 0.22f)
                    path.lineTo(w * 0.66f, h * 0.22f)
                    path.lineTo(w * 0.82f, h * 0.38f)
                    path.lineTo(w * 0.46f, h * 0.78f)
                    path.lineTo(w * 0.18f, h * 0.5f)
                    path.close()
                    canvas.drawPath(path, paint)
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(w * 0.56f, h * 0.36f, w * 0.045f, paint)
                }
            }
        }
    }

    private class FeedIconView(context: Context, private val icon: Icon) : View(context) {
        enum class Icon { MORE, COMMENT, SHARE, LIKE }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val path = Path()
        private val rect = RectF()
        private var tint = Color.WHITE

        fun setTint(color: Int) {
            tint = color
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            val d = resources.displayMetrics.density
            paint.color = tint
            paint.strokeWidth = 1.65f * d
            paint.style = Paint.Style.STROKE

            when (icon) {
                Icon.MORE -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(w * 0.5f, h * 0.25f, w * 0.07f, paint)
                    canvas.drawCircle(w * 0.5f, h * 0.5f, w * 0.07f, paint)
                    canvas.drawCircle(w * 0.5f, h * 0.75f, w * 0.07f, paint)
                }
                Icon.COMMENT -> {
                    rect.set(w * 0.2f, h * 0.24f, w * 0.8f, h * 0.66f)
                    canvas.drawRoundRect(rect, 3f * d, 3f * d, paint)
                    path.reset()
                    path.moveTo(w * 0.38f, h * 0.66f)
                    path.lineTo(w * 0.32f, h * 0.82f)
                    path.lineTo(w * 0.52f, h * 0.66f)
                    canvas.drawPath(path, paint)
                }
                Icon.SHARE -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(w * 0.26f, h * 0.5f, w * 0.065f, paint)
                    canvas.drawCircle(w * 0.74f, h * 0.27f, w * 0.065f, paint)
                    canvas.drawCircle(w * 0.74f, h * 0.73f, w * 0.065f, paint)
                    paint.style = Paint.Style.STROKE
                    canvas.drawLine(w * 0.33f, h * 0.47f, w * 0.67f, h * 0.31f, paint)
                    canvas.drawLine(w * 0.33f, h * 0.53f, w * 0.67f, h * 0.69f, paint)
                }
                Icon.LIKE -> {
                    path.reset()
                    path.moveTo(w * 0.5f, h * 0.78f)
                    path.cubicTo(w * 0.16f, h * 0.55f, w * 0.18f, h * 0.28f, w * 0.34f, h * 0.27f)
                    path.cubicTo(w * 0.43f, h * 0.26f, w * 0.48f, h * 0.32f, w * 0.5f, h * 0.38f)
                    path.cubicTo(w * 0.52f, h * 0.32f, w * 0.57f, h * 0.26f, w * 0.66f, h * 0.27f)
                    path.cubicTo(w * 0.82f, h * 0.28f, w * 0.84f, h * 0.55f, w * 0.5f, h * 0.78f)
                    canvas.drawPath(path, paint)
                }
            }
        }
    }

    private class TopIconView(context: Context, private val icon: Icon) : View(context) {
        enum class Icon { MENU, SEARCH, BELL, MESSAGE, MIC, SHIELD, BOLT }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val path = Path()
        private val rect = RectF()
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
                Icon.MIC -> {
                    rect.set(w * 0.38f, h * 0.18f, w * 0.62f, h * 0.55f)
                    canvas.drawRoundRect(rect, 7f * d, 7f * d, paint)
                    rect.set(w * 0.26f, h * 0.38f, w * 0.74f, h * 0.76f)
                    canvas.drawArc(rect, 20f, 140f, false, paint)
                    canvas.drawLine(w * 0.5f, h * 0.74f, w * 0.5f, h * 0.86f, paint)
                    canvas.drawLine(w * 0.38f, h * 0.86f, w * 0.62f, h * 0.86f, paint)
                }
                Icon.SHIELD -> {
                    path.reset()
                    path.moveTo(w * 0.5f, h * 0.18f)
                    path.lineTo(w * 0.76f, h * 0.3f)
                    path.lineTo(w * 0.7f, h * 0.66f)
                    path.lineTo(w * 0.5f, h * 0.84f)
                    path.lineTo(w * 0.3f, h * 0.66f)
                    path.lineTo(w * 0.24f, h * 0.3f)
                    path.close()
                    paint.style = Paint.Style.FILL
                    canvas.drawPath(path, paint)
                    paint.style = Paint.Style.STROKE
                }
                Icon.BOLT -> {
                    path.reset()
                    path.moveTo(w * 0.58f, h * 0.12f)
                    path.lineTo(w * 0.3f, h * 0.56f)
                    path.lineTo(w * 0.52f, h * 0.56f)
                    path.lineTo(w * 0.42f, h * 0.88f)
                    path.lineTo(w * 0.72f, h * 0.42f)
                    path.lineTo(w * 0.5f, h * 0.42f)
                    path.close()
                    paint.style = Paint.Style.FILL
                    canvas.drawPath(path, paint)
                    paint.style = Paint.Style.STROKE
                }
            }
        }
    }

    private class NavIconView(context: Context, private val icon: Icon) : View(context) {
        enum class Icon { HOME, BARS, COMPASS, USERS, CART }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val path = Path()
        private val rect = RectF()
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
            paint.strokeWidth = 1.8f * d
            paint.style = Paint.Style.STROKE

            when (icon) {
                Icon.HOME -> {
                    path.reset()
                    path.moveTo(w * 0.18f, h * 0.5f)
                    path.lineTo(w * 0.5f, h * 0.22f)
                    path.lineTo(w * 0.82f, h * 0.5f)
                    canvas.drawPath(path, paint)
                    rect.set(w * 0.29f, h * 0.48f, w * 0.71f, h * 0.82f)
                    canvas.drawRoundRect(rect, 2f * d, 2f * d, paint)
                }
                Icon.BARS -> {
                    repeat(3) { i ->
                        val left = w * (0.26f + i * 0.18f)
                        canvas.drawLine(left, h * 0.78f, left, h * (0.58f - i * 0.14f), paint)
                    }
                }
                Icon.COMPASS -> {
                    canvas.drawCircle(w / 2f, h / 2f, s * 0.31f, paint)
                    path.reset()
                    path.moveTo(w * 0.58f, h * 0.32f)
                    path.lineTo(w * 0.47f, h * 0.57f)
                    path.lineTo(w * 0.42f, h * 0.68f)
                    path.lineTo(w * 0.53f, h * 0.43f)
                    path.close()
                    paint.style = Paint.Style.FILL
                    canvas.drawPath(path, paint)
                    paint.style = Paint.Style.STROKE
                }
                Icon.USERS -> {
                    canvas.drawCircle(w * 0.38f, h * 0.39f, s * 0.13f, paint)
                    canvas.drawCircle(w * 0.62f, h * 0.39f, s * 0.13f, paint)
                    rect.set(w * 0.2f, h * 0.58f, w * 0.52f, h * 0.8f)
                    canvas.drawArc(rect, 200f, 140f, false, paint)
                    rect.set(w * 0.48f, h * 0.58f, w * 0.8f, h * 0.8f)
                    canvas.drawArc(rect, 200f, 140f, false, paint)
                }
                Icon.CART -> {
                    path.reset()
                    path.moveTo(w * 0.2f, h * 0.28f)
                    path.lineTo(w * 0.28f, h * 0.28f)
                    path.lineTo(w * 0.36f, h * 0.64f)
                    path.lineTo(w * 0.75f, h * 0.64f)
                    path.lineTo(w * 0.82f, h * 0.38f)
                    path.lineTo(w * 0.32f, h * 0.38f)
                    canvas.drawPath(path, paint)
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(w * 0.42f, h * 0.78f, s * 0.045f, paint)
                    canvas.drawCircle(w * 0.7f, h * 0.78f, s * 0.045f, paint)
                    paint.style = Paint.Style.STROKE
                }
            }
        }
    }

    private class ProIconView(context: Context, private val icon: Icon) : View(context) {
        enum class Icon { MEDAL, AD_BLOCK, FOUR_K, STATS, ARROW }

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val path = Path()
        private val rect = RectF()
        private var tint = Color.WHITE

        fun setTint(color: Int) {
            tint = color
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            val d = resources.displayMetrics.density
            val w = width.toFloat()
            val h = height.toFloat()
            paint.color = tint
            paint.strokeWidth = 1.6f * d
            paint.style = Paint.Style.STROKE

            when (icon) {
                Icon.MEDAL -> {
                    path.reset()
                    path.moveTo(w * 0.3f, h * 0.3f)
                    path.lineTo(w * 0.3f, h * 0.8f)
                    path.lineTo(w * 0.5f, h * 0.65f)
                    path.lineTo(w * 0.7f, h * 0.8f)
                    path.lineTo(w * 0.7f, h * 0.3f)
                    path.close()
                    paint.style = Paint.Style.FILL
                    canvas.drawPath(path, paint)
                    
                    canvas.drawCircle(w * 0.5f, h * 0.4f, w * 0.25f, paint)
                    paint.color = Color.rgb(10, 20, 5)
                    path.reset()
                    path.moveTo(w * 0.5f, h * 0.25f)
                    path.lineTo(w * 0.55f, h * 0.35f)
                    path.lineTo(w * 0.65f, h * 0.38f)
                    path.lineTo(w * 0.58f, h * 0.45f)
                    path.lineTo(w * 0.6f, h * 0.55f)
                    path.lineTo(w * 0.5f, h * 0.5f)
                    path.lineTo(w * 0.4f, h * 0.55f)
                    path.lineTo(w * 0.42f, h * 0.45f)
                    path.lineTo(w * 0.35f, h * 0.38f)
                    path.lineTo(w * 0.45f, h * 0.35f)
                    path.close()
                    canvas.drawPath(path, paint)
                }
                Icon.AD_BLOCK -> {
                    paint.style = Paint.Style.STROKE
                    canvas.drawCircle(w * 0.5f, h * 0.5f, w * 0.35f, paint)
                    canvas.drawLine(w * 0.25f, h * 0.25f, w * 0.75f, h * 0.75f, paint)
                }
                Icon.FOUR_K -> {
                    rect.set(w * 0.1f, h * 0.25f, w * 0.9f, h * 0.75f)
                    paint.style = Paint.Style.STROKE
                    canvas.drawRoundRect(rect, 2f * d, 2f * d, paint)
                    paint.style = Paint.Style.FILL
                    paint.textSize = h * 0.4f
                    paint.typeface = Typeface.DEFAULT_BOLD
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("4K", w * 0.5f, h * 0.65f, paint)
                }
                Icon.STATS -> {
                    paint.style = Paint.Style.FILL
                    canvas.drawRect(w * 0.2f, h * 0.6f, w * 0.35f, h * 0.8f, paint)
                    canvas.drawRect(w * 0.42f, h * 0.4f, w * 0.57f, h * 0.8f, paint)
                    canvas.drawRect(w * 0.64f, h * 0.2f, w * 0.79f, h * 0.8f, paint)
                }
                Icon.ARROW -> {
                    paint.style = Paint.Style.STROKE
                    canvas.drawLine(w * 0.2f, h * 0.5f, w * 0.8f, h * 0.5f, paint)
                    canvas.drawLine(w * 0.5f, h * 0.2f, w * 0.8f, h * 0.5f, paint)
                    canvas.drawLine(w * 0.5f, h * 0.8f, w * 0.8f, h * 0.5f, paint)
                }
            }
        }
    }
}
