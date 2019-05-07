package com.example.misealimi

import android.Manifest
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

const val PERMISSIONCODE_ESSENTIAL: Int = 1000
val permissionsForEssential: Array<out String> = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.INTERNET,
    Manifest.permission.GET_ACCOUNTS
)
    get() = field.clone()

class MainActivity : AppCompatActivity() {
    private var timelineList: GPSTimelineManager? = null

    private var gpsBackground: GPSStamper? = null
    val timeline = GPSTimelineManager.gpsTimeline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timeline.add(GPSTimeStamp(Location("A")))
        (gpsTimelineView as RecyclerView).apply {
            adapter = GPSStampAdapter(timeline)
        }
        (gpsTimelineView as RecyclerView).layoutManager = LinearLayoutManager(this)

        gpsBackground = GPSStamper(this)

        //권한 요청
        if(PermissionManager.isExist_deniedPermission(this, permissionsForEssential)) {
            PermissionManager.showRequest(this,
                PermissionManager.deniedPermListOf(this, permissionsForEssential), PERMISSIONCODE_ESSENTIAL,
                "일중 이동경로 기반 호흡량 계산", "위치정보 수집")
        }

        gpsBackground?.initializeLocationManager()
        gpsBackground?.stamp()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //////for log
        println("requestCode : $requestCode")
        println()

        for(index: Int in 0..grantResults.size - 1)
            println(permissions[index] + " : " + if(grantResults[index] == 0) "허가됨" else "불허")
    }
}