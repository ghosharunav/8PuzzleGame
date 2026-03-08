package com.example.eightpuzzle

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView

class TileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    // Mode is now set as a separate property, NOT a constructor parameter
    var mode: GameMode = GameMode.MODE1

    var tileNumber: Int = 0
        set(value) {
            field = value
            updateAppearance()
        }

    init {
        gravity = Gravity.CENTER
        textSize = 32f
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        updateAppearance()
    }

    private fun updateAppearance() {
        if (tileNumber == 0) {
            text = ""
            setBackgroundResource(R.drawable.tile_empty_drawable)
        } else {
            text = tileNumber.toString()
            setTextColor(Color.WHITE)
            if (mode == GameMode.MODE1) {
                setBackgroundResource(R.drawable.tile_bg_drawable)
            } else {
                setBackgroundResource(R.drawable.tile_bg_orange_drawable)
            }
        }
    }

    fun animateMove() {
        val scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.88f, 1f)
        val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.88f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 150
            start()
        }
    }
}