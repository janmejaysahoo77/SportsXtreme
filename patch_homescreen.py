import sys

file_path = 'app/src/main/java/com/example/sportsxtreme/HomeScreenView.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Normalize CRLF to LF
content = content.replace('\r\n', '\n')

init_target = '''    private lateinit var navRow: LinearLayout
    private val cachedTabs = mutableMapOf<Int, View>()

    init {
        clipChildren = false
        clipToPadding = false
        setBackgroundColor(bg)
        addView(HomeBackgroundView(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(createShell(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }'''

init_replacement = '''    private lateinit var navRow: LinearLayout
    private val cachedTabs = mutableMapOf<Int, View>()
    private lateinit var drawerLayout: DrawerLayout

    init {
        clipChildren = false
        clipToPadding = false
        setBackgroundColor(bg)

        drawerLayout = DrawerLayout(context).apply {
            val contentFrame = FrameLayout(context).apply {
                addView(HomeBackgroundView(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
                addView(createShell(context), LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            }
            addView(contentFrame, DrawerLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            
            val drawerView = createDrawerContent(context)
            val drawerParams = DrawerLayout.LayoutParams(dp(300), LayoutParams.MATCH_PARENT).apply {
                gravity = GravityCompat.START
            }
            addView(drawerView, drawerParams)
        }
        addView(drawerLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun createDrawerContent(context: Context): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(20, 24, 28)) // Dark grayish background from screenshot
            isClickable = true
            isFocusable = true

            // Header Section
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(40), dp(20), dp(20))
                
                // Profile row
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL

                    addView(FrameLayout(context).apply {
                        // Colored Circle
                        addView(View(context).apply {
                            background = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setColor(Color.rgb(65, 95, 120))
                                setStroke(dp(2), primary)
                            }
                        }, FrameLayout.LayoutParams(dp(50), dp(50)))
                        
                        addView(DrawerIconView(context, DrawerIconView.Icon.CHECK).apply {
                            setTint(Color.BLACK)
                            background = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setColor(primary)
                            }
                            setPadding(dp(2), dp(2), dp(2), dp(2))
                        }, FrameLayout.LayoutParams(dp(16), dp(16), Gravity.BOTTOM or Gravity.END))
                    }, LinearLayout.LayoutParams(dp(50), dp(50)))

                    addView(View(context), LinearLayout.LayoutParams(0, 1, 1f))
                    
                    addView(DrawerIconView(context, DrawerIconView.Icon.RIGHT_ARROW).apply {
                        setTint(Color.GRAY)
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setColor(Color.rgb(40, 45, 50))
                        }
                    }, LinearLayout.LayoutParams(dp(28), dp(28)))
                })
                
                // Name
                addView(TextView(context).apply {
                    text = "Manoj Kumar Das"
                    setTextColor(Color.WHITE)
                    textSize = 20f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(16)
                })

                // ID
                addView(TextView(context).apply {
                    text = "56254585"
                    setTextColor(Color.GRAY)
                    textSize = 12f
                    includeFontPadding = false
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(4)
                })

                // Pro Member Row
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    
                    addView(LinearLayout(context).apply {
                        gravity = Gravity.CENTER_VERTICAL
                        background = GradientDrawable().apply {
                            cornerRadius = dp(12).toFloat()
                            setColor(primary)
                        }
                        setPadding(dp(8), dp(4), dp(8), dp(4))
                        addView(DrawerIconView(context, DrawerIconView.Icon.PRO_STAR).apply {
                            setTint(Color.BLACK)
                        }, LinearLayout.LayoutParams(dp(12), dp(12)))
                        addView(TextView(context).apply {
                            text = "PRO MEMBER"
                            setTextColor(Color.BLACK)
                            textSize = 10f
                            typeface = Typeface.DEFAULT_BOLD
                            includeFontPadding = false
                        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                            leftMargin = dp(4)
                        })
                    })

                    addView(TextView(context).apply {
                        text = "Profile completed • 58%"
                        setTextColor(primary)
                        textSize = 11f
                        typeface = Typeface.DEFAULT_BOLD
                        includeFontPadding = false
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        leftMargin = dp(12)
                    })
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = dp(16)
                })

                // Progress Bar
                addView(FrameLayout(context).apply {
                    background = GradientDrawable().apply {
                        cornerRadius = dp(2).toFloat()
                        setColor(Color.rgb(60, 65, 70))
                    }
                    addView(View(context).apply {
                        background = GradientDrawable().apply {
                            cornerRadius = dp(2).toFloat()
                            setColor(primary)
                        }
                    }, FrameLayout.LayoutParams(0, dp(4)).apply {
                        width = dp(150) // Simulating 58%
                    })
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(4)).apply {
                    topMargin = dp(10)
                })
                
                // Divider
                addView(View(context).apply {
                    setBackgroundColor(Color.argb(50, 255, 255, 255))
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)).apply {
                    topMargin = dp(20)
                })
            })

            // Scrollable Menu Content
            addView(ScrollView(context).apply {
                clipToPadding = false
                isVerticalScrollBarEnabled = false
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(20), 0, dp(20), dp(40))

                    // Pro Banner
                    addView(LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        background = GradientDrawable().apply {
                            cornerRadius = dp(8).toFloat()
                            setColor(Color.rgb(35, 45, 30))
                            setStroke(dp(1), Color.argb(100, 193, 255, 0))
                        }
                        setPadding(dp(16), dp(16), dp(16), dp(16))
                        
                        addView(DrawerIconView(context, DrawerIconView.Icon.PRO_STAR).apply {
                            setTint(Color.BLACK)
                            background = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setColor(primary)
                            }
                            setPadding(dp(4), dp(4), dp(4), dp(4))
                        }, LinearLayout.LayoutParams(dp(24), dp(24)))
                        
                        addView(TextView(context).apply {
                            text = "PRO at ₹199"
                            setTextColor(Color.WHITE)
                            textSize = 13f
                            typeface = Typeface.DEFAULT_BOLD
                            includeFontPadding = false
                        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                            leftMargin = dp(12)
                        })

                        addView(TextView(context).apply {
                            text = "No autopay"
                            setTextColor(Color.GRAY)
                            textSize = 11f
                            includeFontPadding = false
                        })
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                        bottomMargin = dp(20)
                    })

                    // Drawer Items
                    val items = listOf(
                        DrawerItem("Add a Tournament/Series", DrawerIconView.Icon.PLUS_CIRCLE, badge = "FREE"),
                        DrawerItem("Start A Match", DrawerIconView.Icon.BAT, badge = "FREE"),
                        DrawerItem("Go Live", DrawerIconView.Icon.VIDEO),
                        DrawerItem("My Cricket", DrawerIconView.Icon.STADIUM),
                        DrawerItem("My Stats", DrawerIconView.Icon.STATS),
                        DrawerItem("SportsXtreme Store", DrawerIconView.Icon.STORE, hasDot = true),
                        DrawerItem("SportsXtreme Awards", DrawerIconView.Icon.TROPHY),
                        DrawerItem("Associations", DrawerIconView.Icon.USERS),
                        DrawerItem("Clubs", DrawerIconView.Icon.BUILDING),
                        DrawerItem("Contact", DrawerIconView.Icon.HELP),
                        DrawerItem("Share the app", DrawerIconView.Icon.SHARE),
                        DrawerItem("Rate us", DrawerIconView.Icon.STAR),
                        DrawerItem("App code", DrawerIconView.Icon.QR),
                        DrawerItem("More", DrawerIconView.Icon.MORE, isExpandable = true)
                    )

                    items.forEach { item ->
                        addView(createDrawerItemView(context, item), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)))
                    }

                    // Expanded Sub-items (Hardcoded expanded for visual matching)
                    addView(LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(44), 0, 0, 0)
                        
                        addView(createSubItemView(context, "What's New", DrawerIconView.Icon.INFO), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(40)))
                        addView(createSubItemView(context, "Change Language", DrawerIconView.Icon.GLOBE), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(40)))
                        
                        // Social Row
                        addView(LinearLayout(context).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                            addView(DrawerIconView(context, DrawerIconView.Icon.SOCIAL_IG).apply { setTint(Color.LTGRAY) }, LinearLayout.LayoutParams(dp(20), dp(20)))
                            addView(DrawerIconView(context, DrawerIconView.Icon.SOCIAL_FB).apply { setTint(Color.LTGRAY) }, LinearLayout.LayoutParams(dp(20), dp(20)).apply { leftMargin = dp(16) })
                            addView(DrawerIconView(context, DrawerIconView.Icon.SOCIAL_X).apply { setTint(Color.LTGRAY) }, LinearLayout.LayoutParams(dp(20), dp(20)).apply { leftMargin = dp(16) })
                            addView(DrawerIconView(context, DrawerIconView.Icon.SOCIAL_YT).apply { setTint(Color.LTGRAY) }, LinearLayout.LayoutParams(dp(20), dp(20)).apply { leftMargin = dp(16) })
                        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(40)))

                        // Footer Links
                        addView(createFooterLinkView(context, "About Us"), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(32)))
                        addView(createFooterLinkView(context, "Help / FAQs"), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(32)))
                        addView(createFooterLinkView(context, "Privacy Policy"), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(32)))

                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

                })
            })
        }
    }

    private data class DrawerItem(val label: String, val icon: DrawerIconView.Icon, val badge: String? = null, val hasDot: Boolean = false, val isExpandable: Boolean = false)

    private fun createDrawerItemView(context: Context, item: DrawerItem): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            
            addView(DrawerIconView(context, item.icon).apply {
                setTint(Color.rgb(200, 210, 215))
            }, LinearLayout.LayoutParams(dp(22), dp(22)))

            addView(TextView(context).apply {
                text = item.label
                setTextColor(Color.rgb(220, 230, 235))
                textSize = 14f
                includeFontPadding = false
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = dp(16)
            })

            if (item.badge != null) {
                addView(TextView(context).apply {
                    text = item.badge
                    setTextColor(Color.LTGRAY)
                    textSize = 9f
                    typeface = Typeface.DEFAULT_BOLD
                    background = GradientDrawable().apply {
                        cornerRadius = dp(4).toFloat()
                        setColor(Color.rgb(60, 65, 70))
                    }
                    setPadding(dp(6), dp(2), dp(6), dp(2))
                })
            }

            if (item.hasDot) {
                addView(View(context).apply {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.rgb(255, 180, 0))
                    }
                }, LinearLayout.LayoutParams(dp(6), dp(6)))
            }

            if (item.isExpandable) {
                addView(DrawerIconView(context, DrawerIconView.Icon.RIGHT_ARROW).apply {
                    setTint(Color.WHITE) // Should be UP arrow, but we use RIGHT_ARROW and rotate it
                    rotation = -90f
                }, LinearLayout.LayoutParams(dp(16), dp(16)))
            }
        }
    }

    private fun createSubItemView(context: Context, label: String, icon: DrawerIconView.Icon): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            
            addView(DrawerIconView(context, icon).apply {
                setTint(Color.LTGRAY)
            }, LinearLayout.LayoutParams(dp(16), dp(16)))

            addView(TextView(context).apply {
                text = label
                setTextColor(Color.rgb(200, 210, 215))
                textSize = 13f
                includeFontPadding = false
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                leftMargin = dp(16)
            })
        }
    }

    private fun createFooterLinkView(context: Context, label: String): View {
        return TextView(context).apply {
            text = label
            setTextColor(Color.rgb(180, 190, 195))
            textSize = 12f
            includeFontPadding = false
            gravity = Gravity.CENTER_VERTICAL
        }
    }'''

