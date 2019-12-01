package com.example.myapplication

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.myapplication.GPSTimelineManager.gpsTimeline

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONObject

class LocationVisualizer : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_visual)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        intent?: return

        val latitude = intent.extras?.getDouble("latitude", 37.305685)
        val longitude = intent.extras?.getDouble("longitude", 127.922691)
        val time = intent.extras?.getLong("time", 0)
        val airInfo = intent.extras?.getString("airInfo", "null")

        if(latitude != null && longitude != null) {
            val placeGathered = LatLng(latitude, longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeGathered, 16.toFloat()))

            if(time != null && airInfo != null)
                if (GPS_List.isOnDrawingPath) {
                    drawTimeline()
                    mMap.addMarker(createMarker(latitude, longitude, time, airInfo))
                } else
                    mMap.addMarker(createMarker(latitude, longitude, time, airInfo))
        }
    }

    private fun drawTimeline(){
        val timelineByOnlyGappedStamps = mutableListOf<GPSTimeStamp>()

        val timelinePolylineOptions = PolylineOptions()
        timelinePolylineOptions.color(Color.BLACK)
        timelinePolylineOptions.width(5.toFloat())

        gpsTimeline.reversed().forEachIndexed { index, it ->
            if(it.airInfo == null)
                return@forEachIndexed

            if(index == 0 ||
                it.location.distanceTo(timelineByOnlyGappedStamps.last().location) >= 200.toDouble()) {
                mMap.addMarker(createMarker(it))
                timelineByOnlyGappedStamps.add(it)

                timelinePolylineOptions.add(
                    LatLng(it.location.latitude, it.location.longitude)
                )
            }

            Log.i(this.javaClass.name + ".gps기록검색", "정상작동")
        }

        mMap.addPolyline(timelinePolylineOptions)
    }

    private fun createMarker(timestamp: GPSTimeStamp) : MarkerOptions{
        val nowAirInfo = timestamp.airInfo
        val pm10str = nowAirInfo?.getString("pm10Value")
        val pm25str = nowAirInfo?.getString("pm25Value")

        val nowLocationCoordinate = LatLng(timestamp.location.latitude, timestamp.location.longitude)

        val markerOptions = MarkerOptions().apply{
            position(nowLocationCoordinate)
            title(TimeSupporter.translateTimeToString(timestamp.location.time))
            snippet("미세먼지 : ${pm10str}, 초미세먼지 : ${pm25str}")
        }

        return markerOptions
    }

    private fun createMarker(
        latitude: Double,
        longitude: Double,
        time: Long,
        airInfo: String
    ) : MarkerOptions{
        val nowAirInfo: AirInfoType =
            if(airInfo != "null")
                JSONObject(airInfo)
            else
                null

        val pm10str = if(nowAirInfo != null) nowAirInfo.getString("pm10Value") else "-"
        val pm25str = if(nowAirInfo != null) nowAirInfo.getString("pm25Value") else "-"

        val nowLocationCoordinate = LatLng(latitude, longitude)

        val markerOptions = MarkerOptions().apply{
            position(nowLocationCoordinate)
            title(TimeSupporter.translateTimeToString(time))
            snippet("미세먼지 : ${pm10str}, 초미세먼지 : ${pm25str}")
        }

        return markerOptions
    }
}
