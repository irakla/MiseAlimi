package com.example.misealimi

import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.gps_cell.view.*

class GPSTimeStampViewHolder(val gpsCellView : LinearLayout) : RecyclerView.ViewHolder(gpsCellView){
    val latitudeView: TextView
    val longitudeView: TextView
    val timeView: TextView

    init{
        latitudeView = gpsCellView.latitude
        longitudeView = gpsCellView.longitude
        timeView = gpsCellView.time
    }
}