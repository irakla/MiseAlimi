package com.example.myapplication

import android.databinding.*
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

class GPSStampAdapter(
    private val activity: AppCompatActivity,
    private var timelineDataset: ObservableArrayList<GPSTimeStamp>,
    private val viewShowing: ViewGroup)
    : RecyclerView.Adapter<GPSTimeStampViewHolder>()
    {

    init {
        timelineDataset.addOnListChangedCallback(object :
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
            ) {
                val scrollOnListViewIsTop = !viewShowing.canScrollVertically(-1)

                notifyItemRangeInserted(positionStart, itemCount)

                //scroll to top
                if(viewShowing is RecyclerView && scrollOnListViewIsTop)
                    viewShowing.scrollToPosition(0)
            }

            override fun onItemRangeChanged(
                sender: ObservableArrayList<GPSTimeStamp>?, positionStart: Int, itemCount: Int
            ) = notifyItemRangeChanged(positionStart, itemCount)

            override fun onChanged(
                sender: ObservableArrayList<GPSTimeStamp>?
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
        val nowTimeStamp = timelineDataset[position]

        holder.latitudeView.setText(String.format("%.3f", nowTimeStamp.location.latitude) + ", ")
        holder.longitudeView.setText(String.format("%.3f", nowTimeStamp.location.longitude))
        holder.timeView.setText(nowTimeStamp.theTimeNotification)
        holder.airInfoView.setText("측정시간 : ${nowTimeStamp.airInfo?.getString("dataTime")} "
            + "pm10 : ${nowTimeStamp.airInfo?.getString("pm10Value")}ppm, " +
                "pm2.5 : ${nowTimeStamp.airInfo?.getString("pm25Value")}ppm")

        holder.itemView.setOnClickListener(nowTimeStamp)
    }
}