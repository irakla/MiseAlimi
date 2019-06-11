package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_amount_info.*
import kotlinx.android.synthetic.main.fragment_input_time.view.*
import java.text.SimpleDateFormat

//for string-splitted time array
const val HOUR = 0
const val MINUTE = 1
const val DefaultGetOutTime = "9:00"
const val DefaultGetInTime = "18:00"

class Amount_Info : Fragment() {
    private var userName: String? = ""
    private var userAge: Int? = 0
    private var userWeight: Int? = 0
    private var userInspiRate: Int? = null

    private var getOutTime = DefaultGetOutTime.split(":")
    private var getInTime = DefaultGetInTime.split(":")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userName = arguments?.getString("name", userName)
        userAge = arguments?.getString("age", userAge.toString())?.toInt()
        userWeight = arguments?.getString("weight", userWeight.toString())?.toInt()

        if(userInspiRate == null) {
            userInspiRate = when (userAge) {
                in 0 .. 2 -> 33
                in 3.. 5 -> 25
                in 6 .. 9 -> 22
                in 10 until 20 -> 20
                in 20 until 65 -> 14
                in 65 until 80 -> 18
                in 80 .. Int.MAX_VALUE ->  22
                else -> -1
            }
        }

        val timePreference = activity?.getSharedPreferences("Time", Context.MODE_PRIVATE)
        if(timePreference != null) {
            getOutTime = timePreference.getString(getString(R.string.GetOutTime),
                DefaultGetOutTime).split(":")
            getInTime = timePreference.getString(getString(R.string.GetInTime),
                DefaultGetInTime).split(":")
        }

