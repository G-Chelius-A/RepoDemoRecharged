package com.example.cheliuschess.engine

data class GameMoveRecord(
    val move: Move,
    val sanNotation: String,
    val fenBefore: String,
    val fenAfter: String,
    val whiteMillis: Long,
    val blackMillis: Long,
    val timestamp: Long = System.currentTimeMillis()
)
