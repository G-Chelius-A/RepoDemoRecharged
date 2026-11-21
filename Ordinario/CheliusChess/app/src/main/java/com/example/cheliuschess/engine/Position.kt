package com.example.cheliuschess.engine

data class Position(
    val row: Int, // 0..7 (0 = fila 1, 7 = fila 8)
    val col: Int  // 0..7 (0 = a, 7 = h)
) {
    fun isValid(): Boolean = row in 0..7 && col in 0..7

    fun toAlgebraic(): String {
        val file = ('a' + col)
        val rank = (row + 1)
        return "$file$rank"
    }

    companion object {
        fun fromAlgebraic(algebraic: String): Position? {
            if (algebraic.length != 2) return null
            val col = algebraic[0] - 'a'
            val row = algebraic[1] - '1'
            val pos = Position(row, col)
            return if (pos.isValid()) pos else null
        }
    }
}
