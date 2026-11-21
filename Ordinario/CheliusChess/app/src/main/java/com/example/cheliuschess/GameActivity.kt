package com.example.cheliuschess

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cheliuschess.cloud.CloudGameRepository
import com.example.cheliuschess.cloud.GameRecord
import com.example.cheliuschess.databinding.ActivityGameBinding
import com.example.cheliuschess.engine.AiDifficulty
import com.example.cheliuschess.engine.ChessAiEngine
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class GameActivity : AppCompatActivity(), ChessBoardView.BoardEventListener, ChessClock.ClockListener {

    private lateinit var binding: ActivityGameBinding
    private lateinit var chessClock: ChessClock
    private lateinit var gameHistoryManager: GameHistoryManager
    private lateinit var cloudGameRepository: CloudGameRepository

    private var currentBoardState: ChessBoardState = ChessBoardState.initial()
    private var selectedPosition: Position? = null
    private var currentLegalMoves: List<Move> = emptyList()
    private var lastAppliedMove: Move? = null
    private var currentGameState: GameState = GameState.ACTIVE
    private var currentTimeConfig: TimeConfig = TimeConfig(minutes = 10, seconds = 0, incrementSeconds = 0)

    // Configuración: VS Computadora
    private var isVsComputer = false
    private var playerColor: PieceColor = PieceColor.WHITE
    private var computerColor: PieceColor = PieceColor.BLACK
    private var aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM
    private var isAiThinking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.gameRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val min = intent.getIntExtra(TimeConfigActivity.EXTRA_MINUTES, 10)
        val sec = intent.getIntExtra(TimeConfigActivity.EXTRA_SECONDS, 0)
        val inc = intent.getIntExtra(TimeConfigActivity.EXTRA_INCREMENT, 0)

        isVsComputer = intent.getBooleanExtra(TimeConfigActivity.EXTRA_VS_COMPUTER, false)
        val rawSide = intent.getStringExtra(TimeConfigActivity.EXTRA_PLAYER_SIDE) ?: "WHITE"
        val rawDiff = intent.getStringExtra(TimeConfigActivity.EXTRA_AI_DIFFICULTY) ?: "MEDIUM"

        aiDifficulty = try {
            AiDifficulty.valueOf(rawDiff)
        } catch (e: Exception) {
            AiDifficulty.MEDIUM
        }

        playerColor = when (rawSide) {
            "BLACK" -> PieceColor.BLACK
            "RANDOM" -> if (Random.nextBoolean()) PieceColor.WHITE else PieceColor.BLACK
            else -> PieceColor.WHITE
        }
        computerColor = playerColor.opposite()

        currentTimeConfig = TimeConfig(minutes = min, seconds = sec, incrementSeconds = inc)

        chessClock = ChessClock(this)
        gameHistoryManager = GameHistoryManager()
        cloudGameRepository = CloudGameRepository(this)

        binding.chessBoardView.boardEventListener = this
        binding.chessBoardView.isFlipped = (playerColor == PieceColor.BLACK)

        setupClickListeners()
        startNewGame(currentTimeConfig)
    }

    private fun setupClickListeners() {
        binding.btnPause.setOnClickListener { togglePauseGame() }
        binding.btnUndo.setOnClickListener { undoMove() }
        binding.btnFlip.setOnClickListener {
            binding.chessBoardView.isFlipped = !binding.chessBoardView.isFlipped
        }
        binding.btnExitMenu.setOnClickListener {
            showExitConfirmationDialog()
        }
    }

    private fun startNewGame(config: TimeConfig) {
        currentTimeConfig = config
        currentBoardState = ChessBoardState.initial()
        selectedPosition = null
        currentLegalMoves = emptyList()
        lastAppliedMove = null
        currentGameState = GameState.ACTIVE
        isAiThinking = false

        chessClock.setup(config)
        gameHistoryManager.reset(
            initialState = currentBoardState,
            initialClock = Pair(config.getTotalMillis(), config.getTotalMillis())
        )

        updateUi()
    }

    override fun onSquareSelected(position: Position) {
        if (currentGameState.isGameOver() || chessClock.isPaused) return
        if (isVsComputer && currentBoardState.turn == computerColor) return // Turno de la IA

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
        if (isVsComputer && currentBoardState.turn == computerColor) return

        if (move.moveType == MoveType.PROMOTION) {
            showPawnPromotionDialog(move)
        } else {
            executeMove(move)
        }
    }

    private fun executeMove(move: Move) {
        val fenBefore = currentBoardState.toFen()
        val nextState = ChessRulesEngine.applyMove(currentBoardState, move)
        val sanNotation = ChessRulesEngine.generateSanNotation(currentBoardState, move, nextState)

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
            showGameOverDialog(currentGameState)
        }

        updateUi()
    }

    private fun triggerAiMoveIfNeeded() {
        if (!isVsComputer || currentGameState.isGameOver() || chessClock.isPaused) return
        if (currentBoardState.turn != computerColor || isAiThinking) return

        isAiThinking = true
        lifecycleScope.launch {
            delay(400L) // Pausa táctica natural de respuesta
            val aiMove = ChessAiEngine.getBestMove(currentBoardState, aiDifficulty)
            isAiThinking = false
            if (aiMove != null && currentBoardState.turn == computerColor && !currentGameState.isGameOver()) {
                executeMove(aiMove)
            }
        }
    }

    private fun undoMove() {
        if (!gameHistoryManager.canUndo()) {
            Toast.makeText(this, "No hay jugadas para revertir", Toast.LENGTH_SHORT).show()
            return
        }

        if (isVsComputer) {
            // Revertir 2 jugadas (la respuesta de la IA y el movimiento del jugador)
            gameHistoryManager.undo()
            if (gameHistoryManager.canUndo() && currentBoardState.turn != playerColor) {
                gameHistoryManager.undo()
            }
            val undoResult = gameHistoryManager.undo() ?: gameHistoryManager.reset(ChessBoardState.initial(), Pair(currentTimeConfig.getTotalMillis(), currentTimeConfig.getTotalMillis())).run { null }
            if (undoResult != null) {
                currentBoardState = undoResult.boardState
                chessClock.setTime(undoResult.whiteMillis, undoResult.blackMillis)
            } else {
                currentBoardState = ChessBoardState.initial()
            }
        } else {
            val undoResult = gameHistoryManager.undo() ?: return
            currentBoardState = undoResult.boardState
            chessClock.setTime(undoResult.whiteMillis, undoResult.blackMillis)
        }

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
            binding.btnPause.setIconResource(android.R.drawable.ic_media_play)
            Toast.makeText(this, "Juego pausado", Toast.LENGTH_SHORT).show()
        } else {
            binding.btnPause.setIconResource(android.R.drawable.ic_media_pause)
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
            showGameOverDialog(currentGameState)
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

        if (!currentGameState.isGameOver() && !chessClock.isPaused && chessClock.activeColor != currentBoardState.turn) {
            chessClock.startTurn(currentBoardState.turn)
        }

        val (whiteCapturedStr, blackCapturedStr) = formatCapturedPieces(currentBoardState.capturedPieces)
        binding.tvWhiteCaptured.text = whiteCapturedStr
        binding.tvBlackCaptured.text = blackCapturedStr

        val scoreText = "Puntaje: ${currentGameState.getScore().displayText}"
        binding.tvGameScore.text = scoreText
        binding.tvMovesSan.text = "SAN: ${gameHistoryManager.getFormattedSanScoreSheet()}"

        val turnName = if (currentBoardState.turn == PieceColor.WHITE) "Blancas" else "Negras"
        val statusText = when (currentGameState) {
            GameState.ACTIVE -> if (isVsComputer && currentBoardState.turn == computerColor) "Turno de Computadora (Pensando...)" else "Turno de $turnName"
            GameState.CHECK -> "JAQUE a las $turnName!"
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

        // Desencadenar movimiento de IA si corresponde
        triggerAiMoveIfNeeded()
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

    private fun showGameOverDialog(state: GameState) {
        val score = state.getScore().displayText
        val reasonText = when (state) {
            GameState.CHECKMATE_WHITE_WIN -> "¡Jaque Mate! Ganan las Blancas"
            GameState.CHECKMATE_BLACK_WIN -> "¡Jaque Mate! Ganan las Negras"
            GameState.TIMEOUT_WHITE_WIN -> "Tiempo Agotado. Ganan las Negras"
            GameState.TIMEOUT_BLACK_WIN -> "Tiempo Agotado. Ganan las Blancas"
            GameState.STALEMATE -> "Tablas por Ahogado"
            GameState.DRAW_MATERIAL -> "Tablas por Material Insuficiente"
            else -> "Fin de la partida"
        }

        AlertDialog.Builder(this)
            .setTitle("Partida Finalizada")
            .setMessage("$reasonText\n\nPuntaje Final: $score\n\n¿Qué deseas hacer a continuación?")
            .setPositiveButton("Nueva Partida") { _, _ ->
                val intent = Intent(this, TimeConfigActivity::class.java).apply {
                    putExtra(TimeConfigActivity.EXTRA_VS_COMPUTER, isVsComputer)
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Menú Principal") { _, _ ->
                finish()
            }
            .show()
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Salir de la Partida")
            .setMessage("¿Estás seguro de salir de la partida actual?")
            .setPositiveButton("Sí, Salir") { _, _ ->
                chessClock.stop()
                finish()
            }
            .setNegativeButton("Cancelar", null)
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

        cloudGameRepository.saveGame(gameRecord) { _, _ -> }
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
                blackCaptured.append(symbol)
            } else {
                whiteCaptured.append(symbol)
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
