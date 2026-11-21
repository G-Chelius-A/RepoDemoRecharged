package com.example.cheliuschess.engine

data class ChessBoardState(
    val grid: Array<Array<ChessPiece?>> = Array(8) { Array(8) { null } },
    val turn: PieceColor = PieceColor.WHITE,
    val enPassantTarget: Position? = null,
    val capturedPieces: List<ChessPiece> = emptyList(),
    val halfMoveClock: Int = 0,
    val fullMoveNumber: Int = 1
) {
    fun getPiece(pos: Position): ChessPiece? {
        if (!pos.isValid()) return null
        return grid[pos.row][pos.col]
    }

    fun copyBoard(): Array<Array<ChessPiece?>> {
        return Array(8) { r ->
            Array(8) { c ->
                grid[r][c]
            }
        }
    }

    fun toFen(): String {
        val sb = StringBuilder()
        for (r in 7 downTo 0) {
            var emptyCount = 0
            for (c in 0..7) {
                val piece = grid[r][c]
                if (piece == null) {
                    emptyCount++
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount)
                        emptyCount = 0
                    }
                    val char = when (piece.type) {
                        PieceType.PAWN -> 'p'
                        PieceType.KNIGHT -> 'n'
                        PieceType.BISHOP -> 'b'
                        PieceType.ROOK -> 'r'
                        PieceType.QUEEN -> 'q'
                        PieceType.KING -> 'k'
                    }
                    sb.append(if (piece.color == PieceColor.WHITE) char.uppercaseChar() else char)
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount)
            }
            if (r > 0) sb.append('/')
        }

        val turnChar = if (turn == PieceColor.WHITE) "w" else "b"
        val epString = enPassantTarget?.toAlgebraic() ?: "-"

        return "$sb $turnChar - $epString $halfMoveClock $fullMoveNumber"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChessBoardState

        for (r in 0..7) {
            for (c in 0..7) {
                if (grid[r][c] != other.grid[r][c]) return false
            }
        }
        if (turn != other.turn) return false
        if (enPassantTarget != other.enPassantTarget) return false
        if (halfMoveClock != other.halfMoveClock) return false
        if (fullMoveNumber != other.fullMoveNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = grid.contentDeepHashCode()
        result = 31 * result + turn.hashCode()
        result = 31 * result + (enPassantTarget?.hashCode() ?: 0)
        result = 31 * result + halfMoveClock
        result = 31 * result + fullMoveNumber
        return result
    }

    companion object {
        fun initial(): ChessBoardState {
            val grid = Array(8) { Array<ChessPiece?>(8) { null } }

            // Peones
            for (c in 0..7) {
                grid[1][c] = ChessPiece(PieceType.PAWN, PieceColor.WHITE)
                grid[6][c] = ChessPiece(PieceType.PAWN, PieceColor.BLACK)
            }

            // Piezas Blancas (fila 0)
            grid[0][0] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)
            grid[0][1] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
            grid[0][2] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
            grid[0][3] = ChessPiece(PieceType.QUEEN, PieceColor.WHITE)
            grid[0][4] = ChessPiece(PieceType.KING, PieceColor.WHITE)
            grid[0][5] = ChessPiece(PieceType.BISHOP, PieceColor.WHITE)
            grid[0][6] = ChessPiece(PieceType.KNIGHT, PieceColor.WHITE)
            grid[0][7] = ChessPiece(PieceType.ROOK, PieceColor.WHITE)

            // Piezas Negras (fila 7)
            grid[7][0] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)
            grid[7][1] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
            grid[7][2] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
            grid[7][3] = ChessPiece(PieceType.QUEEN, PieceColor.BLACK)
            grid[7][4] = ChessPiece(PieceType.KING, PieceColor.BLACK)
            grid[7][5] = ChessPiece(PieceType.BISHOP, PieceColor.BLACK)
            grid[7][6] = ChessPiece(PieceType.KNIGHT, PieceColor.BLACK)
            grid[7][7] = ChessPiece(PieceType.ROOK, PieceColor.BLACK)

            return ChessBoardState(grid = grid, turn = PieceColor.WHITE)
        }
    }
}
