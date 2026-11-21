package com.example.cheliuschess.engine

object ChessRulesEngine {

    fun getLegalMoves(state: ChessBoardState, from: Position): List<Move> {
        val piece = state.getPiece(from) ?: return emptyList()
        if (piece.color != state.turn) return emptyList()

        val pseudoMoves = generatePseudoLegalMoves(state, from)
        return pseudoMoves.filter { move ->
            val nextState = applyMove(state, move)
            !isKingInCheck(nextState.grid, state.turn)
        }
    }

    fun getAllLegalMoves(state: ChessBoardState, color: PieceColor): List<Move> {
        val moves = mutableListOf<Move>()
        for (r in 0..7) {
            for (c in 0..7) {
                val pos = Position(r, c)
                val piece = state.getPiece(pos)
                if (piece != null && piece.color == color) {
                    val pseudoMoves = generatePseudoLegalMoves(state, pos)
                    for (m in pseudoMoves) {
                        val nextState = applyMove(state, m)
                        if (!isKingInCheck(nextState.grid, color)) {
                            moves.add(m)
                        }
                    }
                }
            }
        }
        return moves
    }

    fun isKingInCheck(grid: Array<Array<ChessPiece?>>, color: PieceColor): Boolean {
        val kingPos = findKing(grid, color) ?: return false
        return isSquareAttacked(grid, kingPos, color.opposite())
    }

    fun evaluateGameState(state: ChessBoardState): GameState {
        val inCheck = isKingInCheck(state.grid, state.turn)
        val legalMoves = getAllLegalMoves(state, state.turn)

        if (legalMoves.isEmpty()) {
            return if (inCheck) {
                if (state.turn == PieceColor.WHITE) GameState.CHECKMATE_BLACK_WIN else GameState.CHECKMATE_WHITE_WIN
            } else {
                GameState.STALEMATE
            }
        }

        if (isInsufficientMaterial(state.grid)) {
            return GameState.DRAW_MATERIAL
        }

        return if (inCheck) GameState.CHECK else GameState.ACTIVE
    }

    fun applyMove(state: ChessBoardState, move: Move): ChessBoardState {
        val newGrid = state.copyBoard()
        val piece = move.piece.copyWithMoved()
        var newEnPassantTarget: Position? = null
        val capturedPieces = state.capturedPieces.toMutableList()

        // Limpiar origen
        newGrid[move.from.row][move.from.col] = null

        val actualCaptured = move.capturedPiece

        when (move.moveType) {
            MoveType.NORMAL -> {
                if (actualCaptured != null) {
                    capturedPieces.add(actualCaptured)
                }
                // Si el peón avanza 2 casillas, registrar casilla para Al Paso
                if (piece.type == PieceType.PAWN && Math.abs(move.to.row - move.from.row) == 2) {
                    val targetRow = (move.from.row + move.to.row) / 2
                    newEnPassantTarget = Position(targetRow, move.from.col)
                }
                newGrid[move.to.row][move.to.col] = piece
            }
            MoveType.EN_PASSANT -> {
                val capturedPawnRow = move.from.row
                val capturedPawnCol = move.to.col
                val pawnCaptured = newGrid[capturedPawnRow][capturedPawnCol]
                if (pawnCaptured != null) {
                    capturedPieces.add(pawnCaptured)
                }
                newGrid[capturedPawnRow][capturedPawnCol] = null
                newGrid[move.to.row][move.to.col] = piece
            }
            MoveType.CASTLING_KINGSIDE -> {
                newGrid[move.to.row][move.to.col] = piece
                val rookRow = move.from.row
                val rookFrom = Position(rookRow, 7)
                val rookTo = Position(rookRow, 5)
                val rook = newGrid[rookFrom.row][rookFrom.col]
                if (rook != null) {
                    newGrid[rookFrom.row][rookFrom.col] = null
                    newGrid[rookTo.row][rookTo.col] = rook.copyWithMoved()
                }
            }
            MoveType.CASTLING_QUEENSIDE -> {
                newGrid[move.to.row][move.to.col] = piece
                val rookRow = move.from.row
                val rookFrom = Position(rookRow, 0)
                val rookTo = Position(rookRow, 3)
                val rook = newGrid[rookFrom.row][rookFrom.col]
                if (rook != null) {
                    newGrid[rookFrom.row][rookFrom.col] = null
                    newGrid[rookTo.row][rookTo.col] = rook.copyWithMoved()
                }
            }
            MoveType.PROMOTION -> {
                if (actualCaptured != null) {
                    capturedPieces.add(actualCaptured)
                }
                val promType = move.promotionPieceType ?: PieceType.QUEEN
                newGrid[move.to.row][move.to.col] = ChessPiece(promType, piece.color, true)
            }
        }

        val nextTurn = state.turn.opposite()
        val isHalfReset = piece.type == PieceType.PAWN || actualCaptured != null
        val nextHalfClock = if (isHalfReset) 0 else state.halfMoveClock + 1
        val nextFullMove = if (state.turn == PieceColor.BLACK) state.fullMoveNumber + 1 else state.fullMoveNumber

        return ChessBoardState(
            grid = newGrid,
            turn = nextTurn,
            enPassantTarget = newEnPassantTarget,
            capturedPieces = capturedPieces,
            halfMoveClock = nextHalfClock,
            fullMoveNumber = nextFullMove
        )
    }

