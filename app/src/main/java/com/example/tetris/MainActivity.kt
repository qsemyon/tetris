package com.example.tetris

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    TetrisUI()
                }
            }
        }
    }
}

@Composable
fun TetrisUI(vm: TetrisViewModel = viewModel()) {
    val btnColor = Color(0xFF444444)
    val textColor = Color.White

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TETRIS", fontSize = 28.sp, color = Color.White)
                    Text("SCORE: ${vm.score}", fontSize = 16.sp, color = Color.Gray)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.5f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(2.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val cellSize = this.maxWidth / 10
                    Column {
                        for (r in 0 until 20) {
                            Row {
                                for (c in 0 until 10) {
                                    val activeColor = vm.currentPiece?.let { p ->
                                        if (p.getRelativeCoords().any { (dr, dc) -> p.y + dr == r && p.x + dc == c })
                                            p.type.color else null
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(cellSize)
                                            .padding(1.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(activeColor ?: vm.grid[r][c])
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Кнопка ПОВОРОТ
                Button(
                    onClick = { vm.rotate() },
                    modifier = Modifier.size(65.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = btnColor, contentColor = textColor),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("↻", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val controls = listOf("←", "⤓", "→")
                    controls.forEachIndexed { index, label ->
                        Button(
                            onClick = {
                                when(label) {
                                    "←" -> vm.moveSide(-1)
                                    "⤓" -> vm.hardDrop()
                                    "→" -> vm.moveSide(1)
                                }
                            },
                            modifier = Modifier.weight(1f).height(60.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = btnColor, contentColor = textColor),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(label, fontSize = 28.sp)
                        }
                        if (index < 2) Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }
        }
    }
}