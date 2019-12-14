package com.example.myapplication

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.work.*
import com.example.myapplication.ui.AmountInfoFragment.Companion.TIME_PREFERENCE_NAME
import com.example.myapplication.ui.DefaultGetInTime
import com.example.myapplication.ui.DefaultGetOutTime
import com.example.myapplication.ui.MainPageActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class PeriodicUploader(private val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    companion object{
        private const val UPDATE_WORK_TAG = "UpdateWork"
        const val UPLOAD_PREFERENCE = "UploadPreference"
        const val UPLOAD_PREFERENCE_TIME = "UploadTime"

        fun enqueueUploadWorker(context: Context){
            val uploadWorker = PeriodicWorkRequestBuilder<PeriodicUploader>(
                1, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UPDATE_WORK_TAG, ExistingPeriodicWorkPolicy.KEEP, uploadWorker
            )
        }
    }

    override fun doWork(): Result {
        val userPreference =
            context.getSharedPreferences(MainPageActivity.PREFERENCE_USER, Context.MODE_PRIVATE)
        var userTidalVolumePerMinuteBymL = userPreference.getInt("tidalVolume", 0)
        if(userTidalVolumePerMinuteBymL == 0)        //it couldn't get user information
            return Result.success()

        val timePreference = context.getSharedPreferences(TIME_PREFERENCE_NAME, Context.MODE_PRIVATE)
        var milliGetOutTime = 0L
        var milliGetInTime = 0L

        //calculate get out&in milli time
        timePreference?.let{
            val stringGetOutTime
                    = timePreference.getString(
                context.getString(R.string.GetOutTime), DefaultGetOutTime
            )
            val stringGetInTime
                    = timePreference.getString(
                context.getString(R.string.GetInTime), DefaultGetInTime
            )

            stringGetInTime?.let {
                milliGetInTime = getMilliTimeFromFormatted(stringGetInTime)
            } ?: let{  Log.d("UploadTime formal", stringGetInTime); return Result.success() }
            stringGetOutTime?.let {
                milliGetOutTime = getMilliTimeFromFormatted(stringGetOutTime)
                if(milliGetOutTime > milliGetInTime)
                    milliGetOutTime = getMilliTimeFromFormatted(stringGetOutTime, true)
            } ?: let{  Log.d("UploadTime formal", stringGetOutTime); return Result.success() }
        }

        if(isUploadTime(milliGetInTime)) {
            Log.d("Upload Worker", "Upload Canceled")
            return Result.success()
        }

        val dbEntry = TimelineDBEntry(context)

        val startSpotInfo = dbEntry.loadTimelineLatestTheTime(milliGetOutTime)
        val prevSpot = startSpotInfo?.first

        var finedustByInspiration: Double = if(startSpotInfo?.second == null) 0.0
            else startSpotInfo.second!!.getString("pm10Value").toDouble()

        var finedust25ByInspiration: Double = if(startSpotInfo?.second == null) 0.0
            else startSpotInfo.second!!.getString("pm25Value").toDouble()

        var timeNextTimeStamp = milliGetInTime
        dbEntry.loadTimelinePeriodOrderByLatest(milliGetOutTime, milliGetInTime){ timelineOutside ->
            var countLocations = timelineOutside.count()
            var centerLatitude: Double
            var centerLongitude: Double

            if(prevSpot == null) {
                centerLatitude = 0.0
                centerLongitude = 0.0
            }
            else{
                countLocations += 1
                centerLatitude = prevSpot.latitude
                centerLongitude = prevSpot.longitude
            }

            var nowPM10: Int = 0
            var nowPM25: Int = 0
            timelineOutside.forEach {
                val nowLocation = it.first
                val nowAirInfo = it.second

                val pm10str = nowAirInfo?.getString("pm10Value")
                nowPM10 = if(pm10str == "-" || pm10str == null) 0 else pm10str.toInt()

                val pm25str = nowAirInfo?.getString("pm25Value")
                nowPM25 = if(pm25str == "-" || pm25str == null) 0 else pm25str.toInt()

                val timeInterval = (timeNextTimeStamp - nowLocation.time).toDouble() / MINUTE_BY_MILLI_SEC

                finedustByInspiration += timeInterval * userTidalVolumePerMinuteBymL * nowPM10
                finedust25ByInspiration += timeInterval * userTidalVolumePerMinuteBymL * nowPM25
                centerLatitude += nowLocation.latitude
                centerLongitude += nowLocation.longitude
                timeNextTimeStamp = nowLocation.time
            }

            //add 'from just out to first place'
            val timeIntervalFromGetOutToFirst =
                (timeNextTimeStamp - milliGetOutTime).toDouble() / MINUTE_BY_MILLI_SEC
            finedustByInspiration += timeIntervalFromGetOutToFirst * userTidalVolumePerMinuteBymL * nowPM10
            finedust25ByInspiration += timeIntervalFromGetOutToFirst * userTidalVolumePerMinuteBymL * nowPM25

            finedustByInspiration /= 1000000                    //mL 보정
            finedust25ByInspiration /= 1000000
            centerLatitude /= countLocations
            centerLongitude /= countLocations

            UploadTask(
                finedustByInspiration
                , finedust25ByInspiration
                , centerLatitude
                , centerLongitude
                , context
            ).execute()
        }

        return Result.success()
    }

    //the format : hh:mm
    private fun getMilliTimeFromFormatted(formattedTime: String, isYesterday: Boolean = false)
            : Long{
        var hour = 0
        var minute = 0

        formattedTime.split(":").let{
            hour = it[0].toInt()
            minute = it[1].toInt()
        }

        return TimeSupporter.getTheLatestMilliTime(isYesterday, hour, minute)
    }

    private fun isUploadTime(milliGetInTime: Long) : Boolean {
        val nowTime = System.currentTimeMillis()
        val prevUploadTime =
            context.getSharedPreferences(UPLOAD_PREFERENCE, Context.MODE_PRIVATE)
                .getLong(UPLOAD_PREFERENCE_TIME, 0)
        val isOnOutside = nowTime <= milliGetInTime
        val isCompletedTodayUpload = nowTime / DAY_BY_MILLI_SEC > prevUploadTime / DAY_BY_MILLI_SEC

        return isOnOutside || isCompletedTodayUpload
    }

    private class UploadTask(
        inspirationPM10: Double
        , inspirationPM25: Double
        , centerLatitude: Double
        , centerLongitude: Double
        , private val context: Context
    ) : AsyncTask<String, Void, String>(){
        private val updateURLString = "http://115.86.172.10:3000/finedust" +
                "/${System.currentTimeMillis()}/$inspirationPM10/$inspirationPM25" +
                "/$centerLatitude/$centerLongitude"

        override fun doInBackground(vararg p0: String?): String {
            var conn: HttpURLConnection? = null
            var response = StringBuilder()

            try{
                Log.d("UpdateTask", updateURLString)
                conn = URL(updateURLString).openConnection() as? HttpURLConnection

                if(conn?.responseCode == HttpURLConnection.HTTP_OK) {
                    val isr = InputStreamReader(conn.inputStream)
                    val reader = BufferedReader(isr)

                    reader.forEachLine { response.append(it) }

                    context.getSharedPreferences(UPLOAD_PREFERENCE, Context.MODE_PRIVATE).edit()
                        .putLong(UPLOAD_PREFERENCE_TIME, System.currentTimeMillis())
                        .apply()
                }
            }catch (e: Exception){
                Log.d("Upload Task", "연결 실패", e)
            }finally {
                conn?.disconnect()
            }

            return response.toString()
        }

        override fun onPostExecute(result: String?) {
            result?.let{ println("Upload Response : " + result.toString()) }
        }
    }
}