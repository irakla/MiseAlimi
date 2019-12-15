package com.example.myapplication.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.*
import kotlinx.android.synthetic.main.fragment_amount_info.*
import kotlinx.android.synthetic.main.fragment_input_time.view.*
import org.jetbrains.anko.runOnUiThread

//for string-splitted time array
const val HOUR = 0
const val MINUTE = 1
const val DefaultGetOutTime = "9:00"
const val DefaultGetInTime = "18:00"

class AmountInfoFragment : Fragment() {
    private var userName: String? = ""
    private var userAge: Int? = 0
    private var userWeight: Int? = 0
    private var userTidalVolume: Int? = null

    private var getOutTime = DefaultGetOutTime.split(":")
    private var getInTime = DefaultGetInTime.split(":")

    private var finedustByInspiration: Double = 0.0
    private var finedust25ByInspiration: Double = 0.0
    private val observersForInspiration: MutableList<InspirationObserver> = mutableListOf()

    companion object{
        const val TIME_PREFERENCE_NAME = "Time"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            userName = arguments?.getString("name", userName)
            userAge = arguments?.getInt("age", 0)
            userWeight = arguments?.getInt("weight", 0)
            userTidalVolume = arguments?.getInt("tidalVolume", 0)
        } ?: let { Log.d("AmountInfo", "User값 전달되지 않음"); return null }

        val timePreference = activity?.getSharedPreferences(TIME_PREFERENCE_NAME, Context.MODE_PRIVATE)
        if(timePreference != null) {
            val stringGetOutTime
                    = timePreference.getString(getString(R.string.GetOutTime),
                DefaultGetOutTime
            )
            val stringGetInTime
                    = timePreference.getString(getString(R.string.GetInTime),
                DefaultGetInTime
            )

            getOutTime =
                if(stringGetOutTime != null)
                    stringGetOutTime.split(":")
                else{
                    Log.d("AmountInfo onCreate", "GetOutTimeString is invalid : ${stringGetOutTime.toString()}")
                    DefaultGetOutTime.split(":")
                }

            getInTime =
                if(stringGetInTime != null)
                    stringGetInTime.split(":")
                else{
                    Log.d("AmountInfo onCreate", "GetInTimeString is invalid : ${stringGetInTime.toString()}")
                    DefaultGetInTime.split(":")
                }
        }

        Log.d("amount의 viewgroup", "$container")

