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
import java.lang.Exception

class GPSStamper(private val view_Main : AppCompatActivity) : LocationListener{
    private val lm : LocationManager
    val gpsTimeline = GPSTimelineManager.gpsTimeline
    private var nowLocation : Location? = null

    companion object{
        val permissionForGPS: Array<String> = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
        )
    }

    init{
        lm = view_Main.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun initializeLocationManager(){
        println("Stamper is not null")
        if(PermissionManager.isExist_deniedPermission(view_Main, permissionForGPS)) {
            println("coarse permission : ${ContextCompat.checkSelfPermission(view_Main, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED}")
            println("fine permission : ${ContextCompat.checkSelfPermission(view_Main, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED}")
            return
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 200.toFloat(), this)
        println("Stamper initializing is finished.")
    }

    fun stamp(){
        if(nowLocation == null){
            println("GPSStamper.stamp : Location is null")
            return
        }

        try {
            GPSTimeStamp(view_Main, nowLocation as Location)
        }catch(e: Exception){
            println("catch in stamp... ${e.toString()}")
        }
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