        return inflater.inflate(R.layout.fragment_amount_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonDetail.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                startActivity(Intent(activity, GPS_List::class.java))
            }
        })

        setOutsideTime()
        buttonOutsideTime.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                val builder = AlertDialog.Builder(activity)
                val inflater = activity?.layoutInflater

                if(inflater != null) {
                    val outTimeView = InputTimeView(activity)
                    builder.setView(outTimeView)

                    builder.setPositiveButton("확인", object: DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            val getOutTimeString = "${outTimeView.getOutTime.hour}:${outTimeView.getOutTime.minute}"
                            val getInTimeString = "${outTimeView.getInTime.hour}:${outTimeView.getInTime.minute}"
                            getOutTime = getOutTimeString.split(":")
                            getInTime = getInTimeString.split(":")

                            val timePreferenceEditor = activity?.getSharedPreferences("Time", Context.MODE_PRIVATE)?.edit()
                            timePreferenceEditor?.putString("GetOutTime", getOutTimeString)
                            timePreferenceEditor?.putString("GetInTime", getInTimeString)
                            timePreferenceEditor?.commit()

                            setOutsideTime()
                            setDustInspiration()
                        }
                    })
                }

                builder.show()
            }
        })

        if(userWeight == null || userInspiRate == null)
            return

        inspirationView.setText((7 * userWeight as Int * userInspiRate as Int).toString() + "mL")
    }

    private fun setOutsideTime(){
        var hourOnOut = getInTime[HOUR].toInt() - getOutTime[HOUR].toInt()
        if(hourOnOut < 0)
            hourOnOut += 24

        var minuteOnOut = getInTime[MINUTE].toInt() - getOutTime[MINUTE].toInt()
        if(minuteOnOut < 0)
            minuteOnOut += 60

        buttonOutsideTime.setText((if(hourOnOut != 0) hourOnOut.toString() + "시간"
        else "") + minuteOnOut.toString() + " 분간")
    }

    private fun setDustInspiration(){
        if(userWeight == null || userInspiRate == null)
            return

        var getOutIsPreviousDay =
            if(getOutTime[HOUR].toInt() > getInTime[HOUR].toInt())
                true
            else if(getOutTime[HOUR].toInt() == getInTime[HOUR].toInt()
                && getOutTime[MINUTE].toInt() > getInTime[MINUTE].toInt())
                true
            else
                false

        val time_OneDayMilis = 24 * 60 * 60 * 1000
        var timestr_GetOut = SimpleDateFormat("yyyy-MM-dd").
            format(System.currentTimeMillis() - if(getOutIsPreviousDay) time_OneDayMilis else 0 ) +
                " ${getOutTime[HOUR]}:${getOutTime[MINUTE]}"
        var time_GetOut = SimpleDateFormat("yyyy-MM-dd hh:mm").parse(timestr_GetOut).time

        var timestr_GetIn = SimpleDateFormat("yyyy-MM-dd").
            format(System.currentTimeMillis()) +
                " ${getInTime[HOUR]}:${getInTime[MINUTE]}"
        var time_GetIn = SimpleDateFormat("yyyy-MM-dd hh:mm").parse(timestr_GetIn).time


        val timelineOnOutside = GPSTimelineManager.getTimeStampsInTheTime(
            getOutTime[HOUR].toInt(), getOutTime[MINUTE].toInt(),
            getInTime[HOUR].toInt(), getInTime[MINUTE].toInt()).reversed()
        var timestampLastOnOutside : GPSTimeStamp? = null


        var tidalVolumePerMinute_mL = 7 * userWeight as Int * userInspiRate as Int
        var prevMilliTime: Long? = null
        var prevAirInfo: AirInfoType? = null
        var finedustByInspiration: Double = 0.0

        var inspirationTimeByMinute : Double = 0.0
        var inspirationVolume_mL : Double = 0.0
        /*timelineOnOutside.forEach {

            if(it == null || prevAirInfo == null || it.airInfo == null)
                return@forEach

            if(prevMilliTime == null){
                prevMilliTime = time_GetOut
                prevAirInfo = it.airInfo
                return@forEach
            }

            inspirationTimeByMinute = (it.location.time - prevMilliTime as Long) / 60000.toDouble()

            inspirationVolume_mL = inspirationTimeByMinute * tidalVolumePerMinute_mL

            if(prevAirInfo != null) {
                val pm10 = prevAirInfo?.getString(context?.getString(R.string.PM10))?.toInt()
                finedustByInspiration += inspirationVolume_mL * if(pm10 != null) pm10 else 0
            }

            prevMilliTime = it.location.time
            prevAirInfo = it.airInfo
            timestampLastOnOutside = it

            println("분당호흡량 : ${tidalVolumePerMinute_mL}, 호흡시간 : ${inspirationTimeByMinute}, pm10 : ${pm10}")
        }

        //마지막 timestamp ~ 귀가 시간
        if(timestampLastOnOutside != null) {
            inspirationTimeByMinute =
                ((timestampLastOnOutside as GPSTimeStamp).location.time - prevMilliTime as Long) / 60000.toDouble()

            inspirationVolume_mL = inspirationTimeByMinute * tidalVolumePerMinute_mL

            val pm10 = prevAirInfo?.getString(context?.getString(R.string.PM10))?.toInt()
            finedustByInspiration += inspirationVolume_mL * if (pm10 != null) pm10 else 0
        }*/

        var flagIsNotGot = true
        val time_OnOutsideByMinute = (time_GetIn - time_GetOut) / 60000.toDouble()
        GPSTimelineManager.gpsTimeline.forEach{
            if(it.airInfo == null)
                return@forEach

            val pm10 = it.airInfo?.getString("pm10Value")?.toInt()

            if(pm10 == null)
                return@forEach

            if(flagIsNotGot) {
                finedustByInspiration = time_OnOutsideByMinute * tidalVolumePerMinute_mL * pm10.toInt()
                flagIsNotGot = false
            }
        }

        finedustByInspiration /= 1000000                                         //mL 보정
        inspirationView.setText(String.format("%,.2fμg", finedustByInspiration))
    }
}
