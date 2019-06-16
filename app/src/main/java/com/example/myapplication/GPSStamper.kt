package com.example.myapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import java.lang.Exception

class GPSStamper(private val context : Context) : LocationListener{
    private val lm : LocationManager
    val gpsTimeline = GPSTimelineManager.gpsTimeline
    private var nowLocation : Location? = null

    companion object{
        val permissionForGPS: Array<String> = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
        )

        var meter_MinimalDistanceFromPrev:Float = 200.toFloat()

        val nameUsingPreference = "StampPeriod"
        val prevStampTimeKey = "StampTime"
    }

    init{
        lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if(gpsTimeline.isNotEmpty())
            nowLocation = GPSTimelineManager.gpsTimeline[0]?.location
    }

    fun startGetLocation(){
        if(PermissionManager.isExist_deniedPermission(context, permissionForGPS)) {
            println("coarse permission : ${ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED}")
            println("fine permission : ${ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED}")
            return
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER
            , 1,
            meter_MinimalDistanceFromPrev, this)
        println("Stamper initializing is finished.")
    }

    fun stamp(): Location?{
        if(nowLocation == null){
            println("GPSStamper.stamp : Location is null")
            return null
        }

        try {
            saveToDB(GPSTimeStamp(context, nowLocation as Location))
        }catch(e: Exception){
            println("catch in stamp... $e")
        }

        val stampTimePreference
                = context.applicationContext.getSharedPreferences(nameUsingPreference, Context.MODE_PRIVATE)
        val stampTimeEditor = stampTimePreference.edit()
        stampTimeEditor.putLong(prevStampTimeKey, System.currentTimeMillis())
        stampTimeEditor.apply()

        if(context is GatheringService)
            stopGetLocation()

        return nowLocation
    }

    private fun stopGetLocation() = lm.removeUpdates(this)

    private fun saveToDB(newTimeStamp: GPSTimeStamp){
        val sqlInsert = "INSERT INTO timeline (time_mil, latitude, longitude, airJSON, provider) " +
                "values('${newTimeStamp.location.time}', '${newTimeStamp.location.latitude}', " +
                "'${newTimeStamp.location.longitude}', '${newTimeStamp.airInfo.toString()}', " +
                "'${newTimeStamp.location.provider}')"

        val db = TimelineDBHelper(context.applicationContext)
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