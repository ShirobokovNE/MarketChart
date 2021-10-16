package ru.shirobokov.marketchart

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter

class ChartActivity : AppCompatActivity() {

    private val viewModel: ChartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MarketChart(viewModel.chartState)
        }
    }
}

@Composable
fun MarketChart(state: MarketChartState) {

    val zoomState = rememberTransformableState { zoomChange, _, _ -> state.scaleView(zoomChange) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(0xFF182028))
            .scrollable(state.scrollableState, Orientation.Horizontal)
            .transformable(zoomState)
    ) {

        val chartWidth = constraints.maxWidth - 128.dp.value
        val chartHeight = constraints.maxHeight - 64.dp.value

        state.setViewSize(chartWidth, chartHeight)
        state.calculateGridWidth()

        Canvas(modifier = Modifier.fillMaxSize()) {
            val textPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textSize = 35f
                color = Color.White.toArgb()
            }

            state.timeLines.value.forEach { candle ->
                val offset = state.xOffset(candle)
                if (offset !in 0f..chartWidth) return@forEach
                drawLine(
                    color = Color.White,
                    strokeWidth = 1.dp.value,
                    start = Offset(offset, 0f),
                    end = Offset(offset, chartHeight),
                    pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(10f, 20f), phase = 5f)
                )
                drawIntoCanvas {
                    val text = candle.time.format(DateTimeFormatter.ofPattern("dd.MM, HH:mm"))
                    val textWidth = textPaint.measureText(text)
                    it.nativeCanvas.drawText(
                        text,
                        offset - textWidth / 2,
                        chartHeight + 40.dp.value,
                        textPaint
                    )
                }
            }

            state.priceLines.forEachIndexed { index: Int, value: String ->
                if (index > 0) {
                    val offsetPercent = chartHeight / state.priceLines.size * index
                    drawLine(
                        color = Color.White,
                        strokeWidth = 1.dp.value,
                        start = Offset(0f, offsetPercent),
                        end = Offset(chartWidth, offsetPercent),
                        pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(10f, 20f), phase = 5f)
                    )
                    drawIntoCanvas {
                        it.nativeCanvas.drawText(
                            value,
                            chartWidth + 8.dp.value,
                            offsetPercent + 35f / 2,
                            textPaint
                        )
                    }
                }
            }

            drawLine(
                color = Color.White,
                strokeWidth = 2.dp.value,
                start = Offset(0f, chartHeight),
                end = Offset(size.width, chartHeight)
            )

            drawLine(
                color = Color.White,
                strokeWidth = 2.dp.value,
                start = Offset(chartWidth, 0f),
                end = Offset(chartWidth, size.height)
            )

            state.visibleCandles.forEach { candle ->
                val xOffset = state.xOffset(candle)
                drawLine(
                    color = Color.White,
                    strokeWidth = 2.dp.value,
                    start = Offset(xOffset, state.yOffset(candle.low)),
                    end = Offset(xOffset, state.yOffset(candle.high))
                )
                drawRect(
                    color = if (candle.open > candle.close) Color.Red else Color.Green,
                    topLeft = Offset(xOffset - 6.dp.value, state.yOffset(candle.open)),
                    size = Size(12.dp.value, state.yOffset(candle.close) - state.yOffset(candle.open))
                )
            }
        }
    }
}
