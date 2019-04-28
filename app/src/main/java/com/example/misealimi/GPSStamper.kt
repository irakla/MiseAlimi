package com.example.misealimi

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

class GPSStamper(view : GPSTimelineView) : LocationListener{
    private val lm : LocationManager
    private val gpsTimeline : MutableList<GPSTimeStamp>
    private var nowLocation : Location? = null

    init{
        lm = view.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsTimeline = view.gpsTimeline
    }

    fun stamp(){
        nowLocation ?: return

        var gpstimestamp : GPSTimeStamp = GPSTimeStamp(nowLocation as Location)
        gpsTimeline.add(gpstimestamp)
        println("Misealimiback : Location has added.")
    }

    override fun onLocationChanged(location: Location?) {
        location ?: return

        nowLocation = location
        println("Misealimiback : Location has changed.")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}