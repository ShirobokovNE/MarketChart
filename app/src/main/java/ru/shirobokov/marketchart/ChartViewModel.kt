package ru.shirobokov.marketchart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ChartViewModel : ViewModel() {

    val chartState by mutableStateOf(MarketChartState())

    init {
        viewModelScope.launch {
            with(SupervisorJob() + Dispatchers.IO) {
                javaClass.classLoader?.getResourceAsStream("assets/quotes.txt").use {
                    val candles = mutableListOf<Candle>()
                    it?.bufferedReader()?.forEachLine { line ->
                        val splitStrings = line.split(" ")

                        val year = splitStrings[0].substring(0, 4).toInt()
                        val month = splitStrings[0].substring(4, 6).toInt()
                        val day = splitStrings[0].substring(6, 8).toInt()
                        val hour = splitStrings[1].substring(0, 2).toInt()
                        val minute = splitStrings[1].substring(2, 4).toInt()

                        val dateTime = LocalDateTime.of(year, month, day, hour, minute)
                        val open = splitStrings[2].toFloat()
                        val high = splitStrings[3].toFloat()
                        val low = splitStrings[4].toFloat()
                        val close = splitStrings[5].toFloat()

                        candles.add(Candle(dateTime, open, close, high, low))
                    }
                    candles.sort()
                    chartState.setCandles(candles)
                }
            }
        }
    }
}
