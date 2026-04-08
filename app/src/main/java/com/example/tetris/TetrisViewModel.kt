package com.example.tetris

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TetrisViewModel : ViewModel() {
    private val rows = 20
    private val cols = 10
    private var gameJob: Job? = null

    var grid by mutableStateOf(List(rows) { List(cols) { Color(0xFF2C2C2C) } })
    var currentPiece by mutableStateOf<ActivePiece?>(null)
    var score by mutableStateOf(0)

    init {
        resetGame()
    }

    fun resetGame() {
        gameJob?.cancel()
        grid = List(rows) { List(cols) { Color(0xFF2C2C2C) } }
        score = 0
        spawnPiece()
        gameLoop()
    }

    private fun spawnPiece() {
        currentPiece = ActivePiece(Tetromino.entries.random(), cols / 2 - 1, 0)
        if (!canMove(currentPiece!!.x, currentPiece!!.y, 0)) resetGame()
    }

    private fun gameLoop() {
        gameJob = viewModelScope.launch {
            while (true) {
                delay(700)
                moveDown()
            }
        }
    }

    fun moveDown() {
        val p = currentPiece ?: return
        if (canMove(p.x, p.y + 1, p.rotation)) {
            currentPiece = p.copy(y = p.y + 1)
        } else {
            lockPiece()
        }
    }

    fun moveSide(dx: Int) {
        val p = currentPiece ?: return
        if (canMove(p.x + dx, p.y, p.rotation)) currentPiece = p.copy(x = p.x + dx)
    }

    fun rotate() {
        val p = currentPiece ?: return
        if (canMove(p.x, p.y, p.rotation + 1)) currentPiece = p.copy(rotation = p.rotation + 1)
    }

    private fun canMove(nx: Int, ny: Int, nr: Int): Boolean {
        val tempPiece = currentPiece?.copy(x = nx, y = ny, rotation = nr) ?: return false
        return tempPiece.getRelativeCoords().all { (dr, dc) ->
            val r = ny + dr
            val c = nx + dc
            r in 0 until rows && c in 0 until cols && grid[r][c] == Color(0xFF2C2C2C)
        }
    }

    private fun lockPiece() {
        val p = currentPiece ?: return
        val newGrid = grid.map { it.toMutableList() }
        p.getRelativeCoords().forEach { (dr, dc) ->
            val r = p.y + dr
            val c = p.x + dc
            if (r in 0 until rows && c in 0 until cols) newGrid[r][c] = p.type.color
        }
        grid = newGrid.map { it.toList() }
        clearLines()
        spawnPiece()
    }

    private fun clearLines() {
        val newGrid = grid.filter { row -> row.any { it == Color(0xFF2C2C2C) } }.toMutableList()
        val cleared = rows - newGrid.size
        repeat(cleared) {
            newGrid.add(0, List(cols) { Color(0xFF2C2C2C) })
            score += 100
        }
        grid = newGrid
    }

    fun hardDrop() {
        val p = currentPiece ?: return
        var targetY = p.y
        while (canMove(p.x, targetY + 1, p.rotation)) {
            targetY++
        }
        currentPiece = p.copy(y = targetY)
        lockPiece()
    }
}