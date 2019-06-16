package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.view.View
import java.lang.Exception
import java.time.Instant
import java.time.ZonedDateTime
import java.time.ZoneId
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

var formatstr: String = "yy년 MM월 dd일 E\n" +
        "a hh시mm분"

class GPSTimeStamp(context: Context, location : Location, airInfo: AirInfoType = null) : View.OnClickListener{
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
        if(airInfo == null)
            DownloaderForAirInfo(context).execute(this).get()
        else
            this.airInfo = airInfo
    }

    private fun setTime(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val zdt: ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(location.time), ZoneId.of("Asia/Seoul"))
            theTimeNotification = zdt.format(DateTimeFormatter.ofPattern(formatstr))
        }
        else {
            val dateformat = SimpleDateFormat(formatstr, Locale.KOREAN)
            theTimeNotification = dateformat.format(location.time)
        }
    }

    override fun onClick(v: View?) {
        v?: return

        val intent = Intent(v.context, LocationVisualizer::class.java)
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        intent.putExtra("time", location.time)
        intent.putExtra("airInfo", airInfo.toString())
        v.context.startActivity(intent)
    }
}