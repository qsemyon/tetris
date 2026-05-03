package com.qsemyon.tetris

import androidx.compose.ui.graphics.Color

enum class Tetromino(val shape: List<Pair<Int, Int>>, val color: Color) {
    I(listOf(0 to -1, 0 to 0, 0 to 1, 0 to 2), Color(0xFF00E5FF)),
    O(listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1), Color(0xFFFFEB3B)),
    T(listOf(0 to 0, 0 to 1, 0 to -1, 1 to 0), Color(0xFF9C27B0)),
    S(listOf(0 to 0, 0 to 1, 1 to -1, 1 to 0), Color(0xFF4CAF50)),
    Z(listOf(0 to -1, 0 to 0, 1 to 0, 1 to 1), Color(0xFFF44336)),
    J(listOf(0 to -1, 0 to 0, 0 to 1, 1 to 1), Color(0xFF2196F3)),
    L(listOf(0 to -1, 0 to 0, 0 to 1, 1 to -1), Color(0xFFFF9800))
}

data class ActivePiece(
    val type: Tetromino,
    val x: Int,
    val y: Int,
    val rotation: Int = 0
) {
    fun getRelativeCoords(): List<Pair<Int, Int>> {
        if (type == Tetromino.O) return type.shape
        var coords = type.shape
        repeat(rotation % 4) {
            coords = coords.map { (r, c) -> c to -r }
        }
        return coords
    }
}