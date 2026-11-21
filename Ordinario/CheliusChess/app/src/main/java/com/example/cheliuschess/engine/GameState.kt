package com.example.cheliuschess.engine

enum class GameState {
    ACTIVE,
    CHECK,
    CHECKMATE_WHITE_WIN,
    CHECKMATE_BLACK_WIN,
    STALEMATE,
    TIMEOUT_WHITE_WIN,
    TIMEOUT_BLACK_WIN,
    DRAW_MATERIAL,
    DRAW_AGREEMENT,
    RESIGNATION_WHITE_WIN,
    RESIGNATION_BLACK_WIN;

    fun getScore(): ChessScore {
        return when (this) {
            ACTIVE, CHECK -> ChessScore.IN_PROGRESS
            CHECKMATE_WHITE_WIN, TIMEOUT_BLACK_WIN, RESIGNATION_WHITE_WIN -> ChessScore.WHITE_WINS
            CHECKMATE_BLACK_WIN, TIMEOUT_WHITE_WIN, RESIGNATION_BLACK_WIN -> ChessScore.BLACK_WINS
            STALEMATE, DRAW_MATERIAL, DRAW_AGREEMENT -> ChessScore.DRAW
        }
    }

    fun isGameOver(): Boolean {
        return this != ACTIVE && this != CHECK
    }
}

enum class ChessScore(val displayText: String) {
    IN_PROGRESS("*"),
    WHITE_WINS("1 - 0"),
    BLACK_WINS("0 - 1"),
    DRAW("1/2 - 1/2")
}
