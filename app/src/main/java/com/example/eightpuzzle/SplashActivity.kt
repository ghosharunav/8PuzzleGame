package com.example.eightpuzzle

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Replace this with your actual name
        val tvCredit = findViewById<TextView>(R.id.tvCredit)
        tvCredit.text = "Developed by Arunava Ghosh"

        // Animate the logo — fade in + scale up
        val logo = findViewById<ImageView>(R.id.ivLogo)

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 1000
        }
        val scaleUp = ScaleAnimation(
            0.5f, 1f,
            0.5f, 1f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 1000
        }
        val animSet = AnimationSet(true).apply {
            addAnimation(fadeIn)
            addAnimation(scaleUp)
            fillAfter = true
        }
        logo.startAnimation(animSet)

        // After 2.5 seconds go to Mode Select screen
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, ModeSelectActivity::class.java)
            startActivity(intent)
            finish() // finish so pressing back doesn't return to splash
        }, 2500)
    }
}