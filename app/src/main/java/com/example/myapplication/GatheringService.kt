package com.example.myapplication

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.R.string.cancel
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.example.myapplication.GPSStamper.Companion.meter_MinimalDistanceFromPrev
import java.text.SimpleDateFormat
import java.util.*


class GatheringService : Service(), LocationListener {
    private var stamperInBackground: GPSStamper? = null
    private val INTERVAL: Long = 10 * 1000
    private val mHandler: Handler = Handler()
    private var mTimer: Timer? = null
    private val lm : LocationManager? = null

    override fun onCreate() {
        if (mTimer != null) {
            mTimer?.cancel()
        } else {
            // recreate new
            mTimer = Timer()
        }
        // schedule task
        mTimer?.scheduleAtFixedRate(TimeDisplayTimerTask(), 0, INTERVAL)

        stamperInBackground = GPSStamper(this)
        stamperInBackground?.initializeLocationManager()
    }

    inner class TimeDisplayTimerTask: TimerTask() {

        override fun run() {
            // run on another thread
            mHandler.post(object : Runnable {
                override fun run() {
                    // display toast
                    Toast.makeText(
                        getApplicationContext(), getDateTime(),
                        Toast.LENGTH_SHORT
                    ).show()


                }
            })
        }

        private fun getDateTime(): String {
            // get date time in custom format
            val sdf = SimpleDateFormat("[yyyy/MM/dd - HH:mm:ss]")
            return sdf.format(Date())
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return null
    }


    override fun onLocationChanged(location: Location?) {
        /*location ?: return
        if(nowLocation?.time == location?.time)
            return

        nowLocation = location
        println("Location : ${location?.provider}")
        stamp()
        println("Misealimiback : Location has changed.")*/
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {    }
    override fun onProviderEnabled(provider: String?) {    }
    override fun onProviderDisabled(provider: String?) {    }
}