        return inflater.inflate(R.layout.fragment_amount_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonLocationLog.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                if(LocationListActivity.listIsNotShowing)
                    startActivity(Intent(activity, LocationListActivity::class.java))
            }
        })

        initializeButtonOutSideTime()
        setOutsideTime()

        buttonDetail.setOnClickListener {
            val hourGetOutTime = getOutTime[HOUR].toInt()
            val minuteGetOutTime = getOutTime[MINUTE].toInt()
            val hourGetInTime = getInTime[HOUR].toInt()
            val minuteGetInTime = getInTime[MINUTE].toInt()
            val getOutIsYesterday =
                TimeSupporter.isYesterdayGetOut(
                    hourGetOutTime, minuteGetOutTime,
                    hourGetInTime, minuteGetInTime
                )

            val detailDialog = InspirationDetailDialog(
                activity,
                TimeSupporter.getTheLatestMilliTime(
                    getOutIsYesterday,
                    hourGetOutTime,
                    minuteGetOutTime
                ),
                TimeSupporter.getTheLatestMilliTime(
                    false,
                    hourGetInTime,
                    minuteGetInTime
                ),
                finedustByInspiration,
                finedust25ByInspiration
            )

            detailDialog.show()
        }

        if(userTidalVolume == null)
            return

        setDustInspiration()
    }

    private fun initializeButtonOutSideTime(){
        buttonOutsideTime.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                val builder = AlertDialog.Builder(activity)
                val inflater = activity?.layoutInflater

                if(inflater != null) {
                    val outTimeView =
                        InputTimeView(activity)
                    builder.setView(outTimeView)

                    builder.setPositiveButton("확인", object: DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            val getOutTimeString = "${outTimeView.getOutTime.hour}:${outTimeView.getOutTime.minute}"
                            val getInTimeString = "${outTimeView.getInTime.hour}:${outTimeView.getInTime.minute}"
                            getOutTime = getOutTimeString.split(":")
                            getInTime = getInTimeString.split(":")

                            val timePreferenceEditor = activity?.getSharedPreferences(TIME_PREFERENCE_NAME, Context.MODE_PRIVATE)?.edit()
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
        if(userTidalVolume == null)
            return

        val hourGetOutTime = getOutTime[HOUR].toInt()
        val minuteGetOutTime = getOutTime[MINUTE].toInt()
        val hourGetInTime = getInTime[HOUR].toInt()
        val minuteGetInTime = getInTime[MINUTE].toInt()

        val outTimeIsYesterday = hourGetOutTime > hourGetInTime
                || (hourGetOutTime == hourGetInTime && minuteGetOutTime > minuteGetInTime)

        val milliTimeGetOut =
            TimeSupporter.getTheLatestMilliTime(
                outTimeIsYesterday,
                hourGetOutTime, minuteGetOutTime
            )
        val milliTimeGetIn =
            TimeSupporter.getTheLatestMilliTime(
                false,
                hourGetInTime, minuteGetInTime
            )

        val dbEntry = context?.let { context -> TimelineDBEntry(context) }

        val timelineOnOutside = dbEntry?.loadTimelinePeriodOrderByLatest(
            milliTimeGetOut, milliTimeGetIn
        ) { timelineOnOutside ->
            //위치 값 반영
            var timeNextTimeStamp = milliTimeGetIn

            var nowPM10 = 0
            var nowPM25 = 0
            timelineOnOutside.forEach {
                val nowLocation = it.first
                val nowAirInfo = it.second

                val pm10str = nowAirInfo?.getString("pm10Value")
                nowPM10 = if (pm10str == "-" || pm10str == null) 0 else pm10str.toInt()

                val pm25str = nowAirInfo?.getString("pm25Value")
                nowPM25 = if (pm25str == "-" || pm25str == null) 0 else pm25str.toInt()

                val timeInterval =
                    (timeNextTimeStamp - nowLocation.time).toDouble() / MINUTE_BY_MILLI_SEC

                userTidalVolume?.let { nowUserTidalVolume ->
                    finedustByInspiration += timeInterval * nowUserTidalVolume * nowPM10
                    finedust25ByInspiration += timeInterval * nowUserTidalVolume * nowPM25
                }
                timeNextTimeStamp = nowLocation.time
            }
            val timeInterval =
                (timeNextTimeStamp - milliTimeGetOut).toDouble() / MINUTE_BY_MILLI_SEC

            userTidalVolume?.let { nowUserTidalVolume ->
                finedustByInspiration += timeInterval * nowUserTidalVolume * nowPM10
                finedust25ByInspiration += timeInterval * nowUserTidalVolume * nowPM25
            }

            finedustByInspiration /= 1000000.0                    //mL 보정
            finedust25ByInspiration /= 1000000.0

            context?.runOnUiThread {
                inspirationView.text =
                    String.format("%,.2fμg", finedustByInspiration + finedust25ByInspiration)
            }
        }

        observersForInspiration.forEach{
            it.update(
                finedustByInspiration, finedust25ByInspiration
                , milliTimeGetIn, milliTimeGetOut
            )
        }
    }

    fun addInspirationObserver(inspirationObserver: InspirationObserver){
        if(!observersForInspiration.contains(inspirationObserver))
            observersForInspiration.add(inspirationObserver)
    }

    fun delInspirationObserver(inspirationObserver: InspirationObserver){
        if(observersForInspiration.contains(inspirationObserver))
            observersForInspiration.remove(inspirationObserver)
    }
}
