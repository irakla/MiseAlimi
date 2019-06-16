package com.example.myapplication

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.view.View
import android.widget.TimePicker
import kotlinx.android.synthetic.main.fragment_input_time.view.*

class InputTimeView(context: Context?) : ConstraintLayout(context){
    init{
        val inflaterService = Context.LAYOUT_INFLATER_SERVICE
        val inflater = context?.getSystemService(inflaterService) as LayoutInflater
        val view = inflater.inflate(R.layout.fragment_input_time, this, false)
        addView(view)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val preference = context.getSharedPreferences("Time", Context.MODE_PRIVATE)
        if(preference != null) {
            val savedGetOutTime = preference.getString("GetOutTime", DefaultGetOutTime).split(":")
            val savedGetInTime = preference.getString("GetInTime", DefaultGetInTime).split(":")

            getOutTime.hour = savedGetOutTime[HOUR].toInt()
            getOutTime.minute = savedGetOutTime[MINUTE].toInt()
            getInTime.hour = savedGetInTime[HOUR].toInt()
            getInTime.minute = savedGetInTime[MINUTE].toInt()
        }
    }
}