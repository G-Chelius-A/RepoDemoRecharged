package com.example.cheliuschess.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.cheliuschess.R
import com.example.cheliuschess.engine.ChessBoardState
import com.example.cheliuschess.engine.ChessPiece
import com.example.cheliuschess.engine.Move
import com.example.cheliuschess.engine.PieceColor
import com.example.cheliuschess.engine.PieceType
import com.example.cheliuschess.engine.Position

class ChessBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface BoardEventListener {
        fun onSquareSelected(position: Position)
        fun onMoveRequested(move: Move)
    }

    var boardEventListener: BoardEventListener? = null

    private var boardState: ChessBoardState = ChessBoardState.initial()
    private var selectedPosition: Position? = null
    private var legalMoves: List<Move> = emptyList()
    private var lastMove: Move? = null
    private var isKingInCheck: Boolean = false
    private var kingInCheckPosition: Position? = null

    var isFlipped: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    // Colores del tablero
    private val lightSquareColor = Color.parseColor("#EEEED2")
    private val darkSquareColor = Color.parseColor("#769656")
    private val selectedSquareColor = Color.parseColor("#BBCB44")
    private val moveHighlightColor = Color.parseColor("#80208020")
    private val lastMoveColor = Color.parseColor("#80F5F682")
    private val checkColor = Color.parseColor("#CCFF3333")

    private val squarePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        isFakeBoldText = true
    }
    private val unicodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val pieceDrawableCache = mutableMapOf<Pair<PieceType, PieceColor>, Drawable?>()

    init {
        preloadPieceDrawables()
    }

    private fun preloadPieceDrawables() {
        val pieces = listOf(
            Pair(PieceType.PAWN, PieceColor.WHITE) to R.drawable.ic_chess_wp,
            Pair(PieceType.KNIGHT, PieceColor.WHITE) to R.drawable.ic_chess_wn,
            Pair(PieceType.BISHOP, PieceColor.WHITE) to R.drawable.ic_chess_wb,
            Pair(PieceType.ROOK, PieceColor.WHITE) to R.drawable.ic_chess_wr,
            Pair(PieceType.QUEEN, PieceColor.WHITE) to R.drawable.ic_chess_wq,
            Pair(PieceType.KING, PieceColor.WHITE) to R.drawable.ic_chess_wk,
            Pair(PieceType.PAWN, PieceColor.BLACK) to R.drawable.ic_chess_bp,
            Pair(PieceType.KNIGHT, PieceColor.BLACK) to R.drawable.ic_chess_bn,
            Pair(PieceType.BISHOP, PieceColor.BLACK) to R.drawable.ic_chess_bb,
            Pair(PieceType.ROOK, PieceColor.BLACK) to R.drawable.ic_chess_br,
            Pair(PieceType.QUEEN, PieceColor.BLACK) to R.drawable.ic_chess_bq,
            Pair(PieceType.KING, PieceColor.BLACK) to R.drawable.ic_chess_bk
        )

        for ((key, resId) in pieces) {
            pieceDrawableCache[key] = ContextCompat.getDrawable(context, resId)
        }
    }

    fun updateState(
        state: ChessBoardState,
        selectedPos: Position?,
        moves: List<Move>,
        lastAppliedMove: Move?,
        inCheck: Boolean,
        checkKingPos: Position?
    ) {
        this.boardState = state
        this.selectedPosition = selectedPos
        this.legalMoves = moves
        this.lastMove = lastAppliedMove
        this.isKingInCheck = inCheck
        this.kingInCheckPosition = checkKingPos
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = Math.min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = width.toFloat()
        val tileSize = size / 8f

        // 1. Dibujar casillas
        for (r in 0..7) {
            for (c in 0..7) {
                val displayRow = if (isFlipped) r else 7 - r
                val displayCol = if (isFlipped) 7 - c else c

                val isLight = (r + c) % 2 != 0
                squarePaint.color = if (isLight) lightSquareColor else darkSquareColor

                val left = displayCol * tileSize
                val top = displayRow * tileSize
                canvas.drawRect(left, top, left + tileSize, top + tileSize, squarePaint)
            }
        }

        // 2. Resaltado de Último Movimiento
        lastMove?.let { m ->
            drawSquareHighlight(canvas, m.from, tileSize, lastMoveColor)
            drawSquareHighlight(canvas, m.to, tileSize, lastMoveColor)
        }

        // 3. Resaltado de Casilla Seleccionada
        selectedPosition?.let { pos ->
            drawSquareHighlight(canvas, pos, tileSize, selectedSquareColor)
        }

        // 4. Resaltado de Rey en Jaque
        if (isKingInCheck && kingInCheckPosition != null) {
            drawSquareHighlight(canvas, kingInCheckPosition!!, tileSize, checkColor)
        }

        // 5. Indicadores de Movimientos Legales
        for (m in legalMoves) {
            val (displayRow, displayCol) = getDisplayCoordinates(m.to)
            val cx = (displayCol + 0.5f) * tileSize
            val cy = (displayRow + 0.5f) * tileSize

            val targetPiece = boardState.getPiece(m.to)
            if (targetPiece != null || m.moveType == com.example.cheliuschess.engine.MoveType.EN_PASSANT) {
                // Anillo de captura
                highlightPaint.color = moveHighlightColor
                highlightPaint.style = Paint.Style.STROKE
                highlightPaint.strokeWidth = tileSize * 0.12f
                canvas.drawCircle(cx, cy, tileSize * 0.4f, highlightPaint)
            } else {
                // Círculo centro para movimiento
                highlightPaint.color = moveHighlightColor
                highlightPaint.style = Paint.Style.FILL
                canvas.drawCircle(cx, cy, tileSize * 0.18f, highlightPaint)
            }
        }

        // 6. Dibujar Piezas
        unicodePaint.textSize = tileSize * 0.75f
        for (r in 0..7) {
            for (c in 0..7) {
                val pos = Position(r, c)
                val piece = boardState.getPiece(pos) ?: continue
                val (displayRow, displayCol) = getDisplayCoordinates(pos)

                val left = (displayCol * tileSize).toInt()
                val top = (displayRow * tileSize).toInt()
                val right = (left + tileSize).toInt()
                val bottom = (top + tileSize).toInt()

                val drawable = pieceDrawableCache[Pair(piece.type, piece.color)]
                if (drawable != null) {
                    drawable.setBounds(
                        left + (tileSize * 0.08f).toInt(),
                        top + (tileSize * 0.08f).toInt(),
                        right - (tileSize * 0.08f).toInt(),
                        bottom - (tileSize * 0.08f).toInt()
                    )
                    drawable.draw(canvas)
                } else {
                    // Fallback Unicode
                    val symbol = getUnicodeSymbol(piece)
                    unicodePaint.color = if (piece.color == PieceColor.WHITE) Color.WHITE else Color.BLACK
                    val baseline = top + tileSize / 2f - (unicodePaint.descent() + unicodePaint.ascent()) / 2f
                    canvas.drawText(symbol, left + tileSize / 2f, baseline, unicodePaint)
                }
            }
        }
    }

    private fun drawSquareHighlight(canvas: Canvas, pos: Position, tileSize: Float, color: Int) {
        val (displayRow, displayCol) = getDisplayCoordinates(pos)
        highlightPaint.color = color
        highlightPaint.style = Paint.Style.FILL
        val left = displayCol * tileSize
        val top = displayRow * tileSize
        canvas.drawRect(left, top, left + tileSize, top + tileSize, highlightPaint)
    }

    private fun getDisplayCoordinates(pos: Position): Pair<Int, Int> {
        val displayRow = if (isFlipped) pos.row else 7 - pos.row
        val displayCol = if (isFlipped) 7 - pos.col else pos.col
        return Pair(displayRow, displayCol)
    }

    private fun getPositionFromCoordinates(x: Float, y: Float): Position? {
        val tileSize = width / 8f
        val col = (x / tileSize).toInt()
        val row = (y / tileSize).toInt()

        if (col !in 0..7 || row !in 0..7) return null

        val boardRow = if (isFlipped) row else 7 - row
        val boardCol = if (isFlipped) 7 - col else col

        val pos = Position(boardRow, boardCol)
        return if (pos.isValid()) pos else null
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            performClick()
            val touchedPos = getPositionFromCoordinates(event.x, event.y) ?: return false

            val matchingMove = legalMoves.find { it.to == touchedPos }
            if (matchingMove != null) {
                boardEventListener?.onMoveRequested(matchingMove)
            } else {
                boardEventListener?.onSquareSelected(touchedPos)
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun getUnicodeSymbol(piece: ChessPiece): String {
        return when (piece.color) {
            PieceColor.WHITE -> when (piece.type) {
                PieceType.KING -> "♔"
                PieceType.QUEEN -> "♕"
                PieceType.ROOK -> "♖"
                PieceType.BISHOP -> "♗"
                PieceType.KNIGHT -> "♘"
                PieceType.PAWN -> "♙"
            }
            PieceColor.BLACK -> when (piece.type) {
                PieceType.KING -> "♚"
                PieceType.QUEEN -> "♛"
                PieceType.ROOK -> "♜"
                PieceType.BISHOP -> "♝"
                PieceType.KNIGHT -> "♞"
                PieceType.PAWN -> "♟"
            }
        }
    }
}
