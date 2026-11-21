package com.example.cheliuschess

import com.example.cheliuschess.engine.AiDifficulty
import com.example.cheliuschess.engine.ChessAiEngine
import com.example.cheliuschess.engine.ChessBoardState
import com.example.cheliuschess.engine.ChessRulesEngine
import com.example.cheliuschess.engine.Move
import com.example.cheliuschess.engine.PieceColor
import com.example.cheliuschess.engine.PieceType
import com.example.cheliuschess.engine.Position
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ChessAiEngineTest {

    @Test
    fun getBestMove_returnsValidMoveForInitialBoard() = runBlocking {
        val state = ChessBoardState.initial()
        val move = ChessAiEngine.getBestMove(state, AiDifficulty.EASY)
        assertNotNull(move)

        val legalMoves = ChessRulesEngine.getAllLegalMoves(state, PieceColor.WHITE)
        assert(legalMoves.contains(move))
    }

    @Test
    fun getBestMove_findsMateInOne() = runBlocking {
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

        // Le toca a las Blancas (IA en Difícil debe encontrar Qxf7#)
        val bestMove = ChessAiEngine.getBestMove(state, AiDifficulty.HARD)
        assertNotNull(bestMove)
        assertEquals(Position(4, 7), bestMove!!.from)
        assertEquals(Position(6, 5), bestMove.to)
    }
}
