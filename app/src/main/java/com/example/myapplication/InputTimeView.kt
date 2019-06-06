package com.example.myapplication

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import com.example.myapplication.R
import kotlinx.android.synthetic.main.fragment_input_time.view.*

class InputTimeView(context: Context?) : ConstraintLayout(context) {
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
            val savedGetOutTime = preference.getString("GetOutTime", "9:00").split(":")
            val savedGetInTime = preference.getString("GetInTime", "18:00").split(":")

            getOutTime.hour = savedGetOutTime[0].toInt()
            getOutTime.minute = savedGetOutTime[1].toInt()
            getInTime.hour = savedGetInTime[0].toInt()
            getInTime.minute = savedGetInTime[1].toInt()
        }
    }
}