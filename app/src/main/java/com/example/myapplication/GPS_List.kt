package com.example.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_gps_list.*

class GPS_List : AppCompatActivity() {
    private var gpsBackground: GPSStamper? = null
    val timeline = GPSTimelineManager.gpsTimeline
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_list)
        gpsTimelineView.adapter = GPSStampAdapter(this, timeline)
        gpsBackground = GPSStamper(this)
        gpsBackground?.initializeLocationManager()
    }
}