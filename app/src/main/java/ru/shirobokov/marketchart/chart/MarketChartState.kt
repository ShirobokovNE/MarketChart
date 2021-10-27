package ru.shirobokov.marketchart.chart

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat
import kotlin.math.roundToInt

class MarketChartState {

    private val candles = mutableStateOf(listOf<Candle>())

    private var viewWidth = 0f
    private var viewHeight = 0f

    private val decimalFormat = DecimalFormat("##.00")

    private val visibleCandleCount = mutableStateOf(START_CANDLES)
    private val scrollOffset = mutableStateOf(UNINITIALIZED_SCROLL_OFFSET)
    private var candleInGrid = Float.MAX_VALUE

    private val maxPrice by derivedStateOf { visibleCandles.maxOfOrNull { it.high } ?: 0f }
    private val minPrice by derivedStateOf { visibleCandles.minOfOrNull { it.low } ?: 0f }

    val transformableState = TransformableState { zoomChange, _, _ -> scaleView(zoomChange) }

    val scrollableState = ScrollableState {
        if (it > 0) {
            scrollOffset.value =
                (scrollOffset.value - it.scrolledCandles).coerceAtLeast(0f)
        } else {
            scrollOffset.value =
                (scrollOffset.value - it.scrolledCandles).coerceAtMost(candles.value.lastIndex.toFloat())
        }
        it
    }

    private val Float.scrolledCandles: Float
        get() = this * visibleCandleCount.value.toFloat() / viewWidth

    val timeLines = mutableStateOf(listOf<Candle>())

    val priceLines by derivedStateOf {
        val priceItem = (maxPrice - minPrice) / PRICES_COUNT
        mutableListOf<String>().apply {
            repeat(PRICES_COUNT) { add(decimalFormat.format(maxPrice - priceItem * it)) }
        }
    }

    val visibleCandles by derivedStateOf {
        if (candles.value.isNotEmpty()) {
            candles.value.subList(
                scrollOffset.value.roundToInt().coerceAtLeast(0),
                (scrollOffset.value.roundToInt() + visibleCandleCount.value).coerceAtMost(candles.value.size)
            )
        } else {
            emptyList()
        }
    }

    private fun scaleView(zoomChange: Float) {
        if ((zoomChange < 1f && visibleCandleCount.value / zoomChange <= MAX_CANDLES) ||
            (zoomChange > 1f && visibleCandleCount.value / zoomChange >= MIN_CANDLES)
        ) {
            visibleCandleCount.value = (visibleCandleCount.value / zoomChange).roundToInt()
        }
    }

    fun setViewSize(width: Float, height: Float) {
        viewWidth = width
        viewHeight = height
    }

    fun calculateGridWidth() {
        val candleWidth = viewWidth / visibleCandleCount.value
        val currentGridWidth = candleInGrid * candleWidth
        when {
            currentGridWidth < MIN_GRID_WIDTH -> {
                candleInGrid = MAX_GRID_WIDTH / candleWidth
                timeLines.value = candles.value.filterIndexed { index, _ -> index % candleInGrid.roundToInt() == 0 }
            }
            currentGridWidth > MAX_GRID_WIDTH -> {
                candleInGrid = MIN_GRID_WIDTH / candleWidth
                timeLines.value = candles.value.filterIndexed { index, _ -> index % candleInGrid.roundToInt() == 0 }
            }
        }
    }

    fun xOffset(candle: Candle) =
        viewWidth * visibleCandles.indexOf(candle).toFloat() / visibleCandleCount.value.toFloat()

    fun yOffset(value: Float) = viewHeight * ((value - maxPrice) / (minPrice - maxPrice))

    fun setCandles(newCandles: List<Candle>?) {
        if (newCandles.isNullOrEmpty()) return
        candles.value = newCandles
        if (scrollOffset.value == UNINITIALIZED_SCROLL_OFFSET) {
            scrollOffset.value = newCandles.size.toFloat() - visibleCandleCount.value
        }
    }

    companion object {
        private val MAX_GRID_WIDTH = 500.dp.value
        private val MIN_GRID_WIDTH = 250.dp.value
        private const val MAX_CANDLES = 100
        private const val MIN_CANDLES = 30
        private const val START_CANDLES = 60
        private const val PRICES_COUNT = 10
        private const val UNINITIALIZED_SCROLL_OFFSET = -1f

        @Suppress("CAST_NEVER_SUCCEEDS")
        val Saver: Saver<MarketChartState, Any> = listSaver(
            save = { listOf(it.scrollOffset.value, it.visibleCandleCount.value) },
            restore = {
                MarketChartState().apply {
                    scrollOffset.value = it[0] as Float
                    visibleCandleCount.value = it[1] as Int
                }
            }
        )
    }
}
