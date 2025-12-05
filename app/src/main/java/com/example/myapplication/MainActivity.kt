package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp


data class StrokeData(
    val points: MutableList<Offset>,
    val color: Color,
    val width: Float
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DrawingScreen()
                }
            }
        }
    }
}

@Composable
fun DrawingScreen() {
    // All strokes drawn so far
    val strokes = remember { mutableStateListOf<StrokeData>() }

    // Current brush settings
    var brushColor by remember { mutableStateOf(Color.Black) }
    var brushSize by remember { mutableStateOf(10f) }

    Column {
        ToolPanel(
            brushColor = brushColor,
            onColorChange = { brushColor = it },
            brushSize = brushSize,
            onBrushSizeChange = { brushSize = it },
            onClear = { strokes.clear() },
            onUndo = {
                if (strokes.isNotEmpty()) {
                    strokes.removeAt(strokes.lastIndex) // <-- instead of removeLast()
                }
            }
        )

        DrawingCanvas(
            strokes = strokes,
            currentColor = brushColor,
            currentWidth = brushSize
        )
    }
}

@Composable
fun ToolPanel(
    brushColor: Color,
    onColorChange: (Color) -> Unit,
    brushSize: Float,
    onBrushSizeChange: (Float) -> Unit,
    onClear: () -> Unit,
    onUndo: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(Color(0xFFEFEFEF))
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Text("Brush Size: ${brushSize.toInt()}")

        Slider(
            value = brushSize,
            onValueChange = onBrushSizeChange,
            valueRange = 5f..50f
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Color palette
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            val colors = listOf(Color.Black, Color.Red, Color.Blue, Color.Green)
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color, CircleShape)
                        .border(
                            width = if (brushColor == color) 3.dp else 1.dp,
                            color = if (brushColor == color) Color.DarkGray else Color.LightGray,
                            shape = CircleShape
                        )
                        .clickable { onColorChange(color) } // <-- clickable now valid
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = onUndo) { Text("Undo") }
            Button(onClick = onClear) { Text("Clear") }
        }
    }
}

@Composable
fun DrawingCanvas(
    strokes: MutableList<StrokeData>,
    currentColor: Color,
    currentWidth: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(currentColor, currentWidth) {
                // Handle drag gestures to draw
                detectDragGestures(
                    onDragStart = { offset ->
                        // Start a new stroke with the first point
                        strokes.add(
                            StrokeData(
                                points = mutableListOf(offset),
                                color = currentColor,
                                width = currentWidth
                            )
                        )
                    },
                    onDrag = { change, _ ->
                        // Add points as the user drags
                        strokes.last().points.add(change.position)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Redraw all strokes from state
            for (stroke in strokes) {
                val path = Path()
                val pts = stroke.points

                if (pts.isNotEmpty()) {
                    path.moveTo(pts.first().x, pts.first().y)
                }
                for (p in pts.drop(1)) {
                    path.lineTo(p.x, p.y)
                }

                drawPath(
                    path = path,
                    color = stroke.color,
                    style = Stroke(
                        width = stroke.width,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}
