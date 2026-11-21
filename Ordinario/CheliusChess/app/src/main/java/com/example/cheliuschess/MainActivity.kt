package com.example.cheliuschess

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cheliuschess.cloud.CloudGameRepository
import com.example.cheliuschess.cloud.GameRecord
import com.example.cheliuschess.databinding.ActivityMainBinding
import com.example.cheliuschess.engine.ChessBoardState
import com.example.cheliuschess.engine.ChessPiece
import com.example.cheliuschess.engine.ChessRulesEngine
import com.example.cheliuschess.engine.GameHistoryManager
import com.example.cheliuschess.engine.GameMoveRecord
import com.example.cheliuschess.engine.GameState
import com.example.cheliuschess.engine.Move
import com.example.cheliuschess.engine.MoveType
import com.example.cheliuschess.engine.PieceColor
import com.example.cheliuschess.engine.PieceType
import com.example.cheliuschess.engine.Position
import com.example.cheliuschess.model.TimeConfig
import com.example.cheliuschess.timer.ChessClock
import com.example.cheliuschess.ui.ChessBoardView
import java.util.UUID

class MainActivity : AppCompatActivity(), ChessBoardView.BoardEventListener, ChessClock.ClockListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chessClock: ChessClock
    private lateinit var gameHistoryManager: GameHistoryManager
    private lateinit var cloudGameRepository: CloudGameRepository

    private var currentBoardState: ChessBoardState = ChessBoardState.initial()
    private var selectedPosition: Position? = null
    private var currentLegalMoves: List<Move> = emptyList()
    private var lastAppliedMove: Move? = null
    private var currentGameState: GameState = GameState.ACTIVE
    private var currentTimeConfig: TimeConfig = TimeConfig(minutes = 10, seconds = 0, incrementSeconds = 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        chessClock = ChessClock(this)
        gameHistoryManager = GameHistoryManager()
        cloudGameRepository = CloudGameRepository(this)

        binding.chessBoardView.boardEventListener = this

        setupClickListeners()
        startNewGame(currentTimeConfig)
    }

    private fun setupClickListeners() {
        binding.btnTimeConfig.setOnClickListener { showTimeConfigDialog() }
        binding.btnPause.setOnClickListener { togglePauseGame() }
        binding.btnUndo.setOnClickListener { undoMove() }
        binding.btnFlip.setOnClickListener {
            binding.chessBoardView.isFlipped = !binding.chessBoardView.isFlipped
        }
        binding.btnCloud.setOnClickListener { showCloudRecordsDialog() }
    }

    private fun startNewGame(config: TimeConfig) {
        currentTimeConfig = config
        currentBoardState = ChessBoardState.initial()
        selectedPosition = null
        currentLegalMoves = emptyList()
        lastAppliedMove = null
        currentGameState = GameState.ACTIVE

        chessClock.setup(config)
        gameHistoryManager.reset(
            initialState = currentBoardState,
            initialClock = Pair(config.getTotalMillis(), config.getTotalMillis())
        )

        updateUi()
    }

    override fun onSquareSelected(position: Position) {
        if (currentGameState.isGameOver() || chessClock.isPaused) return

        val piece = currentBoardState.getPiece(position)
        if (piece != null && piece.color == currentBoardState.turn) {
            selectedPosition = position
            currentLegalMoves = ChessRulesEngine.getLegalMoves(currentBoardState, position)
        } else {
            selectedPosition = null
            currentLegalMoves = emptyList()
        }
        updateUi()
    }

    override fun onMoveRequested(move: Move) {
        if (currentGameState.isGameOver() || chessClock.isPaused) return

        if (move.moveType == MoveType.PROMOTION) {
            showPawnPromotionDialog(move)
        } else {
            executeMove(move)
        }
    }

    private fun executeMove(move: Move) {
        val fenBefore = currentBoardState.toFen()
        val nextState = ChessRulesEngine.applyMove(currentBoardState, move)
        val sanNotation = generateSanNotation(move)

        // Actualizar reloj con incremento
        chessClock.onMoveFinished(currentBoardState.turn)

        val newClockTimes = Pair(chessClock.whiteMillis, chessClock.blackMillis)
        val fenAfter = nextState.toFen()

        val moveRecord = GameMoveRecord(
            move = move,
            sanNotation = sanNotation,
            fenBefore = fenBefore,
            fenAfter = fenAfter,
            whiteMillis = chessClock.whiteMillis,
            blackMillis = chessClock.blackMillis
        )

        gameHistoryManager.recordMove(moveRecord, nextState, newClockTimes)

        currentBoardState = nextState
        lastAppliedMove = move
        selectedPosition = null
        currentLegalMoves = emptyList()

        currentGameState = ChessRulesEngine.evaluateGameState(currentBoardState)

        if (currentGameState.isGameOver()) {
            chessClock.stop()
            saveGameToCloud(currentGameState.name)
        }

        updateUi()
    }

    private fun undoMove() {
        if (!gameHistoryManager.canUndo()) {
            Toast.makeText(this, "No hay jugadas para revertir", Toast.LENGTH_SHORT).show()
            return
        }

        val undoResult = gameHistoryManager.undo() ?: return
        currentBoardState = undoResult.boardState
        chessClock.setTime(undoResult.whiteMillis, undoResult.blackMillis)

        selectedPosition = null
        currentLegalMoves = emptyList()
        lastAppliedMove = gameHistoryManager.getMoveRecordList().lastOrNull()?.move
        currentGameState = ChessRulesEngine.evaluateGameState(currentBoardState)

        if (currentGameState == GameState.ACTIVE || currentGameState == GameState.CHECK) {
            chessClock.startTurn(currentBoardState.turn)
        }

        Toast.makeText(this, "Jugada revertida", Toast.LENGTH_SHORT).show()
        updateUi()
    }

    private fun togglePauseGame() {
        if (currentGameState.isGameOver()) return

        val isPausedNow = chessClock.togglePause()
        if (isPausedNow) {
            binding.btnPause.text = "Reanudar"
            Toast.makeText(this, "Juego pausado", Toast.LENGTH_SHORT).show()
        } else {
            binding.btnPause.text = "Pausa"
            Toast.makeText(this, "Juego reanudado", Toast.LENGTH_SHORT).show()
        }
        updateUi()
    }

    override fun onTick(whiteMillis: Long, blackMillis: Long) {
        runOnUiThread {
            binding.tvWhiteTimer.text = ChessClock.formatTime(whiteMillis)
            binding.tvBlackTimer.text = ChessClock.formatTime(blackMillis)
        }
    }

    override fun onTimeout(winnerColor: PieceColor) {
        runOnUiThread {
            currentGameState = if (winnerColor == PieceColor.WHITE) {
                GameState.TIMEOUT_BLACK_WIN
            } else {
                GameState.TIMEOUT_WHITE_WIN
            }
            chessClock.stop()
            saveGameToCloud("TIEMPO_AGOTADO")
            updateUi()
            showGameOverDialog("Victoria por tiempo de las ${if (winnerColor == PieceColor.WHITE) "Blancas" else "Negras"}")
        }
    }

    private fun updateUi() {
        val inCheck = currentGameState == GameState.CHECK
        val checkKingPos = if (inCheck) {
            findKingPosition(currentBoardState, currentBoardState.turn)
        } else null

        binding.chessBoardView.updateState(
            state = currentBoardState,
            selectedPos = selectedPosition,
            moves = currentLegalMoves,
            lastAppliedMove = lastAppliedMove,
            inCheck = inCheck,
            checkKingPos = checkKingPos
        )

        // Actualizar reloj activo si la partida está en curso y no pausada
        if (!currentGameState.isGameOver() && !chessClock.isPaused && chessClock.activeColor != currentBoardState.turn) {
            chessClock.startTurn(currentBoardState.turn)
        }

        // Actualizar piezas capturadas
        val (whiteCapturedStr, blackCapturedStr) = formatCapturedPieces(currentBoardState.capturedPieces)
        binding.tvWhiteCaptured.text = whiteCapturedStr
        binding.tvBlackCaptured.text = blackCapturedStr

        // Actualizar marcador y estado
        val scoreText = "Puntaje: ${currentGameState.getScore().displayText}"
        binding.tvGameScore.text = scoreText

        val statusText = when (currentGameState) {
            GameState.ACTIVE -> "Turno de ${if (currentBoardState.turn == PieceColor.WHITE) "Blancas" else "Negras"}"
            GameState.CHECK -> "JAQUE a las ${if (currentBoardState.turn == PieceColor.WHITE) "Blancas" else "Negras"}!"
            GameState.CHECKMATE_WHITE_WIN -> "JAQUE MATE - Ganan Blancas (1 - 0)"
            GameState.CHECKMATE_BLACK_WIN -> "JAQUE MATE - Ganan Negras (0 - 1)"
            GameState.STALEMATE -> "TABLAS por Ahogado (1/2 - 1/2)"
            GameState.TIMEOUT_WHITE_WIN -> "TIEMPO - Ganan Negras (0 - 1)"
            GameState.TIMEOUT_BLACK_WIN -> "TIEMPO - Ganan Blancas (1 - 0)"
            GameState.DRAW_MATERIAL -> "TABLAS por Material Insuficiente (1/2 - 1/2)"
            GameState.DRAW_AGREEMENT -> "TABLAS por Acuerdo (1/2 - 1/2)"
            GameState.RESIGNATION_WHITE_WIN -> "RENDICIÓN - Ganan Blancas (1 - 0)"
            GameState.RESIGNATION_BLACK_WIN -> "RENDICIÓN - Ganan Negras (0 - 1)"
        }
        binding.tvGameStatus.text = statusText
    }

    private fun showTimeConfigDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_time_config, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etMinutes = dialogView.findViewById<EditText>(R.id.et_minutes)
        val etSeconds = dialogView.findViewById<EditText>(R.id.et_seconds)
        val etIncrement = dialogView.findViewById<EditText>(R.id.et_increment)

        dialogView.findViewById<Button>(R.id.btn_preset_3_2).setOnClickListener {
            etMinutes.setText("3")
            etSeconds.setText("0")
            etIncrement.setText("2")
        }

        dialogView.findViewById<Button>(R.id.btn_preset_5_0).setOnClickListener {
            etMinutes.setText("5")
            etSeconds.setText("0")
            etIncrement.setText("0")
        }

        dialogView.findViewById<Button>(R.id.btn_preset_10_5).setOnClickListener {
            etMinutes.setText("10")
            etSeconds.setText("0")
            etIncrement.setText("5")
        }

        dialogView.findViewById<Button>(R.id.btn_cancel_time).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_apply_time).setOnClickListener {
            val min = etMinutes.text.toString().toIntOrNull() ?: 10
            val sec = etSeconds.text.toString().toIntOrNull() ?: 0
            val inc = etIncrement.text.toString().toIntOrNull() ?: 0

            val config = TimeConfig(minutes = min, seconds = sec, incrementSeconds = inc)
            startNewGame(config)
            dialog.dismiss()
            Toast.makeText(this, "Modalidad configurada: ${config.getFormattedPreset()}", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showPawnPromotionDialog(baseMove: Move) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pawn_promotion, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val btnQueen = dialogView.findViewById<ImageButton>(R.id.btn_promote_queen)
        val btnRook = dialogView.findViewById<ImageButton>(R.id.btn_promote_rook)
        val btnBishop = dialogView.findViewById<ImageButton>(R.id.btn_promote_bishop)
        val btnKnight = dialogView.findViewById<ImageButton>(R.id.btn_promote_knight)

        val isWhite = baseMove.piece.color == PieceColor.WHITE
        btnQueen.setImageResource(if (isWhite) R.drawable.ic_chess_wq else R.drawable.ic_chess_bq)
        btnRook.setImageResource(if (isWhite) R.drawable.ic_chess_wr else R.drawable.ic_chess_br)
        btnBishop.setImageResource(if (isWhite) R.drawable.ic_chess_wb else R.drawable.ic_chess_bb)
        btnKnight.setImageResource(if (isWhite) R.drawable.ic_chess_wn else R.drawable.ic_chess_bn)

        val applyPromotion = { promoType: PieceType ->
            val finalMove = baseMove.copy(promotionPieceType = promoType)
            dialog.dismiss()
            executeMove(finalMove)
        }

        btnQueen.setOnClickListener { applyPromotion(PieceType.QUEEN) }
        btnRook.setOnClickListener { applyPromotion(PieceType.ROOK) }
        btnBishop.setOnClickListener { applyPromotion(PieceType.BISHOP) }
        btnKnight.setOnClickListener { applyPromotion(PieceType.KNIGHT) }

        dialog.show()
    }

    private fun showGameOverDialog(titleReason: String) {
        val score = currentGameState.getScore().displayText
        AlertDialog.Builder(this)
            .setTitle("Fin de la Partida")
            .setMessage("$titleReason\n\nPuntaje Final: $score\n\n¿Deseas iniciar una nueva partida?")
            .setPositiveButton("Nueva Partida") { _, _ ->
                startNewGame(currentTimeConfig)
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun showCloudRecordsDialog() {
        val savedGames = cloudGameRepository.getSavedGames()
        if (savedGames.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Registro en la Nube")
                .setMessage("No hay partidas registradas en el almacenamiento de la nube.")
                .setPositiveButton("Aceptar", null)
                .show()
            return
        }

        val sb = StringBuilder()
        for ((index, game) in savedGames.withIndex()) {
            sb.append("${index + 1}. Partida ID: ${game.gameId.take(8)}\n")
            sb.append("   Resultado: ${game.score} (${game.endReason})\n")
            sb.append("   Movimientos: ${game.totalMoves} | Tiempo: ${game.timeConfigMinutes}m+${game.timeConfigIncrement}s\n\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Registro de Posiciones en la Nube (${savedGames.size})")
            .setMessage(sb.toString())
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun saveGameToCloud(endReason: String) {
        val gameRecord = GameRecord(
            gameId = UUID.randomUUID().toString(),
            dateTimestamp = System.currentTimeMillis(),
            timeConfigMinutes = currentTimeConfig.minutes,
            timeConfigSeconds = currentTimeConfig.seconds,
            timeConfigIncrement = currentTimeConfig.incrementSeconds,
            fenHistory = gameHistoryManager.getFenList(),
            movesSan = gameHistoryManager.getSanMoveList(),
            score = currentGameState.getScore().displayText,
            endReason = endReason,
            totalMoves = gameHistoryManager.getMoveRecordList().size
        )

        cloudGameRepository.saveGame(gameRecord) { success, _ ->
            if (success) {
                Toast.makeText(this, "Partida registrada en la nube", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateSanNotation(move: Move): String {
        if (move.moveType == MoveType.CASTLING_KINGSIDE) return "O-O"
        if (move.moveType == MoveType.CASTLING_QUEENSIDE) return "O-O-O"

        val pieceChar = when (move.piece.type) {
            PieceType.PAWN -> ""
            PieceType.KNIGHT -> "N"
            PieceType.BISHOP -> "B"
            PieceType.ROOK -> "R"
            PieceType.QUEEN -> "Q"
            PieceType.KING -> "K"
        }

        val isCapture = move.capturedPiece != null || move.moveType == MoveType.EN_PASSANT
        val captureChar = if (isCapture) {
            if (move.piece.type == PieceType.PAWN) "${('a' + move.from.col)}x" else "x"
        } else ""

        val promoChar = if (move.moveType == MoveType.PROMOTION) {
            "=" + when (move.promotionPieceType) {
                PieceType.QUEEN -> "Q"
                PieceType.ROOK -> "R"
                PieceType.BISHOP -> "B"
                PieceType.KNIGHT -> "N"
                else -> "Q"
            }
        } else ""

        val destSquare = move.to.toAlgebraic()
        return "$pieceChar$captureChar$destSquare$promoChar"
    }

    private fun formatCapturedPieces(capturedPieces: List<ChessPiece>): Pair<String, String> {
        val whiteCaptured = StringBuilder()
        val blackCaptured = StringBuilder()

        for (piece in capturedPieces) {
            val symbol = when (piece.type) {
                PieceType.PAWN -> "♙"
                PieceType.KNIGHT -> "♘"
                PieceType.BISHOP -> "♗"
                PieceType.ROOK -> "♖"
                PieceType.QUEEN -> "♕"
                PieceType.KING -> "♔"
            }
            if (piece.color == PieceColor.WHITE) {
                blackCaptured.append(symbol) // Blancas capturadas por las Negras
            } else {
                whiteCaptured.append(symbol) // Negras capturadas por las Blancas
            }
        }

        return Pair(whiteCaptured.toString(), blackCaptured.toString())
    }

    private fun findKingPosition(state: ChessBoardState, color: PieceColor): Position? {
        for (r in 0..7) {
            for (c in 0..7) {
                val pos = Position(r, c)
                val piece = state.getPiece(pos)
                if (piece != null && piece.type == PieceType.KING && piece.color == color) {
                    return pos
                }
            }
        }
        return null
    }
}
