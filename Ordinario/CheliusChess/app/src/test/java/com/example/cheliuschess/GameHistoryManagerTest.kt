package com.example.cheliuschess

import com.example.cheliuschess.engine.ChessBoardState
import com.example.cheliuschess.engine.ChessRulesEngine
import com.example.cheliuschess.engine.GameHistoryManager
import com.example.cheliuschess.engine.GameMoveRecord
import com.example.cheliuschess.engine.Move
import com.example.cheliuschess.engine.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameHistoryManagerTest {

    @Test
    fun testUndo_restoresPreviousBoardAndClockState() {
        val manager = GameHistoryManager()
        val initial = ChessBoardState.initial()
        val initialClock = Pair(600000L, 600000L)

        manager.reset(initial, initialClock)
        assertFalse(manager.canUndo())

        val move = Move(
            from = Position(1, 4),
            to = Position(3, 4),
            piece = initial.getPiece(Position(1, 4))!!
        )
        val nextState = ChessRulesEngine.applyMove(initial, move)
        val nextClock = Pair(598000L, 600000L)

        val moveRecord = GameMoveRecord(
            move = move,
            sanNotation = "e4",
            fenBefore = initial.toFen(),
            fenAfter = nextState.toFen(),
            whiteMillis = 598000L,
            blackMillis = 600000L
        )

        manager.recordMove(moveRecord, nextState, nextClock)
        assertTrue(manager.canUndo())
        assertEquals(1, manager.getSanMoveList().size)

        val undoResult = manager.undo()
        assertNotNull(undoResult)
        assertEquals(initial, undoResult!!.boardState)
        assertEquals(600000L, undoResult.whiteMillis)
        assertEquals(600000L, undoResult.blackMillis)
        assertFalse(manager.canUndo())
    }
}
