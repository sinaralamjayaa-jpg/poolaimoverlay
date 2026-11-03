package com.poolaim.overlay

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import android.graphics.Color

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
            setBackgroundColor(Color.rgb(18, 27, 38))
        }
        val title = TextView(this).apply {
            text = "POOL AIM OVERLAY\n\nGaris bidik manual untuk latihan 8 Ball Pool"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        val start = Button(this).apply { text = "AKTIFKAN GARIS BIDIK" }
        val stop = Button(this).apply { text = "MATIKAN GARIS BIDIK" }
        val note = TextView(this).apply {
            text = "\nIzinkan tampil di atas aplikasi lain saat diminta. Overlay tidak membaca posisi bola otomatis."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }
        layout.addView(title)
        layout.addView(start)
        layout.addView(stop)
        layout.addView(note)
        setContentView(layout)

        start.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else {
                startService(Intent(this, AimOverlayService::class.java))
                finish()
            }
        }
        stop.setOnClickListener {
            stopService(Intent(this, AimOverlayService::class.java))
        }
    }
}
