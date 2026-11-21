package com.example.cheliuschess.engine

enum class MoveType {
    NORMAL,
    EN_PASSANT,
    CASTLING_KINGSIDE,
    CASTLING_QUEENSIDE,
    PROMOTION
}

data class Move(
    val from: Position,
    val to: Position,
    val piece: ChessPiece,
    val capturedPiece: ChessPiece? = null,
    val moveType: MoveType = MoveType.NORMAL,
    val promotionPieceType: PieceType? = null
)
