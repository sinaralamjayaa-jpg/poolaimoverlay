package com.poolaim.overlay

import android.app.Service
import android.content.Intent
import android.graphics.*
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.graphics.PixelFormat
import kotlin.math.cos
import kotlin.math.sin

class AimOverlayService : Service() {
    private lateinit var wm: WindowManager
    private var drawing: AimView? = null
    private var panel: LinearLayout? = null
    private var angle = -28f
    private var length = 0.30f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        drawing = AimView()
        val drawParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        wm.addView(drawing, drawParams)

        panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 8)
            setBackgroundColor(Color.argb(210, 20, 25, 30))
        }
        fun button(label: String, action: () -> Unit) {
            val b = Button(this).apply { text = label; textSize = 10f }
            panel!!.addView(b, LinearLayout.LayoutParams(150, 44))
            b.setOnClickListener { action(); drawing?.invalidate() }
        }
        button("Sudut -") { angle -= 2f }
        button("Sudut +") { angle += 2f }
        button("Garis pendek") { length = (length - 0.03f).coerceAtLeast(0.08f) }
        button("Garis panjang") { length = (length + 0.03f).coerceAtMost(0.65f) }
        button("Tutup X") { stopSelf() }

        val panelParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.END; x = 8; y = 140 }
        wm.addView(panel, panelParams)
    }

    override fun onDestroy() {
        drawing?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        panel?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        drawing = null; panel = null
        super.onDestroy()
    }

    inner class AimView : View(this@AimOverlayService) {
        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(40, 235, 245); strokeWidth = 4f; style = Paint.Style.STROKE
        }
        private val ghostPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(190, 255, 210, 55); strokeWidth = 3f; style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(12f, 9f), 0f)
        }
        private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat(); val h = height.toFloat()
            // Default cue-ball marker and adjustable guide; positioned approximately over the table.
            val sx = w * 0.18f
            val sy = h * 0.59f
            val radians = Math.toRadians(angle.toDouble())
            val ex = sx + cos(radians).toFloat() * w * length
            val ey = sy + sin(radians).toFloat() * w * length
            canvas.drawLine(sx, sy, ex, ey, linePaint)
            canvas.drawCircle(sx, sy, 8f, dotPaint)
            canvas.drawLine(ex, ey, ex + cos(radians).toFloat() * w * 0.13f,
                ey + sin(radians).toFloat() * w * 0.13f, ghostPaint)
            // Small crosshair at the projected contact point.
            canvas.drawCircle(ex, ey, 10f, ghostPaint)
        }
    }
}
