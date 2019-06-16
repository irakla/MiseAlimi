package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
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

    private var finedustByInspiration: Double = 0.0
    private val observersForInspiration: MutableList<Observer> = mutableListOf()

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

        println("amount의 viewgroup : ${container})")

        return inflater.inflate(R.layout.fragment_amount_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonLocationLog.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                if(GPS_List.listIsNotShowing)
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
                            timePreferenceEditor?.apply()

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

        setDustInspiration()
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

        val hourGetOutTime = getOutTime[HOUR].toInt()
        val minuteGetOutTime = getOutTime[MINUTE].toInt()
        val hourGetInTime = getInTime[HOUR].toInt()
        val minuteGetInTime = getInTime[MINUTE].toInt()

        val outTimeIsYesterday = hourGetOutTime > hourGetInTime
                || (hourGetOutTime == hourGetInTime && minuteGetOutTime > minuteGetInTime)

        val milliTimeGetOut = TimeSupporter.getTheLatestMilliTime(outTimeIsYesterday, hourGetOutTime, minuteGetOutTime)
        val milliTimeGetIn = TimeSupporter.getTheLatestMilliTime(false, hourGetInTime, minuteGetInTime)

        val timelineOnOutside = GPSTimelineManager.getTimeStampsInTheTime(
            hourGetOutTime, minuteGetOutTime,
            hourGetInTime, minuteGetInTime
        )
        val fortestlist = mutableListOf(timelineOnOutside)

        var tidalVolumePerMinute_mL = 7 * userWeight as Int * userInspiRate as Int

        //위치 값 반영
        var timeNextTimeStamp = milliTimeGetIn
        val airInfoGetOutTime = timelineOnOutside.last()?.airInfo

        timelineOnOutside.dropLast(1).forEach{
            if(it == null)
                return@forEach

            val pm10str = it.airInfo?.getString("pm10Value")
            val pm10 = if(pm10str == "-" || pm10str == null) null else pm10str.toInt()
            pm10?: return@forEach

            val timeInterval = (timeNextTimeStamp - it.location.time).toDouble() / MINUTE_BY_MILLI_SEC

            finedustByInspiration += timeInterval * tidalVolumePerMinute_mL * pm10.toInt()
            timeNextTimeStamp = it.location.time
        }

        if(airInfoGetOutTime != null){
            val pm10str = airInfoGetOutTime.getString("pm10Value")
            val pm10 = if(pm10str == "-" || pm10str == null) null else pm10str.toInt()

            if(pm10 != null) {
                val timeInterval = (timeNextTimeStamp - milliTimeGetOut).toDouble() / MINUTE_BY_MILLI_SEC
                finedustByInspiration += timeInterval * tidalVolumePerMinute_mL * pm10.toInt()
            }
        }

        finedustByInspiration /= 1000000                    //mL 보정
        inspirationView.text = String.format("%,.2fμg", finedustByInspiration)
        //위치 값 반영

        //초안(외출시간 * 첫번째 값 미세먼지)
        var flagIsNotGot = true
        val time_OnOutsideByMinute = (
                TimeSupporter.getTheLatestMilliTime(false, hourGetInTime, minuteGetInTime) -
                        TimeSupporter.getTheLatestMilliTime(outTimeIsYesterday, hourGetOutTime, minuteGetOutTime)
        ) / MINUTE_BY_MILLI_SEC.toDouble()
        GPSTimelineManager.gpsTimeline.forEach{
            if(it.airInfo == null)
                return@forEach

            val pm10str = it.airInfo?.getString("pm10Value")
            val pm10 = if(pm10str == "-" || pm10str == null) null else pm10str.toInt()

            if(pm10 == null)
                return@forEach

            if(flagIsNotGot) {
                finedustByInspiration = time_OnOutsideByMinute * tidalVolumePerMinute_mL * pm10.toInt()
                flagIsNotGot = false
            }
        }

        finedustByInspiration /= 1000000                                         //mL 보정
        expectationText.setText(String.format("%,.2fμg", finedustByInspiration))
        //초안(외출시간 * 첫번째 값 미세먼지)

        observersForInspiration.forEach{ it.update(finedustByInspiration) }
    }

    fun addInspirationObserver(observer: Observer){
        if(!observersForInspiration.contains(observer))
            observersForInspiration.add(observer)
    }

    fun delInspirationObserver(observer: Observer){
        if(observersForInspiration.contains(observer))
            observersForInspiration.remove(observer)
    }
}
