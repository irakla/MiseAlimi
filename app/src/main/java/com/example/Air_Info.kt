package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.misealimi.GPSTimelineManager
import com.example.myapplication.R
import kotlinx.android.synthetic.main.fragment_air_info.*

class Air_Info : Fragment() {
    /*override fun n(): Air_Info{
        val args = Bundle()

        val frag = Air_Info()

        return frag
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_air_info, container, false)
        return layout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        print("airInfo 정상진행")
        if(GPSTimelineManager.gpsTimeline.isNotEmpty()){
            var pm10data = GPSTimelineManager.gpsTimeline[0].airInfo?.getString("pm10Value")
            var pm25data = GPSTimelineManager.gpsTimeline[0].airInfo?.getString("pm25Value")
            var placeinfo = GPSTimelineManager.gpsTimeline[0].airInfo?.getString("name")
            placeinfo += " " + GPSTimelineManager.gpsTimeline[0].airInfo?.getString("dataTime")
            place.setText(placeinfo)
            pm10.setText(pm10data)
            pm25.setText(pm25data)
        }

    }
}
