package com.example.misealimi

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

class GPSStamper(private val view : AppCompatActivity) : LocationListener{
    private val lm : LocationManager
    val gpsTimeline = GPSTimelineManager.gpsTimeline
    private var nowLocation : Location? = null

    init{
        lm = view.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun initializeLocationManager(){
        println("Stamper is not null")
        if(PermissionManager.isExist_deniedPermission(view, permissionForEssential)) {
            println("coarse permission : " + (ContextCompat.checkSelfPermission(view, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            println("fine permission : " + (ContextCompat.checkSelfPermission(view, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            return
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 200.toFloat(), this)
        println("Stamper initializing is finished.")
    }

    fun stamp(){
        if(nowLocation == null){
            println("GPSStamper.stamp : Location is null")
            return
        }

        val gpstimestamp = GPSTimeStamp(nowLocation as Location)
        gpsTimeline.add(gpstimestamp)
        println("Misealimiback : Location has added : " + nowLocation)
    }

    override fun onLocationChanged(location: Location?) {
        location ?: return

        nowLocation = location
        stamp()
        println("Misealimiback : Location has changed.")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

}