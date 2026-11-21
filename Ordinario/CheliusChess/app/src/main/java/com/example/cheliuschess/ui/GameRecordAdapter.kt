package com.example.cheliuschess.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cheliuschess.cloud.GameRecord
import com.example.cheliuschess.databinding.ItemGameRecordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameRecordAdapter(
    private var records: List<GameRecord> = emptyList()
) : RecyclerView.Adapter<GameRecordAdapter.GameRecordViewHolder>() {

    fun updateRecords(newRecords: List<GameRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameRecordViewHolder {
        val binding = ItemGameRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameRecordViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    class GameRecordViewHolder(
        private val binding: ItemGameRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: GameRecord) {
            val shortId = if (record.gameId.length > 8) record.gameId.take(8) else record.gameId
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(record.dateTimestamp))

            binding.tvGameId.text = "Partida #$shortId ($dateStr)"
            binding.tvGameScore.text = record.score
            binding.tvGameDetails.text = "Modalidad: ${record.timeConfigMinutes}m + ${record.timeConfigIncrement}s | Jugadas: ${record.totalMoves} | Motivo: ${record.endReason}"

            val finalFen = record.fenHistory.lastOrNull() ?: "Sin datos FEN"
            val formattedSan = formatSanSheet(record.movesSan)
            binding.tvGameFenPreview.text = "SAN: $formattedSan\nFEN Final: $finalFen"
        }

        private fun formatSanSheet(moves: List<String>): String {
            if (moves.isEmpty()) return "-"
            val sb = StringBuilder()
            for (i in moves.indices) {
                if (i % 2 == 0) {
                    val moveNum = (i / 2) + 1
                    if (i > 0) sb.append("  ")
                    sb.append("$moveNum. ").append(moves[i])
                } else {
                    sb.append(" ").append(moves[i])
                }
            }
            return sb.toString()
        }
    }
}
