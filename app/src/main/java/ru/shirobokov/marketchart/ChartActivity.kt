package ru.shirobokov.marketchart

import MarketChart
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import ru.shirobokov.marketchart.chart.Candle
import java.time.LocalDateTime

class ChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val candles = mutableListOf<Candle>()
        assets.open("quotes.txt").use {
            it.bufferedReader().forEachLine { line ->
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
        }

        setContent {
            MarketChart(candles)
        }
    }
}
