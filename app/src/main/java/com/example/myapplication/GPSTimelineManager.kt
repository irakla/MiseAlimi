package com.example.myapplication

import android.content.Context
import android.databinding.ObservableArrayList
import android.location.Location
import android.os.Build
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

object GPSTimelineManager {
    var gpsTimeline : ObservableArrayList<GPSTimeStamp> = ObservableArrayList<GPSTimeStamp>()
        get() = field
        private set(newTimeLine) { field = newTimeLine }

    fun initializeTimeline(context: Context){
        val db = TimelineDBHelper(context)
        val dbCursor = db.readableDatabase.rawQuery("SELECT * FROM timeline", null)

        //TODO : db읽기 예외처리
        if(dbCursor.moveToFirst())
            do{
                val location = Location(dbCursor.getString(dbCursor.getColumnIndex("provider")))
                location.time = dbCursor.getLong(dbCursor.getColumnIndex("time_mil"))
                location.latitude = dbCursor.getDouble(dbCursor.getColumnIndex("latitude"))
                location.longitude = dbCursor.getDouble(dbCursor.getColumnIndex("longitude"))

                val serializedJSON = dbCursor.getString(dbCursor.getColumnIndex("airJSON"))
                println("1serializedJSON : ${serializedJSON is String}")
                val airInfo: AirInfoType = if(serializedJSON == "null") null
                else JSONObject(serializedJSON)

                GPSTimeStamp(context, location, airInfo)
            }while(dbCursor.moveToNext())
        dbCursor.close()
    }

    /*
    * 가장 최근의 parameter시간 범위 GPSTimeStamp리스트를 제공
    *
    * @parameter 구하고자 하는 시간 범위
    * @return parameter시간 범위(in the time)의 GPSTimeStamp리스트            [index] in 1..(size - 1)
    * + parameter시간 범위 직전 timestamp(없으면 null)                        nextLast
    * 
    * 해당하는 시간 범위의 timestamp 없으면 emptylist
     */
    fun getTimeStampsInTheTime(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) : List<GPSTimeStamp?> {
        //parameter validity check
        if (!(startHour in 0 until 24)
            || !(endHour in 0 until 24)
            || !(startMinute in 0 until 60)
            || !(endMinute in 0 until 60)
        )
            return emptyList()

        val timeStampsInTheTime = mutableListOf<GPSTimeStamp?>()
        val startIsPreviousDay = startHour > endHour

        var theStartTime: Long
        var theEndTime: Long

        //시작시간과 끝시간 구하기
        if (Build.VERSION.SDK_INT >= 26) {           //OS가 Oreo거나 Oreo보다 최신버전일때
            var startzdt =
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Seoul"))
            startzdt = startzdt.minusDays(if (startIsPreviousDay) 1 else 0)
            startzdt = startzdt.withHour(startHour)
            startzdt = startzdt.withMinute(startMinute)
            startzdt = startzdt.withSecond(0)
            theStartTime = startzdt.toEpochSecond() * 1000

            var endzdt = startzdt.plusDays(if (startIsPreviousDay) 1 else 0)
            endzdt = endzdt.withHour(endHour)
            endzdt = endzdt.withMinute(endMinute)
            endzdt = endzdt.withSecond(0)
            theEndTime = endzdt.toEpochSecond() * 1000
        } else {
            //하위버전에서 정상작동하지 않음.
            val OneDay = "02"
            val ZeroDay = "01"

            var startTimeString = SimpleDateFormat("yyyy-MM-dd ").format(
                Date(
                    System.currentTimeMillis() - SimpleDateFormat("dd").parse(if (startIsPreviousDay) OneDay else ZeroDay).time
                )
            )

            startTimeString = startTimeString + String.format(" %2d:%2d:00", startHour, startMinute)
            theStartTime = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(startTimeString).time

            var endTimeString = SimpleDateFormat("yyyy-MM-dd ").format(Date(System.currentTimeMillis()))
            endTimeString = endTimeString + String.format(" %2d:%2d:00", endHour, endMinute)
            theEndTime = SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(endTimeString).time
        }
        //시작시간과 끝시간 구하기

        //범위 내 시간에 기록된 timestamp 저장
        var indexFirstInTheTime: Int? = null
        for((nowIndex, nowTimeStamp) in gpsTimeline.withIndex())
            if(nowTimeStamp.location.time <= theEndTime) {                     //find first in parameter time
                indexFirstInTheTime = nowIndex
                break
            }

        if(indexFirstInTheTime == null)
            return emptyList()

        println("시간 범위 체크 시작")
        var indexNextLast: Int? = null
        for(nowIndex in indexFirstInTheTime until gpsTimeline.size)
            if (gpsTimeline[nowIndex].location.time >= theStartTime)
                timeStampsInTheTime.add(gpsTimeline[nowIndex])
            else {
                indexNextLast = nowIndex
                break
            }
        //범위 내 시간에 기록된 timestamp 저장

        timeStampsInTheTime.add(                //nextLast 추가
            if(indexNextLast != null)
                gpsTimeline[indexNextLast]
            else                                //the found was last on gpsTimeline, so next is not exist
                null
        )

        return timeStampsInTheTime
    }
}