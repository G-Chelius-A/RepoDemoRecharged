package com.example.cheliuschess.engine

enum class PieceColor {
    WHITE,
    BLACK;

    fun opposite(): PieceColor = if (this == WHITE) BLACK else WHITE
}
