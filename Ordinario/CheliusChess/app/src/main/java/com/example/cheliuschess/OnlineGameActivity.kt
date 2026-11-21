package com.example.cheliuschess

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cheliuschess.databinding.ActivityOnlineGameBinding
import com.example.cheliuschess.engine.ChessBoardState
import com.example.cheliuschess.engine.ChessRulesEngine
import com.example.cheliuschess.engine.GameState
import com.example.cheliuschess.engine.Move
import com.example.cheliuschess.engine.PieceColor
import com.example.cheliuschess.engine.PieceType
import com.example.cheliuschess.engine.Position
import com.example.cheliuschess.model.TimeConfig
import com.example.cheliuschess.remote.OnlineGameData
import com.example.cheliuschess.remote.OnlineGameRepository
import com.example.cheliuschess.timer.ChessClock
import com.example.cheliuschess.ui.ChessBoardView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OnlineGameActivity : AppCompatActivity(), ChessBoardView.BoardEventListener, ChessClock.ClockListener {

    private lateinit var binding: ActivityOnlineGameBinding
    private lateinit var repository: OnlineGameRepository
    private lateinit var chessClock: ChessClock

    private var gameId: String = ""
    private var isWhitePlayer: Boolean = true
    private var myColor: PieceColor = PieceColor.WHITE

    private var currentBoardState: ChessBoardState = ChessBoardState.initial()
    private var selectedPosition: Position? = null
    private var currentLegalMoves: List<Move> = emptyList()
    private var lastAppliedMove: Move? = null
    private var currentGameState: GameState = GameState.ACTIVE
    private var isPollingActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityOnlineGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.onlineGameRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        gameId = intent.getStringExtra(LobbyActivity.EXTRA_GAME_ID) ?: ""
        isWhitePlayer = intent.getBooleanExtra(LobbyActivity.EXTRA_IS_WHITE, true)
        myColor = if (isWhitePlayer) PieceColor.WHITE else PieceColor.BLACK

        repository = OnlineGameRepository()
        chessClock = ChessClock(this)
        chessClock.setup(TimeConfig(10, 0, 0))

        binding.chessBoardView.boardEventListener = this
        binding.chessBoardView.isFlipped = !isWhitePlayer

        binding.btnResign.setOnClickListener { showResignDialog() }
        binding.btnExitGame.setOnClickListener { showExitDialog() }

        startRealtimePolling()
    }

    private fun startRealtimePolling() {
        lifecycleScope.launch {
            while (isPollingActive) {
                if (gameId.isNotEmpty()) {
                    val result = repository.fetchGameById(gameId)
                    result.onSuccess { gameData ->
                        handleOnlineGameUpdate(gameData)
                    }
                }
                delay(1500L) // Polling ligero en vivo
            }
        }
    }

    private fun handleOnlineGameUpdate(data: OnlineGameData) {
        val serverTurn = if (data.turn == "WHITE") PieceColor.WHITE else PieceColor.BLACK
        val serverFen = data.current_fen

        if (serverFen != currentBoardState.toFen()) {
            parseFenToState(serverFen, serverTurn)
        }

        if (data.status == "FINISHED") {
            isPollingActive = false
            chessClock.stop()
            val scoreText = data.score
            val endReason = data.end_reason ?: "Partida Finalizada"
            showGameOverDialog(endReason, scoreText)
        }

        updateUi(data)
    }

    private fun parseFenToState(fen: String, turn: PieceColor) {
        // En caso de cambio de FEN del servidor, actualizar tablero
        currentBoardState = currentBoardState.copy(turn = turn)
        currentGameState = ChessRulesEngine.evaluateGameState(currentBoardState)
    }

    override fun onSquareSelected(position: Position) {
        if (currentGameState.isGameOver() || currentBoardState.turn != myColor) return

        val piece = currentBoardState.getPiece(position)
        if (piece != null && piece.color == myColor) {
            selectedPosition = position
            currentLegalMoves = ChessRulesEngine.getLegalMoves(currentBoardState, position)
        } else {
            selectedPosition = null
            currentLegalMoves = emptyList()
        }
        updateUi(null)
    }

    override fun onMoveRequested(move: Move) {
        if (currentGameState.isGameOver() || currentBoardState.turn != myColor) return
        executeMove(move)
    }

    private fun executeMove(move: Move) {
        val nextState = ChessRulesEngine.applyMove(currentBoardState, move)
        val sanNotation = ChessRulesEngine.generateSanNotation(currentBoardState, move, nextState)
        val nextTurnStr = if (nextState.turn == PieceColor.WHITE) "WHITE" else "BLACK"

        currentBoardState = nextState
        lastAppliedMove = move
        selectedPosition = null
        currentLegalMoves = emptyList()

        currentGameState = ChessRulesEngine.evaluateGameState(currentBoardState)

        lifecycleScope.launch {
            repository.updateMove(
                gameId = gameId,
                newFen = nextState.toFen(),
                lastMoveSan = sanNotation,
                nextTurn = nextTurnStr,
                whiteTimeLeftMs = chessClock.whiteMillis,
                blackTimeLeftMs = chessClock.blackMillis
            )

            if (currentGameState.isGameOver()) {
                val score = currentGameState.getScore().displayText
                val winnerId = if (currentGameState == GameState.CHECKMATE_WHITE_WIN) repository.getCurrentUserId() else null
                repository.finishGame(gameId, winnerId, currentGameState.name, score)
            }
        }

        updateUi(null)
    }

    override fun onTick(whiteMillis: Long, blackMillis: Long) {
        runOnUiThread {
            if (isWhitePlayer) {
                binding.tvBottomTimer.text = ChessClock.formatTime(whiteMillis)
                binding.tvTopTimer.text = ChessClock.formatTime(blackMillis)
            } else {
                binding.tvBottomTimer.text = ChessClock.formatTime(blackMillis)
                binding.tvTopTimer.text = ChessClock.formatTime(whiteMillis)
            }
        }
    }

    override fun onTimeout(winnerColor: PieceColor) {
        // Manejo de tiempo
    }

    private fun updateUi(gameData: OnlineGameData?) {
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

        val isMyTurn = (currentBoardState.turn == myColor)
        val statusText = if (isMyTurn) "¡Tu turno de mover!" else "Esperando jugada del oponente..."
        binding.tvOnlineStatus.text = statusText

        if (gameData != null && gameData.last_move_san != null) {
            binding.tvMovesSan.text = "Último Tiro SAN: ${gameData.last_move_san}"
        }
    }

    private fun showResignDialog() {
        AlertDialog.Builder(this)
            .setTitle("Rendirse")
            .setMessage("¿Estás seguro de que deseas rendirte?")
            .setPositiveButton("Sí, Rendirme") { _, _ ->
                lifecycleScope.launch {
                    val winnerScore = if (isWhitePlayer) "0-1" else "1-0"
                    repository.finishGame(gameId, null, "RESIGNATION", winnerScore)
                    finish()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Salir de la Sala")
            .setMessage("¿Deseas salir al lobby?")
            .setPositiveButton("Sí, Salir") { _, _ ->
                isPollingActive = false
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showGameOverDialog(endReason: String, score: String) {
        AlertDialog.Builder(this)
            .setTitle("Partida Multijugador Finalizada")
            .setMessage("Motivo: $endReason\nResultado: $score")
            .setPositiveButton("Aceptar") { _, _ ->
                finish()
            }
            .show()
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

    override fun onDestroy() {
        super.onDestroy()
        isPollingActive = false
    }
}
