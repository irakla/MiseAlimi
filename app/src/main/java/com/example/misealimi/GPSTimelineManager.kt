package com.example.misealimi

import android.databinding.ObservableArrayList
import java.util.*

object GPSTimelineManager {
    var gpsTimeline : ObservableArrayList<GPSTimeStamp> = ObservableArrayList<GPSTimeStamp>()
        get() = field
        private set(newTimeLine) { field = newTimeLine }


}