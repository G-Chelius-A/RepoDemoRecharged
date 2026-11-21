package com.example.cheliuschess.engine

data class ChessPiece(
    val type: PieceType,
    val color: PieceColor,
    val hasMoved: Boolean = false
) {
    fun copyWithMoved(): ChessPiece = copy(hasMoved = true)
}
