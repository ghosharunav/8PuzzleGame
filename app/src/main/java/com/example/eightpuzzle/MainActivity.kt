package com.example.eightpuzzle

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.eightpuzzle.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val puzzleManager = PuzzleManager()
    private val gameTimer = GameTimer()
    private val tileViews = arrayOfNulls<TileView>(9)
    private var gameActive = false
    private var boardReady = false
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Read which mode was selected
        val modeName = intent.getStringExtra("GAME_MODE") ?: GameMode.MODE1.name
        puzzleManager.mode = GameMode.valueOf(modeName)

        updateModeUI()
        setupGrid()
        setupButtons()
    }

    // ─── Mode UI ─────────────────────────────────────────────────────────────

    private fun updateModeUI() {
        if (puzzleManager.mode == GameMode.MODE1) {
            binding.tvModeLabel.text = "MODE 1"
            binding.tvModeDesc.text = "Classic"
            binding.tvGoalPreview.text = "1 2 3 | 4 5 6 | 7 8 _"
            binding.headerBar.setBackgroundColor(0xFF1A2E45.toInt())
        } else {
            binding.tvModeLabel.text = "MODE 2"
            binding.tvModeDesc.text = "Reverse"
            binding.tvGoalPreview.text = "_ 8 7 | 6 5 4 | 3 2 1"
            binding.headerBar.setBackgroundColor(0xFF2D1F0E.toInt())
        }
    }

    // ─── Grid Setup ──────────────────────────────────────────────────────────

    private fun setupGrid() {
        val grid = binding.puzzleGrid
        grid.post {
            val tileSize = (grid.width - grid.paddingStart - grid.paddingEnd) / 3
            for (i in 0 until 9) {
                val tile = TileView(this).apply {
                    mode = puzzleManager.mode
                }
                val params = GridLayout.LayoutParams().apply {
                    width = tileSize - 8
                    height = tileSize - 8
                    setMargins(4, 4, 4, 4)
                    rowSpec = GridLayout.spec(i / 3)
                    columnSpec = GridLayout.spec(i % 3)
                }
                tile.layoutParams = params
                tile.setOnClickListener { onTileTapped(i) }
                tileViews[i] = tile
                grid.addView(tile)
            }
            loadNewBoard()
        }
    }

    // ─── Button Setup ────────────────────────────────────────────────────────

    private fun setupButtons() {
        binding.btnStart.setOnClickListener {
            if (boardReady && !gameActive) startGame()
        }
        binding.btnNewGame.setOnClickListener {
            loadNewBoard()
        }
        binding.btnBack.setOnClickListener {
            gameTimer.reset()
            val intent = Intent(this, ModeSelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    // ─── Load Board ───────────────────────────────────────────────────────────

    private fun loadNewBoard() {
        gameActive = false
        gameTimer.reset()
        puzzleManager.shuffle()
        boardReady = true

        binding.tvMoves.text = "0"
        binding.tvTime.text = "00:00"
        binding.tvOptimal.text = "—"
        binding.tvWinMessage.visibility = View.GONE
        binding.tvStatus.text = "Press START to begin!"
        binding.tvStatus.setTextColor(0xFF5B8DB8.toInt())

        binding.btnStart.isEnabled = true
        binding.btnStart.alpha = 1.0f

        renderTiles()
    }

    // ─── Start Game ───────────────────────────────────────────────────────────

    private fun startGame() {
        gameActive = true
        boardReady = false

        binding.btnStart.isEnabled = false
        binding.btnStart.alpha = 0.5f
        binding.tvStatus.text = "Solve the puzzle! 🧩"
        binding.tvStatus.setTextColor(0xFF4CAF50.toInt())

        // Calculate initial optimal moves in background
        calculateOptimalMoves()

        gameTimer.start { seconds ->
            binding.tvTime.text = GameTimer.formatTime(seconds)
        }
    }

    // ─── Tile Tap ─────────────────────────────────────────────────────────────

    private fun onTileTapped(index: Int) {
        if (!gameActive) return
        val moved = puzzleManager.moveTile(index)
        if (moved) {
            tileViews[index]?.animateMove()
            renderTiles()
            binding.tvMoves.text = puzzleManager.moveCount.toString()
            binding.tvOptimal.text = "…"

            // Recalculate optimal after each move (in background thread)
            calculateOptimalMoves()

            if (puzzleManager.isSolved()) {
                gameActive = false
                gameTimer.stop()
                binding.tvOptimal.text = "0"
                showWinDialog()
            }
        }
    }

    // ─── Optimal Moves Calculation (background thread) ───────────────────────

    private fun calculateOptimalMoves() {
        val snapshot = puzzleManager.tiles.copyOf()
        val goal = puzzleManager.goalState.copyOf()
        Thread {
            val optimal = PuzzleManager().apply {
                mode = puzzleManager.mode
            }.run {
                aStarPublic(snapshot, goal)
            }
            mainHandler.post {
                if (gameActive || puzzleManager.isSolved()) {
                    binding.tvOptimal.text = if (optimal == null) "?" else optimal.toString()
                }
            }
        }.start()
    }

    // ─── Render ───────────────────────────────────────────────────────────────

    private fun renderTiles() {
        for (i in 0 until 9) {
            tileViews[i]?.tileNumber = puzzleManager.tiles[i]
        }
    }

    // ─── Win Dialog ───────────────────────────────────────────────────────────

    private fun showWinDialog() {
        binding.tvWinMessage.visibility = View.VISIBLE

        val dialog = Dialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_win, null)

        view.findViewById<TextView>(R.id.tvDialogMoves).text =
            "Moves: ${puzzleManager.moveCount}"
        view.findViewById<TextView>(R.id.tvDialogTime).text =
            "Time: ${GameTimer.formatTime(gameTimer.secondsElapsed)}"

        view.findViewById<Button>(R.id.btnPlayAgain).setOnClickListener {
            dialog.dismiss()
            loadNewBoard()
        }

        dialog.setContentView(view)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        dialog.show()
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onPause() {
        super.onPause()
        if (gameActive) gameTimer.stop()
    }

    override fun onResume() {
        super.onResume()
        if (gameActive) {
            gameTimer.start { seconds ->
                binding.tvTime.text = GameTimer.formatTime(seconds)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gameTimer.reset()
    }
}