    private fun generatePseudoLegalMoves(state: ChessBoardState, from: Position): List<Move> {
        val piece = state.getPiece(from) ?: return emptyList()
        val moves = mutableListOf<Move>()

        when (piece.type) {
            PieceType.PAWN -> generatePawnMoves(state, from, piece, moves)
            PieceType.KNIGHT -> generateKnightMoves(state, from, piece, moves)
            PieceType.BISHOP -> generateRayMoves(state, from, piece, moves, BISHOP_DIRECTIONS)
            PieceType.ROOK -> generateRayMoves(state, from, piece, moves, ROOK_DIRECTIONS)
            PieceType.QUEEN -> generateRayMoves(state, from, piece, moves, QUEEN_DIRECTIONS)
            PieceType.KING -> generateKingMoves(state, from, piece, moves)
        }

        return moves
    }

    private fun generatePawnMoves(
        state: ChessBoardState,
        from: Position,
        pawn: ChessPiece,
        moves: MutableList<Move>
    ) {
        val direction = if (pawn.color == PieceColor.WHITE) 1 else -1
        val startRow = if (pawn.color == PieceColor.WHITE) 1 else 6
        val promoRow = if (pawn.color == PieceColor.WHITE) 7 else 0

        // 1 Paso Adelante
        val oneStep = Position(from.row + direction, from.col)
        if (oneStep.isValid() && state.getPiece(oneStep) == null) {
            if (oneStep.row == promoRow) {
                addPromotionMoves(from, oneStep, pawn, null, moves)
            } else {
                moves.add(Move(from, oneStep, pawn))
                // 2 Pasos Adelante
                val twoStep = Position(from.row + 2 * direction, from.col)
                if (from.row == startRow && twoStep.isValid() && state.getPiece(twoStep) == null) {
                    moves.add(Move(from, twoStep, pawn))
                }
            }
        }

        // Capturas Diagonales
        val captureCols = intArrayOf(from.col - 1, from.col + 1)
        for (c in captureCols) {
            val target = Position(from.row + direction, c)
            if (target.isValid()) {
                val targetPiece = state.getPiece(target)
                if (targetPiece != null && targetPiece.color != pawn.color) {
                    if (target.row == promoRow) {
                        addPromotionMoves(from, target, pawn, targetPiece, moves)
                    } else {
                        moves.add(Move(from, target, pawn, capturedPiece = targetPiece))
                    }
                } else if (state.enPassantTarget != null && target == state.enPassantTarget) {
                    moves.add(Move(from, target, pawn, moveType = MoveType.EN_PASSANT))
                }
            }
        }
    }

    private fun addPromotionMoves(
        from: Position,
        to: Position,
        pawn: ChessPiece,
        captured: ChessPiece?,
        moves: MutableList<Move>
    ) {
        val promoTypes = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
        for (pt in promoTypes) {
            moves.add(
                Move(
                    from = from,
                    to = to,
                    piece = pawn,
                    capturedPiece = captured,
                    moveType = MoveType.PROMOTION,
                    promotionPieceType = pt
                )
            )
        }
    }

    private fun generateKnightMoves(
        state: ChessBoardState,
        from: Position,
        knight: ChessPiece,
        moves: MutableList<Move>
    ) {
        val offsets = arrayOf(
            intArrayOf(2, 1), intArrayOf(2, -1), intArrayOf(-2, 1), intArrayOf(-2, -1),
            intArrayOf(1, 2), intArrayOf(1, -2), intArrayOf(-1, 2), intArrayOf(-1, -2)
        )
        for (off in offsets) {
            val target = Position(from.row + off[0], from.col + off[1])
            if (target.isValid()) {
                val targetPiece = state.getPiece(target)
                if (targetPiece == null || targetPiece.color != knight.color) {
                    moves.add(Move(from, target, knight, capturedPiece = targetPiece))
                }
            }
        }
    }

