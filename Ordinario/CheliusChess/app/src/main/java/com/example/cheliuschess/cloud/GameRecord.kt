package com.example.cheliuschess.cloud

data class GameRecord(
    val gameId: String,
    val dateTimestamp: Long,
    val timeConfigMinutes: Int,
    val timeConfigSeconds: Int,
    val timeConfigIncrement: Int,
    val fenHistory: List<String>,
    val movesSan: List<String>,
    val score: String,
    val endReason: String,
    val totalMoves: Int
)
