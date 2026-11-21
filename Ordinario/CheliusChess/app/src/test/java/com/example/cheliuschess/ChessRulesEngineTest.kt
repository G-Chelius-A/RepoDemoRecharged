package com.example.cheliuschess

import com.example.cheliuschess.engine.ChessBoardState
import com.example.cheliuschess.engine.ChessPiece
import com.example.cheliuschess.engine.ChessRulesEngine
import com.example.cheliuschess.engine.GameState
import com.example.cheliuschess.engine.Move
import com.example.cheliuschess.engine.MoveType
import com.example.cheliuschess.engine.PieceColor
import com.example.cheliuschess.engine.PieceType
import com.example.cheliuschess.engine.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessRulesEngineTest {

    @Test
    fun initialBoardState_hasCorrectTurnAndPieces() {
        val state = ChessBoardState.initial()
        assertEquals(PieceColor.WHITE, state.turn)
        assertEquals(ChessPiece(PieceType.KING, PieceColor.WHITE), state.getPiece(Position(0, 4)))
        assertEquals(ChessPiece(PieceType.KING, PieceColor.BLACK), state.getPiece(Position(7, 4)))
        assertEquals(ChessPiece(PieceType.PAWN, PieceColor.WHITE), state.getPiece(Position(1, 4)))
    }

    @Test
    fun pawnInitialMoves_allowsOneOrTwoSteps() {
        val state = ChessBoardState.initial()
        val e2Moves = ChessRulesEngine.getLegalMoves(state, Position(1, 4)) // e2
        assertEquals(2, e2Moves.size)

        val targets = e2Moves.map { it.to }
        assertTrue(targets.contains(Position(2, 4))) // e3
        assertTrue(targets.contains(Position(3, 4))) // e4
    }

    @Test
    fun knightMoves_movesInL() {
        val state = ChessBoardState.initial()
        val b1Moves = ChessRulesEngine.getLegalMoves(state, Position(0, 1)) // Nb1
        assertEquals(2, b1Moves.size)

        val targets = b1Moves.map { it.to }
        assertTrue(targets.contains(Position(2, 0))) // Na3
        assertTrue(targets.contains(Position(2, 2))) // Nc3
    }

    @Test
    fun applyMove_swapsTurnAndUpdatesPosition() {
        val state = ChessBoardState.initial()
        val move = Move(
            from = Position(1, 4), // e2
            to = Position(3, 4),   // e4
            piece = state.getPiece(Position(1, 4))!!
        )

        val nextState = ChessRulesEngine.applyMove(state, move)
        assertEquals(PieceColor.BLACK, nextState.turn)
        assertEquals(null, nextState.getPiece(Position(1, 4)))
        assertNotNull(nextState.getPiece(Position(3, 4)))
        assertEquals(Position(2, 4), nextState.enPassantTarget)
    }

    @Test
    fun scholarMate_resultsInCheckmateBlackWin() {
        var state = ChessBoardState.initial()

        // 1. e4 e5
        state = ChessRulesEngine.applyMove(state, Move(Position(1, 4), Position(3, 4), state.getPiece(Position(1, 4))!!))
        state = ChessRulesEngine.applyMove(state, Move(Position(6, 4), Position(4, 4), state.getPiece(Position(6, 4))!!))

        // 2. Bc4 Nc6
        state = ChessRulesEngine.applyMove(state, Move(Position(0, 5), Position(3, 2), state.getPiece(Position(0, 5))!!))
        state = ChessRulesEngine.applyMove(state, Move(Position(7, 1), Position(5, 2), state.getPiece(Position(7, 1))!!))

        // 3. Qh5 Nf6
        state = ChessRulesEngine.applyMove(state, Move(Position(0, 3), Position(4, 7), state.getPiece(Position(0, 3))!!))
        state = ChessRulesEngine.applyMove(state, Move(Position(7, 6), Position(5, 5), state.getPiece(Position(7, 6))!!))

        // 4. Qxf7#
        val queenMove = Move(
            from = Position(4, 7),
            to = Position(6, 5),
            piece = state.getPiece(Position(4, 7))!!,
            capturedPiece = state.getPiece(Position(6, 5))
        )
        state = ChessRulesEngine.applyMove(state, queenMove)

        val gameState = ChessRulesEngine.evaluateGameState(state)
        assertEquals(GameState.CHECKMATE_WHITE_WIN, gameState)
        assertEquals("1 - 0", gameState.getScore().displayText)
    }

    @Test
    fun castlingKingside_movesKingAndRookCorrectly() {
        val grid = Array(8) { Array<ChessPiece?>(8) { null } }
        grid[0][4] = ChessPiece(PieceType.KING, PieceColor.WHITE, hasMoved = false)
        grid[0][7] = ChessPiece(PieceType.ROOK, PieceColor.WHITE, hasMoved = false)
        grid[7][4] = ChessPiece(PieceType.KING, PieceColor.BLACK, hasMoved = false)

        val state = ChessBoardState(grid = grid, turn = PieceColor.WHITE)
        val moves = ChessRulesEngine.getLegalMoves(state, Position(0, 4))

        val castlingMove = moves.find { it.moveType == MoveType.CASTLING_KINGSIDE }
        assertNotNull(castlingMove)

        val nextState = ChessRulesEngine.applyMove(state, castlingMove!!)
        assertEquals(ChessPiece(PieceType.KING, PieceColor.WHITE, hasMoved = true), nextState.getPiece(Position(0, 6)))
        assertEquals(ChessPiece(PieceType.ROOK, PieceColor.WHITE, hasMoved = true), nextState.getPiece(Position(0, 5)))
    }
}
