package com.example.cheliuschess.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

enum class AiDifficulty(val displayName: String) {
    EASY("Fácil"),
    MEDIUM("Medio"),
    HARD("Difícil")
}

object ChessAiEngine {

    suspend fun getBestMove(
        boardState: ChessBoardState,
        difficulty: AiDifficulty
    ): Move? = withContext(Dispatchers.Default) {
        val color = boardState.turn
        val legalMoves = ChessRulesEngine.getAllLegalMoves(boardState, color)

        if (legalMoves.isEmpty()) return@withContext null

        when (difficulty) {
            AiDifficulty.EASY -> getEasyMove(boardState, legalMoves, color)
            AiDifficulty.MEDIUM -> getMinimaxMove(boardState, legalMoves, color, maxDepth = 2)
            AiDifficulty.HARD -> getMinimaxMove(boardState, legalMoves, color, maxDepth = 3)
        }
    }

    private fun getEasyMove(
        boardState: ChessBoardState,
        legalMoves: List<Move>,
        color: PieceColor
    ): Move {
        // 30% probabilidad de movimiento totalmente aleatorio (simula principiante)
        if (Random.nextFloat() < 0.3f) {
            return legalMoves.random()
        }
        return getMinimaxMove(boardState, legalMoves, color, maxDepth = 1)
    }

    private fun getMinimaxMove(
        boardState: ChessBoardState,
        legalMoves: List<Move>,
        color: PieceColor,
        maxDepth: Int
    ): Move {
        var bestMove: Move = legalMoves.random()
        var bestScore = if (color == PieceColor.WHITE) Int.MIN_VALUE else Int.MAX_VALUE

        val orderedMoves = orderMoves(boardState, legalMoves)

        for (move in orderedMoves) {
            val nextState = ChessRulesEngine.applyMove(boardState, move)
            val score = minimax(
                state = nextState,
                depth = maxDepth - 1,
                alpha = Int.MIN_VALUE,
                beta = Int.MAX_VALUE,
                isMaximizing = (color == PieceColor.BLACK) // Si le toca al oponente
            )

            if (color == PieceColor.WHITE) {
                if (score > bestScore) {
                    bestScore = score
                    bestMove = move
                }
            } else {
                if (score < bestScore) {
                    bestScore = score
                    bestMove = move
                }
            }
        }
        return bestMove
    }

