package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import android.widget.Toast
import org.jetbrains.anko.toast
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class GPSStamper(private val context : Context) : LocationListener{
    private val lm
            = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var isHot = false
        private set
    val gpsTimeline = GPSTimelineManager.gpsTimeline

    companion object{
        val permissionForGPS: Array<String> = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        var meter_MinimalDistanceFromPrev:Float = 200.toFloat()

        val nameUsingPreference = "StampPeriod"
        val prevStampTimeKey = "StampTime"
    }

    fun startGetLocation(){
        if(PermissionManager.existDeniedPermission(context, permissionForGPS)) {
            Log.d("coarse permission", "${ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED}")
            Log.d("fine permission", "${ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED}")
            return
        }

        isHot = true

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER
            , 1,
            meter_MinimalDistanceFromPrev, this)
        println("Stamper initializing is finished.")
    }

    fun stamp(nowLocation: Location?): Location?{
        nowLocation ?: return null

        showStampingToUser()
        val nowGPSTimeStamp = GPSTimeStamp(nowLocation)

        try {
            saveToDB(nowGPSTimeStamp)
        }catch(e: Exception){
            println("catch in stamp... $e")
        }

        val stampTimePreference
                = context.applicationContext.getSharedPreferences(nameUsingPreference, Context.MODE_PRIVATE)
        val stampTimeEditor = stampTimePreference.edit()
        stampTimeEditor.putLong(prevStampTimeKey, System.currentTimeMillis())
        stampTimeEditor.apply()

        stopGetLocation()
        isHot = false

        return nowLocation
    }

    private fun showStampingToUser(){
        val showingFormat = SimpleDateFormat("[yyyy/MM/dd - HH:mm:ss]")

        context.toast("위치수집 : ${showingFormat.format(Date())}")
    }

    private fun stopGetLocation() = lm.removeUpdates(this)

    private fun saveToDB(newTimeStamp: GPSTimeStamp){
        val db = TimelineDBEntry(context)
        db.insertTimeStamp(newTimeStamp.location, newTimeStamp.airInfo)
        DownloaderForAirInfo(context).execute(newTimeStamp)
    }

    override fun onLocationChanged(location: Location?) {
        location ?: return

        stamp(location)
        Log.d("Misealimi Stamper", "Location updated")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String?) {}
    override fun onProviderDisabled(provider: String?) {}

}