package com.example.misealimi

import android.Manifest
import android.databinding.DataBindingUtil
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.example.misealimi.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

const val PERMISSIONCODE_Essential: Int = 1000
val permissionForEssential: Array<out String> = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.INTERNET
)
    get() = field.clone()

class MainActivity : AppCompatActivity() {
    private var gpsBackground: GPSStamper? = null
    val timeline = GPSTimelineManager.gpsTimeline

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //권한 요청
        if(PermissionManager.isExist_deniedPermission(this, permissionForEssential)) {
            PermissionManager.showRequest(this,
                PermissionManager.deniedPermListOf(this, permissionForEssential), PERMISSIONCODE_Essential,
                "일중 이동경로 기반 호흡량 계산", "위치정보 수집")
        }

        gpsTimelineView.adapter = GPSStampAdapter(timeline)
        gpsBackground = GPSStamper(this)

        button.setOnClickListener {
            gpsTimelineView.adapter?.notifyItemChanged(timeline.size)
        }


        gpsBackground?.initializeLocationManager()
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