    private fun minimax(
        state: ChessBoardState,
        depth: Int,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean
    ): Int {
        val gameState = ChessRulesEngine.evaluateGameState(state)
        if (gameState == GameState.CHECKMATE_WHITE_WIN) return 100000 + depth
        if (gameState == GameState.CHECKMATE_BLACK_WIN) return -100000 - depth
        if (gameState == GameState.STALEMATE || gameState == GameState.DRAW_MATERIAL) return 0

        if (depth == 0) {
            return evaluateBoard(state)
        }

        var currentAlpha = alpha
        var currentBeta = beta

        val currentColor = if (isMaximizing) PieceColor.WHITE else PieceColor.BLACK
        val moves = ChessRulesEngine.getAllLegalMoves(state, currentColor)

        if (moves.isEmpty()) {
            return evaluateBoard(state)
        }

        val orderedMoves = orderMoves(state, moves)

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (move in orderedMoves) {
                val nextState = ChessRulesEngine.applyMove(state, move)
                val eval = minimax(nextState, depth - 1, currentAlpha, currentBeta, false)
                maxEval = Math.max(maxEval, eval)
                currentAlpha = Math.max(currentAlpha, eval)
                if (currentBeta <= currentAlpha) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in orderedMoves) {
                val nextState = ChessRulesEngine.applyMove(state, move)
                val eval = minimax(nextState, depth - 1, currentAlpha, currentBeta, true)
                minEval = Math.min(minEval, eval)
                currentBeta = Math.min(currentBeta, eval)
                if (currentBeta <= currentAlpha) break
            }
            return minEval
        }
    }

    private fun orderMoves(state: ChessBoardState, moves: List<Move>): List<Move> {
        return moves.sortedByDescending { move ->
            var priority = 0
            if (move.capturedPiece != null) {
                priority += getPieceValue(move.capturedPiece.type) * 10 - getPieceValue(move.piece.type)
            }
            if (move.moveType == MoveType.PROMOTION) {
                priority += 800
            }
            priority
        }
    }

    private fun evaluateBoard(state: ChessBoardState): Int {
        var totalScore = 0

        for (r in 0..7) {
            for (c in 0..7) {
                val piece = state.grid[r][c] ?: continue
                val pieceVal = getPieceValue(piece.type)
                val posVal = getPieceSquareScore(piece, r, c)
                val score = pieceVal + posVal

                if (piece.color == PieceColor.WHITE) {
                    totalScore += score
                } else {
                    totalScore -= score
                }
            }
        }
        return totalScore
    }

    private fun getPieceValue(type: PieceType): Int {
        return when (type) {
            PieceType.PAWN -> 100
            PieceType.KNIGHT -> 320
            PieceType.BISHOP -> 330
            PieceType.ROOK -> 500
            PieceType.QUEEN -> 900
            PieceType.KING -> 20000
        }
    }

    private fun getPieceSquareScore(piece: ChessPiece, row: Int, col: Int): Int {
        val r = if (piece.color == PieceColor.WHITE) row else 7 - row
        val c = col

        return when (piece.type) {
            PieceType.PAWN -> PAWN_PST[r][c]
            PieceType.KNIGHT -> KNIGHT_PST[r][c]
            PieceType.BISHOP -> BISHOP_PST[r][c]
            PieceType.ROOK -> ROOK_PST[r][c]
            PieceType.QUEEN -> QUEEN_PST[r][c]
            PieceType.KING -> KING_PST[r][c]
        }
    }

    // Piece-Square Tables (8x8) para premiar casillas estratégicas
    private val PAWN_PST = arrayOf(
        intArrayOf(0,  0,  0,  0,  0,  0,  0,  0),
        intArrayOf(50, 50, 50, 50, 50, 50, 50, 50),
        intArrayOf(10, 10, 20, 30, 30, 20, 10, 10),
        intArrayOf(5,  5, 10, 25, 25, 10,  5,  5),
        intArrayOf(0,  0,  0, 20, 20,  0,  0,  0),
        intArrayOf(5, -5,-10,  0,  0,-10, -5,  5),
        intArrayOf(5, 10, 10,-20,-20, 10, 10,  5),
        intArrayOf(0,  0,  0,  0,  0,  0,  0,  0)
    )

    private val KNIGHT_PST = arrayOf(
        intArrayOf(-50,-40,-30,-30,-30,-30,-40,-50),
        intArrayOf(-40,-20,  0,  0,  0,  0,-20,-40),
        intArrayOf(-30,  0, 10, 15, 15, 10,  0,-30),
        intArrayOf(-30,  5, 15, 20, 20, 15,  5,-30),
        intArrayOf(-30,  0, 15, 20, 20, 15,  0,-30),
        intArrayOf(-30,  5, 10, 15, 15, 10,  5,-30),
        intArrayOf(-40,-20,  0,  5,  5,  0,-20,-40),
        intArrayOf(-50,-40,-30,-30,-30,-30,-40,-50)
    )

    private val BISHOP_PST = arrayOf(
        intArrayOf(-20,-10,-10,-10,-10,-10,-10,-20),
        intArrayOf(-10,  0,  0,  0,  0,  0,  0,-10),
        intArrayOf(-10,  0,  5, 10, 10,  5,  0,-10),
        intArrayOf(-10,  5,  5, 10, 10,  5,  5,-10),
        intArrayOf(-10,  0, 10, 10, 10, 10,  0,-10),
        intArrayOf(-10, 10, 10, 10, 10, 10, 10,-10),
        intArrayOf(-10,  5,  0,  0,  0,  0,  5,-10),
        intArrayOf(-20,-10,-10,-10,-10,-10,-10,-20)
    )

    private val ROOK_PST = arrayOf(
        intArrayOf(0,  0,  0,  0,  0,  0,  0,  0),
        intArrayOf(5, 10, 10, 10, 10, 10, 10,  5),
        intArrayOf(-5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf(-5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf(-5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf(-5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf(-5,  0,  0,  0,  0,  0,  0, -5),
        intArrayOf(0,  0,  0,  5,  5,  0,  0,  0)
    )

    private val QUEEN_PST = arrayOf(
        intArrayOf(-20,-10,-10, -5, -5,-10,-10,-20),
        intArrayOf(-10,  0,  0,  0,  0,  0,  0,-10),
        intArrayOf(-10,  0,  5,  5,  5,  5,  0,-10),
        intArrayOf(-5,  0,  5,  5,  5,  5,  0, -5),
        intArrayOf(0,  0,  5,  5,  5,  5,  0, -5),
        intArrayOf(-10,  5,  5,  5,  5,  5,  0,-10),
        intArrayOf(-10,  0,  5,  0,  0,  0,  0,-10),
        intArrayOf(-20,-10,-10, -5, -5,-10,-10,-20)
    )

    private val KING_PST = arrayOf(
        intArrayOf(-30,-40,-40,-50,-50,-40,-40,-30),
        intArrayOf(-30,-40,-40,-50,-50,-40,-40,-30),
        intArrayOf(-30,-40,-40,-50,-50,-40,-40,-30),
        intArrayOf(-30,-40,-40,-50,-50,-40,-40,-30),
        intArrayOf(-20,-30,-30,-40,-40,-30,-30,-20),
        intArrayOf(-10,-20,-20,-20,-20,-20,-20,-10),
        intArrayOf(20, 20,  0,  0,  0,  0, 20, 20),
        intArrayOf(20, 30, 10,  0,  0, 10, 30, 20)
    )
}
