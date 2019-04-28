package com.example.misealimi

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView;

class GPSTimelineView : AppCompatActivity(){
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    var gpsTimeline : MutableList<GPSTimeStamp> = mutableListOf<GPSTimeStamp>()
        get() = field
        private set(newTimeLine:MutableList<GPSTimeStamp>) { field = newTimeLine }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gps_timeline)

        viewManager = LinearLayoutManager(this)
        //viewAdapter = MyAdapter(myDataset)

        recyclerView = findViewById<RecyclerView>(R.id.gpsTimelineView)
    }
}