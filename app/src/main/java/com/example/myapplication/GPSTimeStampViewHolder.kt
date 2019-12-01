package com.example.myapplication

import androidx.recyclerview.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.gps_cell.view.*

class GPSTimeStampViewHolder(gpsCellView : LinearLayout) : RecyclerView.ViewHolder(gpsCellView){
    val latitudeView: TextView
    val longitudeView: TextView
    val timeView: TextView
    val airInfoView: TextView
    val finedustView: TextView

    init{
        latitudeView = gpsCellView.latitude
        longitudeView = gpsCellView.longitude
        timeView = gpsCellView.time
        airInfoView = gpsCellView.airInfoTime
        finedustView = gpsCellView.finedustInfo
    }
}