if init_target in content:
    content = content.replace(init_target, init_replacement)
    print('Replaced init block')
else:
    print('Failed to find init_target')

host_menu_target = '''            addView(TopIconView(context, TopIconView.Icon.MENU).apply {
                setTint(Color.rgb(142, 158, 153))
            }, LinearLayout.LayoutParams(dp(20), dp(20)))'''
host_menu_replacement = '''            addView(TopIconView(context, TopIconView.Icon.MENU).apply {
                setTint(Color.rgb(142, 158, 153))
                setOnClickListener { openDrawer() }
            }, LinearLayout.LayoutParams(dp(20), dp(20)))'''

if host_menu_target in content:
    content = content.replace(host_menu_target, host_menu_replacement)
    print('Replaced host menu')
else:
    print('Failed to find host_menu_target')

top_menu_target = '''                addView(TopIconView(context, TopIconView.Icon.MENU).apply {
                    setTint(Color.WHITE)
                }, LinearLayout.LayoutParams(dp(22), dp(22)))'''
top_menu_replacement = '''                addView(TopIconView(context, TopIconView.Icon.MENU).apply {
                    setTint(Color.WHITE)
                    setOnClickListener { openDrawer() }
                }, LinearLayout.LayoutParams(dp(22), dp(22)))'''

if top_menu_target in content:
    content = content.replace(top_menu_target, top_menu_replacement)
    print('Replaced top menu')
else:
    print('Failed to find top_menu_target')

import_target = '''import android.widget.TextView
import kotlin.math.max'''
import_replacement = '''import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import kotlin.math.max'''
if import_target in content:
    content = content.replace(import_target, import_replacement)
    print('Replaced imports')
else:
    print('Failed to find import_target')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)
