package com.example.myapplication

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.GPSTimelineManager.gpsTimeline

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val latitude = intent.extras.getDouble("latitude", 37.305685)
        val longitude = intent.extras.getDouble("longitude", 127.922691)
        val time = intent.extras.getLong("time", 0)
        val placeGathered = LatLng(latitude, longitude)

        if(time.toInt() != 0)
            mMap.addMarker(MarkerOptions().position(placeGathered).
            title(TimeSupporter.translateTimeToString(time)))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeGathered, 16.toFloat()))

        polyline()
    }

    private fun polyline(){
        val timelineByOnlyGappedStamps = mutableListOf<GPSTimeStamp>()

        val polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylineOptions.width(5.toFloat())
        gpsTimeline.reversed().forEachIndexed { index, it ->
            if(index > 0 && it.airInfo != null &&
                (it.location.distanceTo(timelineByOnlyGappedStamps.last().location) >= 200.toDouble())) {
                val nowLocationCoordinate = LatLng(it.location.latitude, it.location.longitude)
                //polylineOptions.add(nowLocationCoordinate)
                mMap.addMarker(MarkerOptions().position(nowLocationCoordinate).
                    title(TimeSupporter.translateTimeToString(it.location.time)))
            }
            else if(index == 0)
                timelineByOnlyGappedStamps.add(it)
        }

        timelineByOnlyGappedStamps.forEach{
            val nowAirInfo = it.airInfo
            val pm10str = nowAirInfo?.getString("pm10Value")

            if(pm10str == null || pm10str == "-") {
                polylineOptions.color(Color.BLACK)
                return@forEach
            }

            when(pm10str.toInt()){
                in 1..VERYGOODMAX_PM10 -> polylineOptions.color(Color.BLUE)
                in (VERYGOODMAX_PM10 + 1) .. NORMALMAX_PM10 -> polylineOptions.color(Color.GREEN)
                in (NORMALMAX_PM10 + 1) .. BADMAX_PM10 -> polylineOptions.color(Color.YELLOW)
                else -> polylineOptions.color(Color.RED)
            }

            polylineOptions.add(LatLng(it.location.latitude, it.location.longitude))
        }

        mMap.addPolyline(polylineOptions)
    }
}
