package com.qsemyon.tetris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val BackgroundColor = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val EmptyCellColor = Color(0xFF2C2C2C)
private val BorderColor = Color(0xFF333333)
private val ButtonColor = Color(0xFF444444)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TetrisAppTheme { TetrisScreen() } }
    }
}

@Composable
private fun TetrisAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content
    )
}

@Composable
private fun TetrisScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundColor
    ) {
        TetrisUI()
    }
}

@Composable
fun TetrisUI(vm: TetrisViewModel = viewModel()) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundColor
    ) { padding ->
        TetrisLayout(vm, padding)
    }
}

@Composable
private fun TetrisLayout(vm: TetrisViewModel, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderSection(vm.score)

        GameFieldContainer(Modifier.weight(1f)) {
            GameGrid(vm)
        }

        Spacer(modifier = Modifier.height(20.dp))

        TetrisControls(vm)
    }
}

@Composable
private fun GameFieldContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(0.5f)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
            .border(2.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        content()
    }
}

@Composable
private fun TetrisControls(vm: TetrisViewModel) {
    ControlPanel(
        onRotate = { vm.rotate() },
        onMoveLeft = { vm.moveSide(-1) },
        onMoveRight = { vm.moveSide(1) },
        onHardDrop = { vm.hardDrop() }
    )
}

@Composable
fun HeaderSection(score: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().height(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("TETRIS", fontSize = 28.sp, color = Color.White)
        Text("SCORE: $score", fontSize = 16.sp, color = Color.Gray)
    }
}

@Composable
fun GameGrid(vm: TetrisViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val size = this.maxWidth / GameConfig.COLS

        for (i in 0 until GameConfig.ROWS * GameConfig.COLS) {
            val r = i / GameConfig.COLS
            val c = i % GameConfig.COLS
            val p = vm.currentPiece
            val isPiece = p?.getRelativeCoords()?.any { it.first + p.y == r && it.second + p.x == c } == true
            val color = p?.takeIf { isPiece }?.type?.color ?: vm.grid[r][c]?.color ?: EmptyCellColor
            val cellMod = Modifier
                .size(size)
                .offset(size * c, size * r)
                .padding(1.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)

            Box(cellMod)
        }
    }
}

@Composable
fun ControlPanel(
    onRotate: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onHardDrop: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameButton("↻", onClick = onRotate, modifier = Modifier.size(65.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GameButton("←", onClick = onMoveLeft, modifier = Modifier.weight(1f).height(60.dp))
            GameButton("⤓", onClick = onHardDrop, modifier = Modifier.weight(1f).height(60.dp))
            GameButton("→", onClick = onMoveRight, modifier = Modifier.weight(1f).height(60.dp))
        }
    }
}

@Composable
fun GameButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ButtonColor, contentColor = Color.White),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(label, fontSize = 28.sp)
    }
}