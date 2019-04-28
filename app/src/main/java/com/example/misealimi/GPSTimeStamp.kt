package com.example.misealimi

import android.location.Location
import java.util.*

class GPSTimeStamp(location : Location){
    val location : Location
    val theTime : Date

    init{
        this.location = location
        this.theTime = Date(location.time)
    }
}