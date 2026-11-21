package com.example.cheliuschess

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cheliuschess.cloud.CloudGameRepository
import com.example.cheliuschess.databinding.ActivityCloudHistoryBinding
import com.example.cheliuschess.ui.GameRecordAdapter

class CloudHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCloudHistoryBinding
    private lateinit var repository: CloudGameRepository
    private lateinit var adapter: GameRecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCloudHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.cloudHistoryRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = CloudGameRepository(this)
        adapter = GameRecordAdapter()

        binding.rvGameRecords.layoutManager = LinearLayoutManager(this)
        binding.rvGameRecords.adapter = adapter

        binding.btnBackMenu.setOnClickListener {
            finish()
        }

        binding.btnClearHistory.setOnClickListener {
            showClearHistoryConfirmationDialog()
        }

        loadHistory()
    }

    private fun loadHistory() {
        val games = repository.getSavedGames()
        if (games.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvGameRecords.visibility = View.GONE
            binding.tvHistorySubtitle.text = "No hay partidas guardadas en el registro de la nube."
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvGameRecords.visibility = View.VISIBLE
            binding.tvHistorySubtitle.text = "${games.size} partidas guardadas localmente y sincronizadas en la nube."
            adapter.updateRecords(games)
        }
    }

    private fun showClearHistoryConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Borrar Historial")
            .setMessage("¿Deseas eliminar todo el registro de partidas guardadas en la nube?")
            .setPositiveButton("Sí, Borrar") { _, _ ->
                repository.clearHistory()
                loadHistory()
                Toast.makeText(this, "Historial borrado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
