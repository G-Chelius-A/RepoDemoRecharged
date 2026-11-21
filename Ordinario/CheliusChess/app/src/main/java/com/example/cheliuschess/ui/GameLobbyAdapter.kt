package com.example.cheliuschess.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cheliuschess.databinding.ItemGameLobbyBinding
import com.example.cheliuschess.remote.OnlineGameData

class GameLobbyAdapter(
    private var games: List<OnlineGameData> = emptyList(),
    private val onJoinClick: (OnlineGameData) -> Unit
) : RecyclerView.Adapter<GameLobbyAdapter.GameLobbyViewHolder>() {

    fun updateGames(newGames: List<OnlineGameData>) {
        games = newGames
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameLobbyViewHolder {
        val binding = ItemGameLobbyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameLobbyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameLobbyViewHolder, position: Int) {
        val game = games[position]
        holder.bind(game)
        holder.binding.btnJoinGame.setOnClickListener {
            onJoinClick(game)
        }
    }

    override fun getItemCount(): Int = games.size

    class GameLobbyViewHolder(
        val binding: ItemGameLobbyBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: OnlineGameData) {
            val hostId = game.white_player_id?.take(8) ?: "Anon"
            binding.tvLobbyHost.text = "Anfitrión: #$hostId"
            binding.tvLobbyDetails.text = "Tiempo: ${game.time_config_minutes}m + ${game.time_config_increment}s | Esperando Oponente"
        }
    }
}
