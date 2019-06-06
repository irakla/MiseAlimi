package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.R.string.cancel
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*


class GatheringService : Service() {
    private val INTERVAL: Long = 10 * 1000
    private val mHandler: Handler = Handler()
    private var mTimer: Timer? = null

    override fun onCreate() {
        if (mTimer != null) {
            mTimer?.cancel()
        } else {
            // recreate new
            mTimer = Timer()
        }
        // schedule task
        mTimer?.scheduleAtFixedRate(TimeDisplayTimerTask(), 0, INTERVAL)
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}