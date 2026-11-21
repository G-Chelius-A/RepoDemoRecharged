package com.example.cheliuschess.engine

class GameHistoryManager {

    data class UndoResult(
        val boardState: ChessBoardState,
        val whiteMillis: Long,
        val blackMillis: Long,
        val undoneMoveRecord: GameMoveRecord?
    )

    private val boardStateHistory = mutableListOf<ChessBoardState>()
    private val moveRecordHistory = mutableListOf<GameMoveRecord>()
    private val clockHistory = mutableListOf<Pair<Long, Long>>()

    fun reset(initialState: ChessBoardState, initialClock: Pair<Long, Long>) {
        boardStateHistory.clear()
        moveRecordHistory.clear()
        clockHistory.clear()

        boardStateHistory.add(initialState)
        clockHistory.add(initialClock)
    }

    fun recordMove(
        moveRecord: GameMoveRecord,
        newState: ChessBoardState,
        newClock: Pair<Long, Long>
    ) {
        moveRecordHistory.add(moveRecord)
        boardStateHistory.add(newState)
        clockHistory.add(newClock)
    }

    fun canUndo(): Boolean {
        return boardStateHistory.size > 1
    }

    fun undo(): UndoResult? {
        if (!canUndo()) return null

        // Remover el estado actual
        boardStateHistory.removeAt(boardStateHistory.size - 1)
        clockHistory.removeAt(clockHistory.size - 1)
        val undoneMove = if (moveRecordHistory.isNotEmpty()) {
            moveRecordHistory.removeAt(moveRecordHistory.size - 1)
        } else null

        val previousState = boardStateHistory.last()
        val previousClock = clockHistory.last()

        return UndoResult(
            boardState = previousState,
            whiteMillis = previousClock.first,
            blackMillis = previousClock.second,
            undoneMoveRecord = undoneMove
        )
    }

    fun getFenList(): List<String> {
        return boardStateHistory.map { it.toFen() }
    }

    fun getSanMoveList(): List<String> {
        return moveRecordHistory.map { it.sanNotation }
    }

    fun getFormattedSanScoreSheet(): String {
        if (moveRecordHistory.isEmpty()) return "-"
        val sb = StringBuilder()
        for (i in moveRecordHistory.indices) {
            if (i % 2 == 0) {
                val moveNum = (i / 2) + 1
                if (i > 0) sb.append("  ")
                sb.append("$moveNum. ").append(moveRecordHistory[i].sanNotation)
            } else {
                sb.append(" ").append(moveRecordHistory[i].sanNotation)
            }
        }
        return sb.toString()
    }

    fun getMoveRecordList(): List<GameMoveRecord> {
        return moveRecordHistory.toList()
    }
}
