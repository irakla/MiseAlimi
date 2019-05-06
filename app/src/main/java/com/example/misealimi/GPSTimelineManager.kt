package com.example.misealimi

object GPSTimelineManager {
    var gpsTimeline : MutableList<GPSTimeStamp> = mutableListOf<GPSTimeStamp>()
        get() = field
        private set(newTimeLine) { field = newTimeLine }


}