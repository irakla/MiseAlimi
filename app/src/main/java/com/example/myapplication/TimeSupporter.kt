package com.example.myapplication

import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

const val INTERVAL_MILLI_FOR_GMT0900 = 9 * 60 * 60 * 1000
const val DAY_BY_MILLI_SEC = 24 * 60 * 60 * 1000
const val MINUTE_BY_MILLI_SEC = 60000
const val ZERODAY = 0

class TimeSupporter {
    companion object {
        fun getTheLatestMilliTime(
            isYesterday: Boolean,
            hourIn0_23: Int,
            minute: Int
        ): Long {
            var theTime: Long

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var timezdt =
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"))
                timezdt = timezdt.minusDays(if (isYesterday) 1 else 0)
                timezdt = timezdt.withHour(hourIn0_23)
                timezdt = timezdt.withMinute(minute)
                timezdt = timezdt.withSecond(0)
                theTime = timezdt.toEpochSecond() * 1000
                Log.i(this.javaClass.name + ".계산된시간", timezdt.toString())
            } else {
                val translateToFullTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                val startHourTimeByGMT0000 = if (hourIn0_23 < 9) hourIn0_23 + 15 else hourIn0_23 - 9

                var timeString = SimpleDateFormat("yyyy-MM-dd ").format(
                    Date(
                        System.currentTimeMillis() - if (isYesterday) DAY_BY_MILLI_SEC else ZERODAY
                    )
                )
                timeString += String.format(" %2d:%2d ", startHourTimeByGMT0000, minute)
                theTime = translateToFullTimeFormat.parse(timeString).time
                Log.i(this.javaClass.name + ".계산된시간", timeString)
            }

            return theTime
        }

        fun translateTimeToString(epochMilliSecond: Long) : String{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var timezdt =
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilliSecond), ZoneId.of("Asia/Seoul"))

                val formatKorean = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")

                return timezdt.format(formatKorean)
            }

            val translateToFullTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")

            var timeString = translateToFullTimeFormat.format(
                Date(epochMilliSecond + INTERVAL_MILLI_FOR_GMT0900)
            )

            return timeString
        }
    }
}