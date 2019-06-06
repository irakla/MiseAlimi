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
import com.example.myapplication.PermissionManager
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

        var min_PeriodLocationRefresh:Long = 1
        var meter_MinimalDistanceFromPrev:Float = 200.toFloat()
    }

    init{
        lm = view_Main.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        GPSTimelineManager.initializeTimeline(view_Main)

        if(gpsTimeline.size > 0)
            nowLocation = GPSTimelineManager.gpsTimeline[0]?.location
    }

    fun initializeLocationManager(){
        println("Stamper is not null")
        if(PermissionManager.isExist_deniedPermission(view_Main, permissionForGPS)) {
            println("coarse permission : ${ContextCompat.checkSelfPermission(view_Main, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED}")
            println("fine permission : ${ContextCompat.checkSelfPermission(view_Main, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED}")
            return
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER
            , /*min_PeriodLocationRefresh * 60000*/1, meter_MinimalDistanceFromPrev, this)
        println("Stamper initializing is finished.")
    }

    fun stamp(): Location?{
        if(nowLocation == null){
            println("GPSStamper.stamp : Location is null")
            return null
        }

        try {
            saveToDB(GPSTimeStamp(view_Main, nowLocation as Location))
        }catch(e: Exception){
            println("catch in stamp... ${e.toString()}")
        }

        return nowLocation
    }

    private fun saveToDB(newTimeStamp: GPSTimeStamp){
        val sqlInsert = "INSERT INTO timeline (time_mil, latitude, longitude, airJSON, provider) " +
                "values('${newTimeStamp.location.time}', '${newTimeStamp.location.latitude}', " +
                "'${newTimeStamp.location.longitude}', '${newTimeStamp.airInfo.toString()}', " +
                "'${newTimeStamp.location.provider}')"

        val db = TimelineDBHelper(view_Main)
        db.writableDatabase.execSQL(sqlInsert)
    }

    override fun onLocationChanged(location: Location?) {
        location ?: return
        if(nowLocation?.time == location?.time)
            return

        nowLocation = location
        println("Location : ${location?.provider}")
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