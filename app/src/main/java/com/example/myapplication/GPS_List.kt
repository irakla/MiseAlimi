package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_gps_list.*
import java.util.*

class GPS_List : AppCompatActivity() {
    private var gpsBackground: GPSStamper? = null
    val timeline = GPSTimelineManager.gpsTimeline

    companion object {
        var listIsNotShowing = true
            private set(value) { field = value }

        val preferenceNameTimelineShow = "TimelineView"
        val keyDrawingPathIsOn = "DrawingPath"

        var isOnDrawingPath = true
            private set
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_list)
        gpsTimelineView.adapter = GPSStampAdapter(this, timeline, gpsTimelineView)
        gpsTimelineView.scrollToPosition(0)
        oneTimeStamper = GPSStamper(applicationContext)

        initializeButtonPath()
        initializeButtonNowLocation()
    }

    private var preference: SharedPreferences? = null

    private fun initializeButtonPath(){
        preference = applicationContext.getSharedPreferences(
            preferenceNameTimelineShow, Context.MODE_PRIVATE)

        val preferenceInstance = preference
        preferenceInstance?: return

        isOnDrawingPath = preferenceInstance.getBoolean(keyDrawingPathIsOn, isOnDrawingPath)

        if(!isOnDrawingPath)
            toggleButtonPath()

        buttonPath.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                isOnDrawingPath = isOnDrawingPath.not()
                toggleButtonPath()

                val preferenceInstanceInClick = preference
                preferenceInstanceInClick?: return

                val buttonStatePreferenceEditor = preferenceInstanceInClick.edit()
                buttonStatePreferenceEditor.putBoolean(keyDrawingPathIsOn, isOnDrawingPath)
                buttonStatePreferenceEditor.apply()
            }
        })
    }

    private fun toggleButtonPath(){
        Log.i(this.javaClass.name + ".toggleButtonPath", isOnDrawingPath.toString())
        if(isOnDrawingPath){
            buttonPath.text = "이동경로 표시 : ON"
            buttonPath.setBackgroundResource(R.drawable.editstyle)
        }
        else{
            buttonPath.text = "이동경로 표시 : OFF"
            buttonPath.setBackgroundResource(R.drawable.offstyle)
        }
    }

    private var oneTimeStamper: GPSStamper? = null
    private fun initializeButtonNowLocation(){
        buttonNowLocation.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                val nowStamper = oneTimeStamper

                Log.i(this.javaClass.name + ".buttonNowLocation", nowStamper?.isHot.toString())

                if(nowStamper == null)
                    return
                else if(nowStamper.isHot)
                    Toast.makeText(applicationContext, "탐색 중 입니다..",Toast.LENGTH_SHORT).show()
                else {
                    nowStamper.startGetLocation()

                    Toast.makeText(applicationContext
                        , "위치정보 수집중...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        listIsNotShowing = false
    }

    override fun onStop() {
        super.onStop()
        listIsNotShowing = true
    }


}