    private fun generateRayMoves(
        state: ChessBoardState,
        from: Position,
        piece: ChessPiece,
        moves: MutableList<Move>,
        directions: Array<IntArray>
    ) {
        for (dir in directions) {
            var step = 1
            while (true) {
                val target = Position(from.row + dir[0] * step, from.col + dir[1] * step)
                if (!target.isValid()) break
                val targetPiece = state.getPiece(target)
                if (targetPiece == null) {
                    moves.add(Move(from, target, piece))
                } else {
                    if (targetPiece.color != piece.color) {
                        moves.add(Move(from, target, piece, capturedPiece = targetPiece))
                    }
                    break
                }
                step++
            }
        }
    }

    private fun generateKingMoves(
        state: ChessBoardState,
        from: Position,
        king: ChessPiece,
        moves: MutableList<Move>
    ) {
        val dir = QUEEN_DIRECTIONS
        for (d in dir) {
            val target = Position(from.row + d[0], from.col + d[1])
            if (target.isValid()) {
                val targetPiece = state.getPiece(target)
                if (targetPiece == null || targetPiece.color != king.color) {
                    moves.add(Move(from, target, king, capturedPiece = targetPiece))
                }
            }
        }

        // Enroques
        if (!king.hasMoved && !isKingInCheck(state.grid, king.color)) {
            val row = from.row
            // Enroque Corto (Kingside)
            val rookKingside = state.getPiece(Position(row, 7))
            if (rookKingside != null && rookKingside.type == PieceType.ROOK && !rookKingside.hasMoved) {
                if (state.getPiece(Position(row, 5)) == null && state.getPiece(Position(row, 6)) == null) {
                    if (!isSquareAttacked(state.grid, Position(row, 5), king.color.opposite()) &&
                        !isSquareAttacked(state.grid, Position(row, 6), king.color.opposite())
                    ) {
                        moves.add(
                            Move(
                                from,
                                Position(row, 6),
                                king,
                                moveType = MoveType.CASTLING_KINGSIDE
                            )
                        )
                    }
                }
            }

            // Enroque Largo (Queenside)
            val rookQueenside = state.getPiece(Position(row, 0))
            if (rookQueenside != null && rookQueenside.type == PieceType.ROOK && !rookQueenside.hasMoved) {
                if (state.getPiece(Position(row, 1)) == null &&
                    state.getPiece(Position(row, 2)) == null &&
                    state.getPiece(Position(row, 3)) == null
                ) {
                    if (!isSquareAttacked(state.grid, Position(row, 3), king.color.opposite()) &&
                        !isSquareAttacked(state.grid, Position(row, 2), king.color.opposite())
                    ) {
                        moves.add(
                            Move(
                                from,
                                Position(row, 2),
                                king,
                                moveType = MoveType.CASTLING_QUEENSIDE
                            )
                        )
                    }
                }
            }
        }
    }

