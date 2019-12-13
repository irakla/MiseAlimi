package com.example.myapplication

import android.content.Context
import android.os.AsyncTask
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

typealias AirInfoType = JSONObject?

class DownloaderForAirInfo(private val context: Context)
    : AsyncTask<GPSTimeStamp, Void, AirInfoType>()
{
    private var timeUpdatedLocation: Long? = null

    override fun doInBackground(vararg params_TimeStamps: GPSTimeStamp): AirInfoType {
        println("Network 진입")
        var airInfo: AirInfoType = null
        var conn: HttpURLConnection? = null

        timeUpdatedLocation = params_TimeStamps[0].location.time

        try{
            val url = URL("http://115.86.172.10:3000/finedust/"
                    + "${params_TimeStamps[0].location.latitude}/${params_TimeStamps[0].location.longitude}")

            println("URL Setted by ${params_TimeStamps[0].location.latitude}, ${params_TimeStamps[0].location.longitude}")

            conn = url.openConnection() as HttpURLConnection

            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK)
                airInfo = getAirInfoFromURL(conn)
        }
        catch(e: UnknownHostException){
            // TODO : 연결실패 GUI 메시지 발생
            println("catch... UnknownHostException : ${e.toString()}")
        }
        catch(e: Exception){
            println("catch...${e.toString()}")
        }
        finally{
            conn?.disconnect()
        }

        println("현재 AirInfo : $airInfo")
        params_TimeStamps[0].airInfo = airInfo
        return airInfo
    }

    override fun onPostExecute(downloaded: AirInfoType) {
        GlobalScope.launch {
            timeUpdatedLocation?.let{ timeTarget -> downloaded?.let{ newAIrInfo->
                TimelineDBEntry(context).updateTimeStamp(timeTarget, newAIrInfo)
            }}
        }
    }

    private fun getAirInfoFromURL(conn: HttpURLConnection) : AirInfoType {
        var nowAirInfo: AirInfoType
        val rawJSONBuffer = StringBuffer()

        try{
            val isr = InputStreamReader(conn.inputStream, "UTF-8")
            val reader = BufferedReader(isr)

            reader.forEachLine {
                rawJSONBuffer.append(it)
            }

            nowAirInfo = JSONObject(rawJSONBuffer.toString())
        }
        catch(e: JSONException){
            println("Contents got is invalid json : ${rawJSONBuffer}")
            throw e
        }

        return nowAirInfo
    }
}