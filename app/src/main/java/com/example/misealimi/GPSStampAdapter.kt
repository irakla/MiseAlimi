package com.example.misealimi

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.gps_cell.view.*

class GPSStampAdapter(private val myDataset: MutableList<GPSTimeStamp>)
    : RecyclerView.Adapter<GPSStampAdapter.GPSTimeStampViewHolder>(){

    class GPSTimeStampViewHolder(val gpsCellView : RelativeLayout) : RecyclerView.ViewHolder(gpsCellView){
        val contentLine: TextView

        init{
            contentLine = gpsCellView.contentLine as TextView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GPSTimeStampViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.gps_cell, parent, false)
                as RelativeLayout

        return GPSTimeStampViewHolder(textView)
    }

    override fun getItemCount() = myDataset.size

    override fun onBindViewHolder(holder: GPSTimeStampViewHolder, position: Int) {
        holder.contentLine.text = "CONTENT"
    }
}