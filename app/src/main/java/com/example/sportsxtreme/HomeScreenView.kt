package com.example.sportsxtreme

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
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

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
                        setPadding(dp(14), dp(14), dp(14), dp(14))
                    }

                    content.addView(locationRow(context))
                    content.addView(xtremeMediaCard(context))
                    content.addView(scoreCardsSection(context), blockParams(top = 14))
                    content.addView(proPassCard(context), blockParams(top = 16))
                    content.addView(personalizeGearSection(context), blockParams(top = 12))
                    content.addView(sectionHeader(context, "Sports Feed", null), blockParams(top = 14))
                    content.addView(feedCard(context), blockParams(top = 8))

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
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            
            // Premium app bar style
            background = GradientDrawable().apply {
                setColor(Color.rgb(6, 12, 17)) // Solid dark color to separate from body
            }
            // Removed standard elevation to use custom gradient shadow/glow below
            setPadding(dp(16), dp(20), dp(16), dp(12)) // Increased top padding to handle status bar area

            addView(TopIconView(context, TopIconView.Icon.MENU).apply {
                setTint(Color.WHITE)
            }, LinearLayout.LayoutParams(dp(22), dp(22)))
            addView(sxLogo(context), LinearLayout.LayoutParams(dp(86), dp(38)).apply {
                leftMargin = dp(12)
            })
            addView(View(context), LinearLayout.LayoutParams(0, 1, 1f))
            addView(TopIconView(context, TopIconView.Icon.SEARCH).apply {
                setTint(Color.WHITE)
            }, LinearLayout.LayoutParams(dp(21), dp(21)).apply { leftMargin = dp(14) })
            addView(TopIconView(context, TopIconView.Icon.BELL).apply {
                setTint(Color.WHITE)
            }, LinearLayout.LayoutParams(dp(21), dp(21)).apply { leftMargin = dp(14) })
            addView(TopIconView(context, TopIconView.Icon.MESSAGE).apply {
                setTint(Color.WHITE)
            }, LinearLayout.LayoutParams(dp(21), dp(21)).apply { leftMargin = dp(14) })
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
            setPadding(0, 0, 0, dp(10))
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
        return FrameLayout(context).apply {
            background = GradientDrawable().apply {
                cornerRadius = dp(7).toFloat()
                setColor(Color.rgb(7, 15, 25))
                setStroke(dp(1), Color.argb(45, 255, 255, 255))
            }
            setPadding(dp(14), dp(11), dp(14), dp(11))
            addView(TextView(context).apply {
                text = context.getString(R.string.str_xtrememedia)
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(2, 11, 5))
                textSize = 17f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                includeFontPadding = false
                background = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(primary, Color.rgb(0, 126, 255))
                ).apply {
                    cornerRadius = dp(10).toFloat()
                }
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(50)))
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
                addView(View(context), LinearLayout.LayoutParams(0, 1, 1f))
                addView(TextView(context).apply {
                    text = context.getString(R.string.str_view_all)
                    setTextColor(primary)
                    textSize = 10f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                })
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            })

            addView(HorizontalScrollView(context).apply {
                isHorizontalScrollBarEnabled = false
                clipToPadding = false
                overScrollMode = View.OVER_SCROLL_NEVER
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 0, dp(14), dp(4))
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
            elevation = dp(14).toFloat()
            translationZ = dp(8).toFloat()
            background = GradientDrawable().apply {
                cornerRadius = dp(14).toFloat()
                setColor(Color.rgb(8, 28, 58))
            }
            addView(ScoreCardGlowView(context), FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(15))

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
                    addView(scoreTeam(context, leftName, leftScore, leftOvers, TopIconView.Icon.SHIELD, primary), LinearLayout.LayoutParams(0, dp(116), 1f))
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
                    addView(scoreTeam(context, rightName, rightScore, rightOvers, TopIconView.Icon.BOLT, cyan), LinearLayout.LayoutParams(0, dp(116), 1f))
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(116)).apply {
                    topMargin = dp(18)
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
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(32)).apply {
                    topMargin = dp(14)
                })

                addView(TextView(context).apply {
                    text = note
                    gravity = Gravity.CENTER
                    setTextColor(Color.rgb(178, 191, 205))
                    textSize = 9f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(12)
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
                setPadding(dp(8), dp(10), dp(8), dp(10))
                addView(FrameLayout(context).apply {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.argb(42, Color.red(accent), Color.green(accent), Color.blue(accent)))
                        setStroke(dp(1), Color.argb(140, Color.red(accent), Color.green(accent), Color.blue(accent)))
                    }
                    addView(TopIconView(context, icon).apply {
                        setTint(accent)
                    }, FrameLayout.LayoutParams(dp(22), dp(22), Gravity.CENTER))
                }, LinearLayout.LayoutParams(dp(42), dp(42)))
                addView(TextView(context).apply {
                    text = name
                    gravity = Gravity.CENTER
                    setTextColor(accent)
                    textSize = 9f
                    letterSpacing = 0.08f
                    typeface = Typeface.DEFAULT_BOLD
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(8)
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

    private fun proPassCard(context: Context): View {
        return FrameLayout(context).apply {
            clipChildren = false
            clipToPadding = false

            addView(View(context).apply {
                background = GradientDrawable().apply {
                    cornerRadius = dp(21).toFloat()
                    setColor(Color.argb(165, 0, 0, 0))
                }
                translationY = dp(12).toFloat()
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(512)).apply {
                setMargins(dp(10), dp(12), dp(10), 0)
            })

            // The main card container
            val cardContainer = FrameLayout(context).apply {
                elevation = dp(48).toFloat()
                translationZ = dp(18).toFloat()
                outlineAmbientShadowColor = Color.BLACK
                outlineSpotShadowColor = Color.BLACK
                
                background = GradientDrawable().apply {
                    cornerRadius = dp(20).toFloat()
                    setColor(Color.rgb(5, 8, 11))
                }
                clipToOutline = true

                addView(ProPassCardArtView(context), FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

                val content = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(25), dp(24), dp(25), dp(30))

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
                                    setColor(primary)
                                }
                            }, LinearLayout.LayoutParams(dp(5), dp(24)))
                            
                            addView(TextView(context).apply {
                                text = context.getString(R.string.str_elite_member)
                                setTextColor(primary)
                                textSize = 9.2f
                                letterSpacing = 0.16f
                                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                                includeFontPadding = false
                            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                                leftMargin = dp(7)
                            })
                        }
                        addView(tagBox, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                        addView(FrameLayout(context).apply {
                            background = GradientDrawable().apply {
                                cornerRadius = dp(9).toFloat()
                                setColor(Color.argb(58, 255, 255, 255))
                                setStroke(1, Color.argb(150, 193, 255, 0))
                            }
                            addView(ProIconView(context, ProIconView.Icon.MEDAL).apply {
                                setTint(primary)
                            }, FrameLayout.LayoutParams(dp(21), dp(21), Gravity.CENTER))
                            addView(PremiumRayView(context, 9, 500L), FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                        }, LinearLayout.LayoutParams(dp(38), dp(38)))
                    }
                    addView(headerRow, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

                    // 2. Large PRO PASS Typography
                    val titleRow = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        addView(TextView(context).apply {
                            text = context.getString(R.string.str_pro)
                            setTextColor(Color.WHITE)
                            textSize = 45f
                            letterSpacing = -0.02f
                            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC)
                            includeFontPadding = false
                        })
                        addView(TextView(context).apply {
                            text = context.getString(R.string.str_pass)
                            setTextColor(primary)
                            textSize = 45f
                            letterSpacing = -0.02f
                            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC)
                            includeFontPadding = false
                        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                            topMargin = dp(-5)
                        })
                    }
                    addView(titleRow, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(18) })

                    // 3. Features
                    val features = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                    }
                    features.addView(proFeatureLine(context, ProIconView.Icon.AD_BLOCK, "ZERO AD", "INTERRUPTIONS"), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(28) })
                    features.addView(proFeatureLine(context, ProIconView.Icon.FOUR_K, "4K ULTRA HDR", "STREAMING"), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(17) })
                    features.addView(proFeatureLine(context, ProIconView.Icon.STATS, "PREMIUM INSIDER", "STATS"), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(17) })
                    addView(features, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

                    addView(View(context), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.72f))

                    // 4. Large centered pricing
                    val priceRow = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER
                        
                        addView(TextView(context).apply {
                            text = context.getString(R.string.str_19)
                            setTextColor(Color.WHITE)
                            textSize = 44f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD_ITALIC)
                            includeFontPadding = false
                        })
                        addView(TextView(context).apply {
                            text = context.getString(R.string.str_month)
                            setTextColor(Color.rgb(156, 166, 156))
                            textSize = 7.2f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                            includeFontPadding = false
                        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                            leftMargin = dp(4)
                            bottomMargin = dp(7)
                        })
                    }
                    addView(priceRow, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) })

                    // Trust text
                    addView(TextView(context).apply {
                        text = context.getString(R.string.str_cancel_anytime__no_h)
                        gravity = Gravity.CENTER
                        setTextColor(primary)
                        textSize = 6.3f
                        letterSpacing = 0.06f
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        includeFontPadding = false
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(1) })

                    // 5. CTA Button
                    val btnContainer = FrameLayout(context).apply {
                        setPadding(0, 0, 0, dp(8))
                        addView(View(context).apply {
                            background = GradientDrawable().apply {
                                cornerRadius = dp(8).toFloat()
                                setColor(Color.argb(92, 0, 0, 0))
                            }
                        }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(39)).apply {
                            topMargin = dp(8)
                            leftMargin = dp(5)
                            rightMargin = dp(5)
                        })

                        // Actual Button
                        val btn = FrameLayout(context).apply {
                            background = GradientDrawable(
                                GradientDrawable.Orientation.TOP_BOTTOM,
                                intArrayOf(Color.rgb(196, 255, 44), Color.rgb(166, 242, 0))
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
                                    setTextColor(Color.rgb(4, 9, 4))
                                    textSize = 9.3f
                                    letterSpacing = 0.08f
                                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                                    includeFontPadding = false
                                })
                                addView(ProIconView(context, ProIconView.Icon.ARROW).apply {
                                    setTint(Color.rgb(4, 9, 4))
                                }, LinearLayout.LayoutParams(dp(15), dp(15)).apply { leftMargin = dp(9) })
                            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                        }
                        addView(btn, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(42)))
                    }
                    addView(btnContainer, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(11) })
                }
                addView(content, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            }

            addView(cardContainer, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(522)))
        }
    }

    private fun proFeatureLine(context: Context, icon: ProIconView.Icon, title: String, subtitle: String): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            
            val iconBox = FrameLayout(context).apply {
                elevation = dp(4).toFloat()
                background = GradientDrawable().apply {
                    cornerRadius = dp(6).toFloat()
                    setColor(Color.argb(42, 255, 255, 255))
                    setStroke(1, Color.argb(58, 193, 255, 0))
                }
                addView(View(context).apply {
                    background = GradientDrawable().apply {
                        gradientType = GradientDrawable.RADIAL_GRADIENT
                        gradientRadius = dp(26).toFloat()
                        colors = intArrayOf(Color.argb(34, 193, 255, 0), Color.TRANSPARENT)
                    }
                }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
                addView(ProIconView(context, icon).apply {
                    setTint(primary)
                }, FrameLayout.LayoutParams(dp(18), dp(18), Gravity.CENTER))
            }
            addView(iconBox, LinearLayout.LayoutParams(dp(36), dp(36)))
            
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(TextView(context).apply {
                    text = title
                    setTextColor(Color.WHITE)
                    textSize = 10.4f
                    letterSpacing = 0.01f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    includeFontPadding = false
                })
                addView(TextView(context).apply {
                    text = subtitle
                    setTextColor(Color.WHITE)
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
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = dp(17) })
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

    private class ProPassCardArtView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val path = Path()
        private val rect = RectF()
        private var glowBitmap: android.graphics.Bitmap? = null
        private var bgShader: Shader? = null

        init {
            // LAYER_TYPE_SOFTWARE removed to prevent vanishing on resume
        }

        override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
            super.onWindowFocusChanged(hasWindowFocus)
            if (hasWindowFocus) invalidate()
        }

        override fun onVisibilityAggregated(isVisible: Boolean) {
            super.onVisibilityAggregated(isVisible)
            if (isVisible) invalidate()
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            val wf = w.toFloat()
            val hf = h.toFloat()
            if (wf > 0 && hf > 0) {
                bgShader = LinearGradient(
                    0f, 0f, wf, hf,
                    intArrayOf(Color.rgb(17, 24, 17), Color.rgb(7, 10, 12), Color.rgb(4, 6, 8)),
                    floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP
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
                intArrayOf(Color.argb(42, 193, 255, 0), Color.argb(12, 193, 255, 0), Color.TRANSPARENT),
                floatArrayOf(0f, 0.44f, 1f),
                Shader.TileMode.CLAMP
            )
            bitmapCanvas.drawRoundRect(bounds, radius, radius, glowPaint)

            glowPaint.shader = LinearGradient(
                0f,
                0f,
                w * 0.62f,
                h * 0.28f,
                intArrayOf(Color.argb(38, 193, 255, 0), Color.argb(8, 193, 255, 0), Color.TRANSPARENT),
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

            rect.set(0f, 0f, w, h)
            paint.shader = bgShader
            canvas.drawRoundRect(rect, radius, radius, paint)
            paint.shader = null

            if (glowBitmap == null) {
                rebuildGlowBitmap(width, height)
            }
            glowBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }

            paint.style = Paint.Style.FILL
            paint.color = Color.argb(42, 193, 255, 0)
            path.reset()
            path.moveTo(w * 0.76f, h * 0.78f)
            path.lineTo(w * 0.86f, h * 0.78f)
            path.lineTo(w * 0.76f, h)
            path.lineTo(w * 0.66f, h)
            path.close()
            canvas.drawPath(path, paint)

            paint.color = Color.argb(28, 255, 255, 255)
            path.reset()
            path.moveTo(w * 0.9f, h * 0.74f)
            path.lineTo(w, h * 0.72f)
            path.lineTo(w * 0.91f, h)
            path.lineTo(w * 0.81f, h)
            path.close()
            canvas.drawPath(path, paint)

            paint.color = Color.argb(18, 193, 255, 0)
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
            paint.color = Color.argb(16, 255, 255, 255)
            repeat(3) { index ->
                val y = h * (0.82f + index * 0.045f)
                canvas.drawLine(w * 0.58f, y, w * 0.96f, y - h * 0.13f, paint)
            }
            paint.style = Paint.Style.FILL
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
