package com.example.misealimi

import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import java.lang.Exception
import java.time.Instant
import java.time.ZonedDateTime
import java.time.ZoneId
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

var formatstr: String = "yy년 MM월 dd일 E\n" +
        "a hh시mm분"

class GPSTimeStamp(view_Main: AppCompatActivity, location : Location){
    val location : Location
    var theTimeNotification = Date(location.time).toString()
        get() = field
        private set(newNotifWay) { field = newNotifWay }

    var airInfo: AirInfoType = null
        set(newAirInfo) {
            if(field == null)
                field = newAirInfo
            else
                throw Exception("비정상적인 대기정보 설정 시도 : ${newAirInfo?.getString("dataTime")}")
        }

    init{
        this.location = location
        setTime()
        GPSTimelineManager.gpsTimeline.add(0, (this))
        println("Misealimiback : New timestamp has created: ${location}")
        DownloaderForAirInfo(view_Main).execute(this).get()
    }

    private fun setTime(){
        if(Build.VERSION.SDK_INT >= 26) {           //OS가 Oreo거나 Oreo보다 최신버전일때
            val zdt: ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(location.time), ZoneId.of("Asia/Seoul"))
            theTimeNotification = zdt.format(DateTimeFormatter.ofPattern(formatstr))
        }
        else {
            val dateformat = SimpleDateFormat(formatstr, Locale.KOREAN)
            theTimeNotification = dateformat.format(location.time)
        }
    }
}