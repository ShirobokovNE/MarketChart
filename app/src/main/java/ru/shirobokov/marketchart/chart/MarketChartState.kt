package ru.shirobokov.marketchart.chart

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt

class MarketChartState {

    private var candles = listOf<Candle>()
    private var visibleCandleCount by mutableStateOf(0)
    private var scrollOffset by mutableStateOf(0f)
    private var viewWidth = 0f
    private var viewHeight = 0f
    private var candleInGrid = Float.MAX_VALUE

    private val maxPrice by derivedStateOf { visibleCandles.maxOfOrNull { it.high } ?: 0f }
    private val minPrice by derivedStateOf { visibleCandles.minOfOrNull { it.low } ?: 0f }

    val transformableState = TransformableState { zoomChange, _, _ -> scaleView(zoomChange) }

    val scrollableState = ScrollableState {
        scrollOffset = if (it > 0) {
            (scrollOffset - it.scrolledCandles).coerceAtLeast(0f)
        } else {
            (scrollOffset - it.scrolledCandles).coerceAtMost(candles.lastIndex.toFloat())
        }
        it
    }

    private val Float.scrolledCandles: Float
        get() = this * visibleCandleCount.toFloat() / viewWidth

    var timeLines by mutableStateOf(listOf<Candle>())

    val priceLines by derivedStateOf {
        val priceItem = (maxPrice - minPrice) / PRICES_COUNT
        mutableListOf<Float>().apply { repeat(PRICES_COUNT) { if (it > 0) add(maxPrice - priceItem * it) } }
    }

    val visibleCandles by derivedStateOf {
        if (candles.isNotEmpty()) {
            candles.subList(
                scrollOffset.roundToInt().coerceAtLeast(0),
                (scrollOffset.roundToInt() + visibleCandleCount).coerceAtMost(candles.size)
            )
        } else {
            emptyList()
        }
    }

    private fun scaleView(zoomChange: Float) {
        if ((zoomChange < 1f && visibleCandleCount / zoomChange <= MAX_CANDLES) ||
            (zoomChange > 1f && visibleCandleCount / zoomChange >= MIN_CANDLES)
        ) {
            visibleCandleCount = (visibleCandleCount / zoomChange).roundToInt()
        }
    }

    fun setViewSize(width: Float, height: Float) {
        viewWidth = width
        viewHeight = height
    }

    fun calculateGridWidth() {
        val candleWidth = viewWidth / visibleCandleCount
        val currentGridWidth = candleInGrid * candleWidth
        when {
            currentGridWidth < MIN_GRID_WIDTH -> {
                candleInGrid = MAX_GRID_WIDTH / candleWidth
                timeLines = candles.filterIndexed { index, _ -> index % candleInGrid.roundToInt() == 0 }
            }
            currentGridWidth > MAX_GRID_WIDTH -> {
                candleInGrid = MIN_GRID_WIDTH / candleWidth
                timeLines = candles.filterIndexed { index, _ -> index % candleInGrid.roundToInt() == 0 }
            }
        }
    }

    fun xOffset(candle: Candle) = viewWidth * visibleCandles.indexOf(candle).toFloat() / visibleCandleCount.toFloat()
    fun yOffset(value: Float) = viewHeight * (maxPrice - value) / (maxPrice - minPrice)

    companion object {
        private const val MAX_GRID_WIDTH = 500
        private const val MIN_GRID_WIDTH = 250
        private const val MAX_CANDLES = 100
        private const val MIN_CANDLES = 30
        private const val START_CANDLES = 60
        private const val PRICES_COUNT = 10

        fun getState(candles: List<Candle>, visibleCandleCount: Int? = null, scrollOffset: Float? = null) =
            MarketChartState().apply {
                this.candles = candles
                this.visibleCandleCount = visibleCandleCount ?: START_CANDLES
                this.scrollOffset = scrollOffset ?: candles.size.toFloat() - this.visibleCandleCount
            }

        @Suppress("UNCHECKED_CAST")
        val Saver: Saver<MarketChartState, Any> = listSaver(
            save = { listOf(it.candles, it.scrollOffset, it.visibleCandleCount) },
            restore = {
                getState(
                    candles = it[0] as List<Candle>,
                    visibleCandleCount = it[2] as Int,
                    scrollOffset = it[1] as Float
                )
            }
        )
    }
}
