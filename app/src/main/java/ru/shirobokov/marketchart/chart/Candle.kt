package ru.shirobokov.marketchart.chart

import java.time.LocalDateTime

data class Candle(
    val time: LocalDateTime,
    val open: Float,
    val close: Float,
    val high: Float,
    val low: Float
) : Comparable<Candle> {

    override fun compareTo(other: Candle) = if (time.isBefore(other.time)) -1 else 1
}