    private fun findKing(grid: Array<Array<ChessPiece?>>, color: PieceColor): Position? {
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = grid[r][c]
                if (piece != null && piece.type == PieceType.KING && piece.color == color) {
                    return Position(r, c)
                }
            }
        }
        return null
    }

    private fun isSquareAttacked(
        grid: Array<Array<ChessPiece?>>,
        targetPos: Position,
        attackerColor: PieceColor
    ): Boolean {
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = grid[r][c]
                if (piece != null && piece.color == attackerColor) {
                    val pos = Position(r, c)
                    if (canPieceAttackTarget(grid, pos, piece, targetPos)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun canPieceAttackTarget(
        grid: Array<Array<ChessPiece?>>,
        from: Position,
        piece: ChessPiece,
        target: Position
    ): Boolean {
        val dr = target.row - from.row
        val dc = target.col - from.col

        when (piece.type) {
            PieceType.PAWN -> {
                val direction = if (piece.color == PieceColor.WHITE) 1 else -1
                return dr == direction && (dc == 1 || dc == -1)
            }
            PieceType.KNIGHT -> {
                val absR = Math.abs(dr)
                val absC = Math.abs(dc)
                return (absR == 1 && absC == 2) || (absR == 2 && absC == 1)
            }
            PieceType.BISHOP -> {
                if (Math.abs(dr) == Math.abs(dc) && dr != 0) {
                    return isPathClear(grid, from, target)
                }
            }
            PieceType.ROOK -> {
                if ((dr == 0 && dc != 0) || (dr != 0 && dc == 0)) {
                    return isPathClear(grid, from, target)
                }
            }
            PieceType.QUEEN -> {
                if (Math.abs(dr) == Math.abs(dc) || dr == 0 || dc == 0) {
                    return isPathClear(grid, from, target)
                }
            }
            PieceType.KING -> {
                return Math.abs(dr) <= 1 && Math.abs(dc) <= 1
            }
        }
        return false
    }

    private fun isPathClear(grid: Array<Array<ChessPiece?>>, from: Position, to: Position): Boolean {
        val stepR = Integer.signum(to.row - from.row)
        val stepC = Integer.signum(to.col - from.col)

        var currR = from.row + stepR
        var currC = from.col + stepC

        while (currR != to.row || currC != to.col) {
            if (grid[currR][currC] != null) return false
            currR += stepR
            currC += stepC
        }
        return true
    }

    private fun isInsufficientMaterial(grid: Array<Array<ChessPiece?>>): Boolean {
        val pieces = mutableListOf<ChessPiece>()
        for (r in 0..7) {
            for (c in 0..7) {
                grid[r][c]?.let { pieces.add(it) }
            }
        }
        if (pieces.size <= 2) return true // Solo Reyes
        if (pieces.size == 3) {
            val nonKing = pieces.find { it.type != PieceType.KING }
            if (nonKing != null && (nonKing.type == PieceType.BISHOP || nonKing.type == PieceType.KNIGHT)) {
                return true // Rey y Alfil/Caballo vs Rey
            }
        }
        return false
    }

    private val BISHOP_DIRECTIONS = arrayOf(
        intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, 1), intArrayOf(-1, -1)
    )
    private val ROOK_DIRECTIONS = arrayOf(
        intArrayOf(1, 0), intArrayOf(-1, 0), intArrayOf(0, 1), intArrayOf(0, -1)
    )
    private val QUEEN_DIRECTIONS = arrayOf(
        intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, 1), intArrayOf(-1, -1),
        intArrayOf(1, 0), intArrayOf(-1, 0), intArrayOf(0, 1), intArrayOf(0, -1)
    )

    fun generateSanNotation(stateBefore: ChessBoardState, move: Move, stateAfter: ChessBoardState): String {
        if (move.moveType == MoveType.CASTLING_KINGSIDE) return "O-O"
        if (move.moveType == MoveType.CASTLING_QUEENSIDE) return "O-O-O"

        val piece = move.piece
        val isPawn = piece.type == PieceType.PAWN

        val pieceSymbol = when (piece.type) {
            PieceType.PAWN -> ""
            PieceType.KNIGHT -> "N"
            PieceType.BISHOP -> "B"
            PieceType.ROOK -> "R"
            PieceType.QUEEN -> "Q"
            PieceType.KING -> "K"
        }

        var disambiguation = ""
        if (!isPawn && piece.type != PieceType.KING) {
            val otherCandidates = mutableListOf<Position>()
            for (r in 0..7) {
                for (c in 0..7) {
                    val candidatePos = Position(r, c)
                    if (candidatePos != move.from) {
                        val p = stateBefore.getPiece(candidatePos)
                        if (p != null && p.type == piece.type && p.color == piece.color) {
                            val legalMoves = getLegalMoves(stateBefore, candidatePos)
                            if (legalMoves.any { it.to == move.to }) {
                                otherCandidates.add(candidatePos)
                            }
                        }
                    }
                }
            }

            if (otherCandidates.isNotEmpty()) {
                val sameFile = otherCandidates.any { it.col == move.from.col }
                val sameRank = otherCandidates.any { it.row == move.from.row }

                disambiguation = if (!sameFile) {
                    "${('a' + move.from.col)}"
                } else if (!sameRank) {
                    "${(move.from.row + 1)}"
                } else {
                    move.from.toAlgebraic()
                }
            }
        }

        val isCapture = move.capturedPiece != null || move.moveType == MoveType.EN_PASSANT
        val captureSymbol = if (isCapture) {
            if (isPawn) "${('a' + move.from.col)}x" else "x"
        } else ""

        val promoSymbol = if (move.moveType == MoveType.PROMOTION) {
            "=" + when (move.promotionPieceType) {
                PieceType.QUEEN -> "Q"
                PieceType.ROOK -> "R"
                PieceType.BISHOP -> "B"
                PieceType.KNIGHT -> "N"
                else -> "Q"
            }
        } else ""

        val destSquare = move.to.toAlgebraic()
        val baseSan = "$pieceSymbol$disambiguation$captureSymbol$destSquare$promoSymbol"

        val nextGameState = evaluateGameState(stateAfter)
        val suffix = when (nextGameState) {
            GameState.CHECKMATE_WHITE_WIN, GameState.CHECKMATE_BLACK_WIN -> "#"
            GameState.CHECK -> "+"
            else -> ""
        }

        return "$baseSan$suffix"
    }
}
