package com.example.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_gps_list.*

class GPS_List : AppCompatActivity() {
    private var gpsBackground: GPSStamper? = null
    val timeline = GPSTimelineManager.gpsTimeline

    companion object {
        var listIsNotShowing = true
            private set(value) { field = value }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_list)
        gpsTimelineView.adapter = GPSStampAdapter(this, timeline, gpsTimelineView)
        gpsTimelineView.scrollToPosition(0)
    }

    override fun onStart() {
        super.onStart()
        listIsNotShowing = false
    }

    override fun onStop() {
        super.onStop()
        listIsNotShowing = true
    }
}
