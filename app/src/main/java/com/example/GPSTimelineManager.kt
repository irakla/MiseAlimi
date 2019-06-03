package com.example.misealimi

import android.databinding.ObservableArrayList
import android.location.Location
import android.support.v7.app.AppCompatActivity
import org.json.JSONObject

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
}