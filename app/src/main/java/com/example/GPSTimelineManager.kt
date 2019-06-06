package com.example.misealimi

import android.databinding.ObservableArrayList
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import com.example.myapplication.HOUR
import com.example.myapplication.MINUTE
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object GPSTimelineManager {
    var gpsTimeline : ObservableArrayList<GPSTimeStamp> = ObservableArrayList<GPSTimeStamp>()
        get() = field
        private set(newTimeLine) { field = newTimeLine }

    fun initializeTimeline(view_Main: AppCompatActivity){
        val db = TimelineDBHelper(view_Main)
        val dbCursor = db.readableDatabase.rawQuery("SELECT * FROM timeline", null)

        //TODO : db읽기 예외처리
        if(dbCursor.moveToFirst())
            do{
                val location = Location(dbCursor.getString(dbCursor.getColumnIndex("provider")))
                location.time = dbCursor.getLong(dbCursor.getColumnIndex("time_mil"))
                location.latitude = dbCursor.getDouble(dbCursor.getColumnIndex("latitude"))
                location.longitude = dbCursor.getDouble(dbCursor.getColumnIndex("longitude"))

                val airInfo: AirInfoType = JSONObject(dbCursor.getString(dbCursor.getColumnIndex("airJSON")))

                GPSTimeStamp(view_Main, location, airInfo)
            }while(dbCursor.moveToNext())
        dbCursor.close()
    }

    fun getTimeStampsInTheTime(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) : List<GPSTimeStamp>{
        val timeStampsInTheTime = mutableListOf<GPSTimeStamp>()

        val startIsPreviousDay = startHour > endHour

        var theStartTime: Long;
        var theEndTime: Long;

        //외출시작시간과 끝시간 구하기
        if(Build.VERSION.SDK_INT >= 26) {           //OS가 Oreo거나 Oreo보다 최신버전일때
            var startzdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"))
            startzdt = startzdt.minusDays(if(startIsPreviousDay) 1 else 0)
            startzdt = startzdt.withHour(startHour)
            startzdt = startzdt.withMinute(startMinute)
            startzdt = startzdt.withSecond(0)
            theStartTime = startzdt.toEpochSecond() * 1000

            var endzdt = startzdt.plusDays(if(startIsPreviousDay) 1 else 0)
            endzdt = endzdt.withHour(endHour)
            endzdt = endzdt.withMinute(endMinute)
            endzdt = endzdt.withSecond(0)
            theEndTime = endzdt.toEpochSecond() * 1000
        }
        else {
            //하위버전에서 정상작동하지 않음.
            val OneDay = "02"
            val ZeroDay = "01"

            var startTimeString = SimpleDateFormat("yyyy-MM-dd ").
                format(Date(System.currentTimeMillis() - SimpleDateFormat("dd").
                    parse(if(startIsPreviousDay) OneDay else ZeroDay).time))

            startTimeString = startTimeString + String.format(" %2d:%2d:00", startHour, startMinute)
            theStartTime = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(startTimeString).time

            var endTimeString = SimpleDateFormat("yyyy-MM-dd ").format(Date(System.currentTimeMillis()))
            endTimeString = endTimeString + String.format(" %2d:%2d:00", endHour, endMinute)
            theEndTime = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(endTimeString).time
        }
        //외출시작시간과 끝시간 구하기

        gpsTimeline.forEach {
            if(theStartTime <= it.location.time && theEndTime >= it.location.time)
                timeStampsInTheTime.add(it)
        }

        return timeStampsInTheTime
    }
}