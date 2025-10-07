package com.utadeo.nebuly.screens

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.utadeo.nebuly.MainActivity
import com.utadeo.nebuly.R

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val backgroundGif = findViewById<ImageView>(R.id.backgroundGif)
        Glide.with(this)
            .asGif()
            .load(R.drawable.fondo_bienvenida)
            .into(backgroundGif)


        val mainLayout = findViewById<RelativeLayout>(R.id.main)
        mainLayout.setOnClickListener {
            abrirMainActivity()
        }

    }

    private fun abrirMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
