package com.example.cheliuschess

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cheliuschess.databinding.ActivityTimeConfigBinding

class TimeConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeConfigBinding

    private var isVsComputer = false
    private var selectedSide = "WHITE"
    private var selectedDifficulty = "MEDIUM"

    private var selectedMin = 10
    private var selectedSec = 0
    private var selectedInc = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTimeConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.timeConfigRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        isVsComputer = intent.getBooleanExtra(EXTRA_VS_COMPUTER, false)

        if (isVsComputer) {
            binding.tvTitleTimeConfig.text = "AJEDREZ VS COMPUTADORA"
            binding.layoutComputerOptions.visibility = View.VISIBLE
            setupComputerOptions()
        } else {
            binding.tvTitleTimeConfig.text = "MODALIDAD DE TIEMPO (2 JUGADORES)"
            binding.layoutComputerOptions.visibility = View.GONE
        }

        setupPresets()

        binding.btnBackMenu.setOnClickListener {
            finish()
        }

        binding.btnStartGame.setOnClickListener {
            val min = binding.etMinutes.text.toString().toIntOrNull() ?: selectedMin
            val sec = binding.etSeconds.text.toString().toIntOrNull() ?: selectedSec
            val inc = binding.etIncrement.text.toString().toIntOrNull() ?: selectedInc

            if (min <= 0 && sec <= 0) {
                Toast.makeText(this, "El tiempo de partida debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, GameActivity::class.java).apply {
                putExtra(EXTRA_MINUTES, min)
                putExtra(EXTRA_SECONDS, sec)
                putExtra(EXTRA_INCREMENT, inc)
                putExtra(EXTRA_VS_COMPUTER, isVsComputer)
                putExtra(EXTRA_PLAYER_SIDE, selectedSide)
                putExtra(EXTRA_AI_DIFFICULTY, selectedDifficulty)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupComputerOptions() {
        binding.btnSideWhite.setOnClickListener {
            selectedSide = "WHITE"
            highlightGroupButton(binding.btnSideWhite, listOf(binding.btnSideWhite, binding.btnSideBlack, binding.btnSideRandom))
        }

        binding.btnSideBlack.setOnClickListener {
            selectedSide = "BLACK"
            highlightGroupButton(binding.btnSideBlack, listOf(binding.btnSideWhite, binding.btnSideBlack, binding.btnSideRandom))
        }

        binding.btnSideRandom.setOnClickListener {
            selectedSide = "RANDOM"
            highlightGroupButton(binding.btnSideRandom, listOf(binding.btnSideWhite, binding.btnSideBlack, binding.btnSideRandom))
        }

        binding.btnDiffEasy.setOnClickListener {
            selectedDifficulty = "EASY"
            highlightGroupButton(binding.btnDiffEasy, listOf(binding.btnDiffEasy, binding.btnDiffMedium, binding.btnDiffHard))
        }

        binding.btnDiffMedium.setOnClickListener {
            selectedDifficulty = "MEDIUM"
            highlightGroupButton(binding.btnDiffMedium, listOf(binding.btnDiffEasy, binding.btnDiffMedium, binding.btnDiffHard))
        }

        binding.btnDiffHard.setOnClickListener {
            selectedDifficulty = "HARD"
            highlightGroupButton(binding.btnDiffHard, listOf(binding.btnDiffEasy, binding.btnDiffMedium, binding.btnDiffHard))
        }
    }

    private fun setupPresets() {
        binding.btnPreset32.setOnClickListener {
            applyPreset(3, 0, 2)
            highlightGroupButton(binding.btnPreset32, listOf(binding.btnPreset32, binding.btnPreset50, binding.btnPreset105, binding.btnPreset1510))
        }

        binding.btnPreset50.setOnClickListener {
            applyPreset(5, 0, 0)
            highlightGroupButton(binding.btnPreset50, listOf(binding.btnPreset32, binding.btnPreset50, binding.btnPreset105, binding.btnPreset1510))
        }

        binding.btnPreset105.setOnClickListener {
            applyPreset(10, 0, 5)
            highlightGroupButton(binding.btnPreset105, listOf(binding.btnPreset32, binding.btnPreset50, binding.btnPreset105, binding.btnPreset1510))
        }

        binding.btnPreset1510.setOnClickListener {
            applyPreset(15, 0, 10)
            highlightGroupButton(binding.btnPreset1510, listOf(binding.btnPreset32, binding.btnPreset50, binding.btnPreset105, binding.btnPreset1510))
        }
    }

    private fun applyPreset(min: Int, sec: Int, inc: Int) {
        selectedMin = min
        selectedSec = sec
        selectedInc = inc
        binding.etMinutes.setText(min.toString())
        binding.etSeconds.setText(sec.toString())
        binding.etIncrement.setText(inc.toString())
    }

    private fun highlightGroupButton(
        activeButton: com.google.android.material.button.MaterialButton,
        group: List<com.google.android.material.button.MaterialButton>
    ) {
        for (btn in group) {
            if (btn == activeButton) {
                btn.strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#007AFF"))
            } else {
                btn.strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#3A3A3C"))
            }
        }
    }

    companion object {
        const val EXTRA_MINUTES = "extra_minutes"
        const val EXTRA_SECONDS = "extra_seconds"
        const val EXTRA_INCREMENT = "extra_increment"
        const val EXTRA_VS_COMPUTER = "extra_vs_computer"
        const val EXTRA_PLAYER_SIDE = "extra_player_side"
        const val EXTRA_AI_DIFFICULTY = "extra_ai_difficulty"
    }
}
