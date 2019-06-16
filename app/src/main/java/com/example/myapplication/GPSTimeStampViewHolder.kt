package com.example.myapplication

import android.content.Intent
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.gps_cell.view.*

class GPSTimeStampViewHolder(gpsCellView : LinearLayout) : RecyclerView.ViewHolder(gpsCellView){
    val latitudeView: TextView
    val longitudeView: TextView
    val timeView: TextView
    val airInfoView: TextView

    init{
        latitudeView = gpsCellView.latitude
        longitudeView = gpsCellView.longitude
        timeView = gpsCellView.time
        airInfoView = gpsCellView.air
    }
}