package com.example.myapplication.ui

import android.content.Context
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.LayoutInflater
import com.example.myapplication.R
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
            val stringGetOutTime = preference.getString("GetOutTime",
                DefaultGetOutTime
            )
            val stringGetInTime = preference.getString("GetInTime",
                DefaultGetInTime
            )

            val savedGetOutTime =
                if(stringGetOutTime != null)
                    stringGetOutTime.split(":")
                else{
                    Log.d("AmountInfo onCreate", "GetOutTimeString is invalid : ${stringGetOutTime.toString()}")
                    DefaultGetOutTime.split(":")
                }

            val savedGetInTime =
                if(stringGetInTime != null)
                    stringGetInTime.split(":")
                else{
                    Log.d("AmountInfo onCreate", "GetInTimeString is invalid : ${stringGetInTime.toString()}")
                    DefaultGetInTime.split(":")
                }

            getOutTime.hour = savedGetOutTime[HOUR].toInt()
            getOutTime.minute = savedGetOutTime[MINUTE].toInt()
            getInTime.hour = savedGetInTime[HOUR].toInt()
            getInTime.minute = savedGetInTime[MINUTE].toInt()
        }
    }
}