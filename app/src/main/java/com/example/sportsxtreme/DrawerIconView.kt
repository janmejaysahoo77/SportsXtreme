package com.example.sportsxtreme

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class DrawerIconView(context: Context, private val icon: Icon) : View(context) {

    enum class Icon {
        PLUS_CIRCLE, BAT, VIDEO, STADIUM, STATS, STORE, TROPHY, USERS, BUILDING, HELP,
        SHARE, STAR, QR, MORE, INFO, GLOBE, SOCIAL_IG, SOCIAL_FB, SOCIAL_X, SOCIAL_YT, RIGHT_ARROW, CHECK, PRO_STAR
    }

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
        paint.strokeWidth = 1.6f * d
        paint.style = Paint.Style.STROKE

        when (icon) {
            Icon.PLUS_CIRCLE -> {
                canvas.drawCircle(w / 2, h / 2, s * 0.35f, paint)
                canvas.drawLine(w / 2, h * 0.35f, w / 2, h * 0.65f, paint)
                canvas.drawLine(w * 0.35f, h / 2, w * 0.65f, h / 2, paint)
            }
            Icon.BAT -> {
                path.reset()
                path.moveTo(w * 0.65f, h * 0.25f)
                path.lineTo(w * 0.75f, h * 0.35f)
                path.lineTo(w * 0.45f, h * 0.65f)
                path.lineTo(w * 0.35f, h * 0.55f)
                path.close()
                canvas.drawPath(path, paint)
                canvas.drawLine(w * 0.4f, h * 0.6f, w * 0.25f, h * 0.75f, paint)
            }
            Icon.VIDEO -> {
                rect.set(w * 0.25f, h * 0.35f, w * 0.65f, h * 0.65f)
                canvas.drawRoundRect(rect, 2f * d, 2f * d, paint)
                path.reset()
                path.moveTo(w * 0.65f, h * 0.45f)
                path.lineTo(w * 0.8f, h * 0.35f)
                path.lineTo(w * 0.8f, h * 0.65f)
                path.lineTo(w * 0.65f, h * 0.55f)
                canvas.drawPath(path, paint)
            }
            Icon.STADIUM -> {
                path.reset()
                path.moveTo(w * 0.2f, h * 0.6f)
                path.lineTo(w * 0.3f, h * 0.3f)
                path.lineTo(w * 0.7f, h * 0.3f)
                path.lineTo(w * 0.8f, h * 0.6f)
                canvas.drawPath(path, paint)
                canvas.drawOval(w * 0.1f, h * 0.5f, w * 0.9f, h * 0.7f, paint)
            }
            Icon.STATS -> {
                canvas.drawLine(w * 0.3f, h * 0.7f, w * 0.3f, h * 0.5f, paint)
                canvas.drawLine(w * 0.5f, h * 0.7f, w * 0.5f, h * 0.3f, paint)
                canvas.drawLine(w * 0.7f, h * 0.7f, w * 0.7f, h * 0.4f, paint)
                path.reset()
                path.moveTo(w * 0.2f, h * 0.55f)
                path.lineTo(w * 0.4f, h * 0.35f)
                path.lineTo(w * 0.6f, h * 0.45f)
                path.lineTo(w * 0.8f, h * 0.25f)
                canvas.drawPath(path, paint)
            }
            Icon.STORE -> {
                path.reset()
                path.moveTo(w * 0.2f, h * 0.2f)
                path.lineTo(w * 0.3f, h * 0.2f)
                path.lineTo(w * 0.4f, h * 0.6f)
                path.lineTo(w * 0.8f, h * 0.6f)
                path.lineTo(w * 0.9f, h * 0.3f)
                path.lineTo(w * 0.32f, h * 0.3f)
                canvas.drawPath(path, paint)
                canvas.drawCircle(w * 0.45f, h * 0.75f, s * 0.08f, paint)
                canvas.drawCircle(w * 0.75f, h * 0.75f, s * 0.08f, paint)
            }
            Icon.TROPHY -> {
                path.reset()
                path.moveTo(w * 0.3f, h * 0.2f)
                path.lineTo(w * 0.7f, h * 0.2f)
                path.lineTo(w * 0.65f, h * 0.5f)
                path.lineTo(w * 0.5f, h * 0.6f)
                path.lineTo(w * 0.35f, h * 0.5f)
                path.close()
                canvas.drawPath(path, paint)
                canvas.drawLine(w * 0.5f, h * 0.6f, w * 0.5f, h * 0.8f, paint)
                canvas.drawLine(w * 0.3f, h * 0.8f, w * 0.7f, h * 0.8f, paint)
                canvas.drawArc(w * 0.15f, h * 0.2f, w * 0.35f, h * 0.4f, 90f, 180f, false, paint)
                canvas.drawArc(w * 0.65f, h * 0.2f, w * 0.85f, h * 0.4f, 270f, 180f, false, paint)
            }
            Icon.USERS -> {
                canvas.drawCircle(w * 0.5f, h * 0.35f, s * 0.15f, paint)
                path.reset()
                path.moveTo(w * 0.25f, h * 0.8f)
                path.cubicTo(w * 0.25f, h * 0.55f, w * 0.75f, h * 0.55f, w * 0.75f, h * 0.8f)
                canvas.drawPath(path, paint)
                canvas.drawCircle(w * 0.25f, h * 0.4f, s * 0.1f, paint)
                path.reset()
                path.moveTo(w * 0.1f, h * 0.8f)
                path.cubicTo(w * 0.1f, h * 0.6f, w * 0.4f, h * 0.6f, w * 0.4f, h * 0.8f)
                canvas.drawPath(path, paint)
            }
            Icon.BUILDING -> {
                rect.set(w * 0.2f, h * 0.3f, w * 0.5f, h * 0.8f)
                canvas.drawRect(rect, paint)
                rect.set(w * 0.5f, h * 0.4f, w * 0.8f, h * 0.8f)
                canvas.drawRect(rect, paint)
                canvas.drawLine(w * 0.1f, h * 0.8f, w * 0.9f, h * 0.8f, paint)
                canvas.drawLine(w * 0.3f, h * 0.4f, w * 0.4f, h * 0.4f, paint)
                canvas.drawLine(w * 0.3f, h * 0.5f, w * 0.4f, h * 0.5f, paint)
                canvas.drawLine(w * 0.6f, h * 0.5f, w * 0.7f, h * 0.5f, paint)
            }
            Icon.HELP -> {
                canvas.drawCircle(w / 2, h / 2, s * 0.4f, paint)
                path.reset()
                path.moveTo(w * 0.4f, h * 0.35f)
                path.cubicTo(w * 0.4f, h * 0.25f, w * 0.6f, h * 0.25f, w * 0.6f, h * 0.35f)
                path.cubicTo(w * 0.6f, h * 0.45f, w * 0.5f, h * 0.45f, w * 0.5f, h * 0.55f)
                canvas.drawPath(path, paint)
                canvas.drawCircle(w * 0.5f, h * 0.68f, d, paint)
            }
            Icon.SHARE -> {
                canvas.drawCircle(w * 0.25f, h * 0.5f, s * 0.1f, paint)
                canvas.drawCircle(w * 0.75f, h * 0.25f, s * 0.1f, paint)
                canvas.drawCircle(w * 0.75f, h * 0.75f, s * 0.1f, paint)
                canvas.drawLine(w * 0.35f, h * 0.45f, w * 0.65f, h * 0.3f, paint)
                canvas.drawLine(w * 0.35f, h * 0.55f, w * 0.65f, h * 0.7f, paint)
            }
            Icon.STAR, Icon.PRO_STAR -> {
                if (icon == Icon.PRO_STAR) {
                    paint.style = Paint.Style.FILL
                }
                path.reset()
                val cx = w / 2
                val cy = h / 2
                val outR = s * 0.35f
                val inR = s * 0.15f
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) outR else inR
                    val a = Math.PI * i / 5 - Math.PI / 2
                    val x = cx + r * cos(a).toFloat()
                    val y = cy + r * sin(a).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                canvas.drawPath(path, paint)
                if (icon == Icon.PRO_STAR) {
                    paint.style = Paint.Style.STROKE
                }
            }
            Icon.QR -> {
                rect.set(w * 0.2f, h * 0.2f, w * 0.45f, h * 0.45f)
                canvas.drawRect(rect, paint)
                rect.set(w * 0.55f, h * 0.2f, w * 0.8f, h * 0.45f)
                canvas.drawRect(rect, paint)
                rect.set(w * 0.2f, h * 0.55f, w * 0.45f, h * 0.8f)
                canvas.drawRect(rect, paint)
                canvas.drawPoint(w * 0.325f, h * 0.325f, paint)
                canvas.drawPoint(w * 0.675f, h * 0.325f, paint)
                canvas.drawPoint(w * 0.325f, h * 0.675f, paint)
                canvas.drawPoint(w * 0.6f, h * 0.6f, paint)
                canvas.drawPoint(w * 0.7f, h * 0.7f, paint)
                canvas.drawPoint(w * 0.75f, h * 0.55f, paint)
            }
            Icon.MORE -> {
                paint.style = Paint.Style.FILL
                canvas.drawCircle(w * 0.25f, h * 0.5f, s * 0.08f, paint)
                canvas.drawCircle(w * 0.5f, h * 0.5f, s * 0.08f, paint)
                canvas.drawCircle(w * 0.75f, h * 0.5f, s * 0.08f, paint)
                paint.style = Paint.Style.STROKE
            }
            Icon.INFO -> {
                canvas.drawCircle(w / 2, h / 2, s * 0.4f, paint)
                canvas.drawLine(w * 0.5f, h * 0.45f, w * 0.5f, h * 0.7f, paint)
                paint.style = Paint.Style.FILL
                canvas.drawCircle(w * 0.5f, h * 0.3f, s * 0.05f, paint)
                paint.style = Paint.Style.STROKE
            }
            Icon.GLOBE -> {
                canvas.drawCircle(w / 2, h / 2, s * 0.4f, paint)
                canvas.drawOval(w * 0.3f, h * 0.1f, w * 0.7f, h * 0.9f, paint)
                canvas.drawLine(w * 0.1f, h * 0.5f, w * 0.9f, h * 0.5f, paint)
            }
            Icon.SOCIAL_IG -> {
                rect.set(w * 0.2f, h * 0.2f, w * 0.8f, h * 0.8f)
                canvas.drawRoundRect(rect, s * 0.15f, s * 0.15f, paint)
                canvas.drawCircle(w / 2, h / 2, s * 0.15f, paint)
                paint.style = Paint.Style.FILL
                canvas.drawCircle(w * 0.65f, h * 0.35f, s * 0.04f, paint)
                paint.style = Paint.Style.STROKE
            }
            Icon.SOCIAL_FB -> {
                path.reset()
                path.moveTo(w * 0.6f, h * 0.2f)
                path.lineTo(w * 0.5f, h * 0.2f)
                path.arcTo(w * 0.4f, h * 0.2f, w * 0.6f, h * 0.4f, -90f, -90f, false)
                path.lineTo(w * 0.4f, h * 0.8f)
                canvas.drawPath(path, paint)
                canvas.drawLine(w * 0.3f, h * 0.45f, w * 0.6f, h * 0.45f, paint)
            }
            Icon.SOCIAL_X -> {
                canvas.drawLine(w * 0.25f, h * 0.25f, w * 0.75f, h * 0.75f, paint)
                canvas.drawLine(w * 0.25f, h * 0.75f, w * 0.75f, h * 0.25f, paint)
            }
            Icon.SOCIAL_YT -> {
                rect.set(w * 0.15f, h * 0.3f, w * 0.85f, h * 0.7f)
                canvas.drawRoundRect(rect, s * 0.1f, s * 0.1f, paint)
                path.reset()
                path.moveTo(w * 0.45f, h * 0.4f)
                path.lineTo(w * 0.65f, h * 0.5f)
                path.lineTo(w * 0.45f, h * 0.6f)
                path.close()
                paint.style = Paint.Style.FILL
                canvas.drawPath(path, paint)
                paint.style = Paint.Style.STROKE
            }
            Icon.RIGHT_ARROW -> {
                path.reset()
                path.moveTo(w * 0.4f, h * 0.25f)
                path.lineTo(w * 0.65f, h * 0.5f)
                path.lineTo(w * 0.4f, h * 0.75f)
                canvas.drawPath(path, paint)
            }
            Icon.CHECK -> {
                path.reset()
                path.moveTo(w * 0.25f, h * 0.5f)
                path.lineTo(w * 0.45f, h * 0.7f)
                path.lineTo(w * 0.75f, h * 0.3f)
                canvas.drawPath(path, paint)
            }
        }
    }
}
