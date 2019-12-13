package com.example.myapplication

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

class GPSTimeStamp(
    val location : Location
    , airInfo: AirInfoType = null
) : View.OnClickListener{

    companion object{
        var formatstr: String = "yy년 MM월 dd일 E\n" +
                "a hh시mm분"
    }

    var theTimeNotification = Date(location.time).toString()
        private set

    var airInfo: AirInfoType = null
        set(newAirInfo) {
            if(field == null) {
                field = newAirInfo
            }
            else
                throw Exception("비정상적인 대기정보 설정 시도 : ${newAirInfo?.getString("dataTime")}")
        }

    init{
        setTimeNotification()
        GPSTimelineManager.gpsTimeline.add(0, (this))

        if(airInfo != null)
            this.airInfo = airInfo
    }

    private fun setTimeNotification(){
        theTimeNotification = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val zdt: ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(location.time), ZoneId.of("Asia/Seoul"))
            zdt.format(DateTimeFormatter.ofPattern(formatstr))
        } else {
            val dateformat = SimpleDateFormat(formatstr, Locale.KOREAN)
            dateformat.format(location.time)
        }
    }

    override fun onClick(view: View?) {
        view?: return

        val intent = Intent(view.context, LocationVisualizer::class.java)
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        intent.putExtra("time", location.time)
        intent.putExtra("airInfo", airInfo.toString())
        view.context.startActivity(intent)
    }
}