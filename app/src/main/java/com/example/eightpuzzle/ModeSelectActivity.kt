package com.example.eightpuzzle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ModeSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mode_select)

        // Mode 1 card clicked
        findViewById<android.view.View>(R.id.cardMode1).setOnClickListener {
            launchGame(GameMode.MODE1)
        }

        // Mode 2 card clicked
        findViewById<android.view.View>(R.id.cardMode2).setOnClickListener {
            launchGame(GameMode.MODE2)
        }
    }

    private fun launchGame(mode: GameMode) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("GAME_MODE", mode.name)
        startActivity(intent)
    }
}