package com.example.misealimi

import android.databinding.*
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.gps_cell.view.*

class GPSStampAdapter(private var timelineDataset: ObservableArrayList<GPSTimeStamp>)
    : RecyclerView.Adapter<GPSTimeStampViewHolder>()
    {

    init{
        timelineDataset.addOnListChangedCallback(object:
            ObservableList.OnListChangedCallback<ObservableArrayList<GPSTimeStamp>>() {
            override fun onItemRangeRemoved(
                sender: ObservableArrayList<GPSTimeStamp>?, positionStart: Int, itemCount: Int
            ) = notifyItemRangeRemoved(positionStart, itemCount)

            override fun onItemRangeMoved(
                sender: ObservableArrayList<GPSTimeStamp>?,
                fromPosition: Int, toPosition: Int, itemCount: Int
            ) = notifyItemMoved(fromPosition, toPosition)

            override fun onItemRangeInserted(
                sender: ObservableArrayList<GPSTimeStamp>?, positionStart: Int, itemCount: Int
            ) = notifyItemRangeInserted(positionStart, itemCount)

            override fun onItemRangeChanged(
                sender: ObservableArrayList<GPSTimeStamp>?, positionStart: Int, itemCount: Int
            ) = notifyItemRangeChanged(positionStart, itemCount)

            override fun onChanged(sender: ObservableArrayList<GPSTimeStamp>?
            ) = notifyDataSetChanged()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GPSTimeStampViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val gpsCell = inflater.inflate(R.layout.gps_cell, parent, false) as LinearLayout

        return GPSTimeStampViewHolder(gpsCell)
    }

    override fun getItemCount() = timelineDataset.size

    override fun onBindViewHolder(holder: GPSTimeStampViewHolder, position: Int) {
        holder.latitudeView.setText(String.format("%.3f", timelineDataset[position].location.latitude) + ", ")
        holder.longitudeView.setText(String.format("%.3f", timelineDataset[position].location.longitude))
        holder.timeView.setText(timelineDataset[position].theTimeNotification)
    }
}