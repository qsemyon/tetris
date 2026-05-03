package com.qsemyon.tetris

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object GameConfig {
    const val ROWS = 20
    const val COLS = 10
    const val STEP_DELAY = 700L
    const val LINE_SCORE = 100
}

class TetrisViewModel : ViewModel() {

    private var lockJob: Job? = null
    private var gameJob: Job? = null

    private var isLocking = false

    var grid by mutableStateOf(List(GameConfig.ROWS) { List<Tetromino?>(GameConfig.COLS) { null } })
        private set

    var currentPiece by mutableStateOf<ActivePiece?>(null)
        private set

    var score by mutableIntStateOf(0)
        private set

    var level by mutableIntStateOf(1)
        private set

    private val gameSpeed: Long
        get() = (GameConfig.STEP_DELAY - (score / 500) * 55).coerceAtLeast(60L)

    private var currentDelay by mutableLongStateOf(GameConfig.STEP_DELAY)

    init {
        resetGame()
    }

    fun resetGame() {
        gameJob?.cancel()
        grid = List(GameConfig.ROWS) { List(GameConfig.COLS) { null } }
        score = 0
        level = 1
        currentDelay = GameConfig.STEP_DELAY
        spawnPiece()
        startGameLoop()
    }

    private fun spawnPiece() {
        val startX = GameConfig.COLS / 2 - 1
        currentPiece = ActivePiece(Tetromino.entries.random(), startX, 0)
        isLocking = false
        if (!canMove(startX, 0, 0)) resetGame()
    }

    private fun startGameLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (true) {
                // Теперь gameSpeed используется, и варнинг исчезнет
                delay(gameSpeed)
                moveDown()
            }
        }
    }

    fun moveDown() {
        val p = currentPiece ?: return
        if (canMove(p.x, p.y + 1, p.rotation)) {
            currentPiece = p.copy(y = p.y + 1)
            lockJob?.cancel()
            isLocking = false
        } else if (!isLocking) {
            isLocking = true
            lockJob = viewModelScope.launch {
                delay(500)
                val currentP = currentPiece
                val cannotMove = currentP != null && !canMove(currentP.x, currentP.y + 1, currentP.rotation)
                if (cannotMove) lockPiece() else isLocking = false
            }
        }
    }

    fun moveSide(dx: Int) {
        val p = currentPiece ?: return
        if (canMove(p.x + dx, p.y, p.rotation)) {
            currentPiece = p.copy(x = p.x + dx)
        }
    }

    fun rotate() {
        val p = currentPiece ?: return
        if (canMove(p.x, p.y, p.rotation + 1)) {
            currentPiece = p.copy(rotation = p.rotation + 1)
        }
    }

    fun hardDrop() {
        val p = currentPiece ?: return
        var targetY = p.y
        while (canMove(p.x, targetY + 1, p.rotation)) targetY++
        currentPiece = p.copy(y = targetY)

        moveDown()
    }

    private fun canMove(nx: Int, ny: Int, nr: Int): Boolean {
        val tempPiece = currentPiece?.copy(x = nx, y = ny, rotation = nr) ?: return false
        return tempPiece.getRelativeCoords().all { (dr, dc) ->
            val r = ny + dr
            val c = nx + dc
            r in 0 until GameConfig.ROWS && c in 0 until GameConfig.COLS && grid[r][c] == null
        }
    }

    private fun lockPiece() {
        lockJob?.cancel()
        isLocking = false
        val p = currentPiece ?: return
        val newGrid = grid.map { it.toMutableList() }
        p.getRelativeCoords().forEach { (dr, dc) ->
            val r = p.y + dr
            val c = p.x + dc
            if (r in 0 until GameConfig.ROWS && c in 0 until GameConfig.COLS) newGrid[r][c] = p.type
        }
        grid = newGrid.map { it.toList() }
        clearLines()
        spawnPiece()
    }

    private fun clearLines() {
        val filteredGrid = grid.filter { row -> row.any { it == null } }
        val clearedRows = GameConfig.ROWS - filteredGrid.size
        if (clearedRows > 0) {
            val newRows = List(clearedRows) { List<Tetromino?>(GameConfig.COLS) { null } }
            grid = newRows + filteredGrid
            score += clearedRows * GameConfig.LINE_SCORE

            level = (score / 1000) + 1
        }
    }
}