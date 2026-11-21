package com.example.cheliuschess

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cheliuschess.databinding.ActivityLobbyBinding
import com.example.cheliuschess.remote.OnlineGameData
import com.example.cheliuschess.remote.OnlineGameRepository
import com.example.cheliuschess.ui.GameLobbyAdapter
import kotlinx.coroutines.launch

class LobbyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLobbyBinding
    private lateinit var repository: OnlineGameRepository
    private lateinit var adapter: GameLobbyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.lobbyRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = OnlineGameRepository()

        val userId = repository.getCurrentUserId()
        if (userId == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        binding.tvUserId.text = "ID Usuario: ${userId.take(8)}"

        adapter = GameLobbyAdapter { game ->
            joinSelectedGame(game)
        }

        binding.rvLobbyGames.layoutManager = LinearLayoutManager(this)
        binding.rvLobbyGames.adapter = adapter

        binding.btnCreateGame.setOnClickListener {
            createNewGameRoom()
        }

        binding.btnRefreshLobby.setOnClickListener {
            loadLobbyGames()
        }

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                repository.signOut()
                startActivity(Intent(this@LobbyActivity, AuthActivity::class.java))
                finish()
            }
        }

        binding.btnBackMenu.setOnClickListener {
            finish()
        }

        loadLobbyGames()
    }

    private fun loadLobbyGames() {
        lifecycleScope.launch {
            val result = repository.getWaitingGames()
            result.onSuccess { games ->
                if (games.isEmpty()) {
                    binding.tvEmptyLobby.visibility = View.VISIBLE
                    binding.rvLobbyGames.visibility = View.GONE
                } else {
                    binding.tvEmptyLobby.visibility = View.GONE
                    binding.rvLobbyGames.visibility = View.VISIBLE
                    adapter.updateGames(games)
                }
            }.onFailure { error ->
                Toast.makeText(this@LobbyActivity, "Error cargando salas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createNewGameRoom() {
        lifecycleScope.launch {
            val result = repository.createGame(timeMinutes = 10, timeIncrement = 0)
            result.onSuccess { createdGame ->
                Toast.makeText(this@LobbyActivity, "Sala creada. Esperando oponente...", Toast.LENGTH_SHORT).show()
                openOnlineGame(createdGame.id!!, isWhitePlayer = true)
            }.onFailure { error ->
                Toast.makeText(this@LobbyActivity, "Error creando sala: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun joinSelectedGame(game: OnlineGameData) {
        if (game.id == null) return
        lifecycleScope.launch {
            val result = repository.joinGame(game.id)
            result.onSuccess { joinedGame ->
                Toast.makeText(this@LobbyActivity, "¡Te has unido a la partida!", Toast.LENGTH_SHORT).show()
                openOnlineGame(joinedGame.id!!, isWhitePlayer = false)
            }.onFailure { error ->
                Toast.makeText(this@LobbyActivity, "Error al unirte a la partida: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openOnlineGame(gameId: String, isWhitePlayer: Boolean) {
        val intent = Intent(this, OnlineGameActivity::class.java).apply {
            putExtra(EXTRA_GAME_ID, gameId)
            putExtra(EXTRA_IS_WHITE, isWhitePlayer)
        }
        startActivity(intent)
    }

    companion object {
        const val EXTRA_GAME_ID = "extra_game_id"
        const val EXTRA_IS_WHITE = "extra_is_white"
    }
}
