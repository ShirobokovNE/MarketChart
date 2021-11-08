import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.shirobokov.marketchart.chart.Candle
import ru.shirobokov.marketchart.chart.MarketChartState
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MarketChart(candles: List<Candle>) {

    val state = rememberSaveable(saver = MarketChartState.Saver) { MarketChartState.getState(candles) }

    val decimalFormat = DecimalFormat("##.00")
    val timeFormatter = DateTimeFormatter.ofPattern("dd.MM, HH:mm")
    val bounds = Rect()
    val textPaint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = 35.sp.value
        color = Color.White.toArgb()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val chartWidth = constraints.maxWidth - 128.dp.value
        val chartHeight = constraints.maxHeight - 64.dp.value

        state.setViewSize(chartWidth, chartHeight)
        state.calculateGridWidth()

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF182028))
                .scrollable(state.scrollableState, Orientation.Horizontal)
                .transformable(state.transformableState)
        ) {
            drawLine(
                color = Color.White,
                strokeWidth = 2.dp.value,
                start = Offset(0f, chartHeight),
                end = Offset(chartWidth, chartHeight)
            )

            drawLine(
                color = Color.White,
                strokeWidth = 2.dp.value,
                start = Offset(chartWidth, 0f),
                end = Offset(chartWidth, chartHeight)
            )

            state.timeLines.forEach { candle ->
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
                    val text = candle.time.format(timeFormatter)
                    textPaint.getTextBounds(text, 0, text.length, bounds)
                    val textHeight = bounds.height()
                    val textWidth = bounds.width()
                    it.nativeCanvas.drawText(
                        text,
                        offset - textWidth / 2,
                        chartHeight + 8.dp.value + textHeight,
                        textPaint
                    )
                }
            }

            state.priceLines.forEach { value: Float ->
                val yOffset = state.yOffset(value)
                val text = decimalFormat.format(value)
                drawLine(
                    color = Color.White,
                    strokeWidth = 1.dp.value,
                    start = Offset(0f, yOffset),
                    end = Offset(chartWidth, yOffset),
                    pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(10f, 20f), phase = 5f)
                )
                drawIntoCanvas {
                    textPaint.getTextBounds(text, 0, text.length, bounds)
                    val textHeight = bounds.height()
                    it.nativeCanvas.drawText(
                        text,
                        chartWidth + 8.dp.value,
                        yOffset + textHeight / 2,
                        textPaint
                    )
                }
            }

            state.visibleCandles.forEach { candle ->
                val xOffset = state.xOffset(candle)
                drawLine(
                    color = Color.White,
                    strokeWidth = 2.dp.value,
                    start = Offset(xOffset, state.yOffset(candle.low)),
                    end = Offset(xOffset, state.yOffset(candle.high))
                )
                if (candle.open > candle.close) {
                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(xOffset - 6.dp.value, state.yOffset(candle.open)),
                        size = Size(12.dp.value, state.yOffset(candle.close) - state.yOffset(candle.open))
                    )
                } else {
                    drawRect(
                        color = Color.Green,
                        topLeft = Offset(xOffset - 6.dp.value, state.yOffset(candle.close)),
                        size = Size(12.dp.value, state.yOffset(candle.open) - state.yOffset(candle.close))
                    )
                }
            }
        }
    }
}

@Preview(device = Devices.PIXEL_2)
@Composable
fun MarketChartPreview() {
    MarketChart(
        listOf(
            Candle(LocalDateTime.now().plusHours(21), 16f, 20f, 22f, 15f),
            Candle(LocalDateTime.now().plusHours(20), 11f, 16f, 16f, 10f),
            Candle(LocalDateTime.now().plusHours(19), 8f, 10f, 11f, 7f),
            Candle(LocalDateTime.now().plusHours(18), 6f, 8f, 9f, 5f),
            Candle(LocalDateTime.now().plusHours(17), 10f, 6f, 11f, 6f),
            Candle(LocalDateTime.now().plusHours(16), 14f, 10f, 15f, 10f),
            Candle(LocalDateTime.now().plusHours(15), 12f, 14f, 15f, 10f),
            Candle(LocalDateTime.now().plusHours(14), 10f, 12f, 13f, 10f),
            Candle(LocalDateTime.now().plusHours(13), 15f, 10f, 16f, 9f),
            Candle(LocalDateTime.now().plusHours(12), 14f, 15f, 15f, 12f),
            Candle(LocalDateTime.now().plusHours(11), 14f, 14f, 15f, 10f),
            Candle(LocalDateTime.now().plusHours(10), 12f, 14f, 15f, 10f),
            Candle(LocalDateTime.now().plusHours(9), 10f, 12f, 13f, 10f),
            Candle(LocalDateTime.now().plusHours(8), 11f, 10f, 16f, 9f),
            Candle(LocalDateTime.now().plusHours(7), 10f, 11f, 12f, 10f),
            Candle(LocalDateTime.now().plusHours(6), 6f, 10f, 12f, 4f),
            Candle(LocalDateTime.now().plusHours(5), 4f, 6f, 7f, 4f),
            Candle(LocalDateTime.now().plusHours(4), 6f, 4f, 8f, 4f),
            Candle(LocalDateTime.now().plusHours(3), 3f, 6f, 6f, 2f),
            Candle(LocalDateTime.now().plusHours(2), 4f, 3f, 5f, 2f),
            Candle(LocalDateTime.now().plusHours(1), 2f, 4f, 6f, 1f)
        ).sorted()
    )
}
