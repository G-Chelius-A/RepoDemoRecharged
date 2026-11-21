package com.example.cheliuschess.timer

import android.os.Handler
import android.os.Looper
import com.example.cheliuschess.engine.PieceColor
import com.example.cheliuschess.model.TimeConfig

class ChessClock(
    private val listener: ClockListener
) {
    interface ClockListener {
        fun onTick(whiteMillis: Long, blackMillis: Long)
        fun onTimeout(winnerColor: PieceColor)
    }

    var whiteMillis: Long = 600000L
        private set
    var blackMillis: Long = 600000L
        private set

    var incrementMillis: Long = 0L
        private set

    var activeColor: PieceColor? = null
        private set

    var isPaused: Boolean = false
        private set

    private val handler = Handler(Looper.getMainLooper())
    private var lastTickTime: Long = 0L

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (isPaused || activeColor == null) return

            val now = System.currentTimeMillis()
            val delta = now - lastTickTime
            lastTickTime = now

            when (activeColor) {
                PieceColor.WHITE -> {
                    whiteMillis -= delta
                    if (whiteMillis <= 0L) {
                        whiteMillis = 0L
                        listener.onTick(whiteMillis, blackMillis)
                        listener.onTimeout(PieceColor.BLACK)
                        stop()
                        return
                    }
                }
                PieceColor.BLACK -> {
                    blackMillis -= delta
                    if (blackMillis <= 0L) {
                        blackMillis = 0L
                        listener.onTick(whiteMillis, blackMillis)
                        listener.onTimeout(PieceColor.WHITE)
                        stop()
                        return
                    }
                }
                null -> return
            }

            listener.onTick(whiteMillis, blackMillis)
            handler.postDelayed(this, 100L)
        }
    }

    fun setup(config: TimeConfig) {
        stop()
        val total = config.getTotalMillis()
        whiteMillis = total
        blackMillis = total
        incrementMillis = config.getIncrementMillis()
        activeColor = null
        isPaused = false
        listener.onTick(whiteMillis, blackMillis)
    }

    fun startTurn(color: PieceColor) {
        if (isPaused) return
        activeColor = color
        lastTickTime = System.currentTimeMillis()
        handler.removeCallbacks(tickRunnable)
        handler.post(tickRunnable)
    }

    fun onMoveFinished(completedTurn: PieceColor) {
        if (completedTurn == PieceColor.WHITE) {
            whiteMillis += incrementMillis
            activeColor = PieceColor.BLACK
        } else {
            blackMillis += incrementMillis
            activeColor = PieceColor.WHITE
        }
        listener.onTick(whiteMillis, blackMillis)
        lastTickTime = System.currentTimeMillis()
    }

    fun pause() {
        if (isPaused) return
        isPaused = true
        handler.removeCallbacks(tickRunnable)
    }

    fun resume() {
        if (!isPaused || activeColor == null) return
        isPaused = false
        lastTickTime = System.currentTimeMillis()
        handler.post(tickRunnable)
    }

    fun togglePause(): Boolean {
        if (isPaused) {
            resume()
        } else {
            pause()
        }
        return isPaused
    }

    fun stop() {
        activeColor = null
        isPaused = false
        handler.removeCallbacks(tickRunnable)
    }

    fun setTime(white: Long, black: Long) {
        whiteMillis = white
        blackMillis = black
        listener.onTick(whiteMillis, blackMillis)
    }

    companion object {
        fun formatTime(millis: Long): String {
            val totalSeconds = (millis + 999) / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
