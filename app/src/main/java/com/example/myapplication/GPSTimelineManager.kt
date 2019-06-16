package com.example.myapplication

import android.content.Context
import android.databinding.ObservableArrayList
import android.location.Location
import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

object GPSTimelineManager {
    var gpsTimeline : ObservableArrayList<GPSTimeStamp> = ObservableArrayList<GPSTimeStamp>()
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
    * @parameter 구하고자 하는 시간 범위(최신순)
    * @return parameter시간 범위(in the time)의 GPSTimeStamp리스트            [index] in 0..(size - 2)
    * + parameter시간 범위 직전 timestamp(없으면 null)                        [size - 1] = lastBeforeStart
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
        val startIsPreviousDay = startHour > endHour || (startHour == endHour && startMinute > endMinute)

        var theStartTime = TimeSupporter.getTheLatestMilliTime(startIsPreviousDay, startHour, startMinute)
        var theEndTime = TimeSupporter.getTheLatestMilliTime(false, endHour, endMinute)

        timeStampsInTheTime.addAll(
            gpsTimeline.filter{ it.location.time in theStartTime .. theEndTime })

        val lastBeforeStart = gpsTimeline.find {
            it != null && it.location.time < theStartTime
        }
        timeStampsInTheTime.add(lastBeforeStart)

        return timeStampsInTheTime
    }
}