import sys

with open('app/src/main/java/com/example/sportsxtreme/HomeScreenView.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
skip = False
i = 0
while i < len(lines):
    line = lines[i]
    if 'private fun topBar(context: Context): View {' in line:
        skip = True
    elif skip and '    private fun xtremeTopBarButtons(context: Context): View {' in line:
        skip = False

    if not skip:
        new_lines.append(line)
    i += 1

topbar_code = """
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
                    setOnClickListener { openDrawer() }
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
"""

final_lines = []
for line in new_lines:
    if '    private fun xtremeTopBarButtons(context: Context): View {' in line:
        final_lines.append(topbar_code[1:])
    final_lines.append(line)

with open('app/src/main/java/com/example/sportsxtreme/HomeScreenView.kt', 'w', encoding='utf-8') as f:
    f.writelines(final_lines)
