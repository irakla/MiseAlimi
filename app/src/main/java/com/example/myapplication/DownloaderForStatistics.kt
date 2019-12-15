package com.example.myapplication

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.example.myapplication.ui.MainPageActivity
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

typealias StatsMap = Map<String, Double>

class DownloaderForStatistics(
    private val context: Context
    , private val doingWithStats: (Map<String, Double>) -> Unit
)
    : AsyncTask<Void, Void, StatsMap>()
{
    private var timeUpdatedLocation: Long? = null

    companion object{
        const val STATS_KEY_PRIVATE_GREATEST_PM10 = "PrivateGreatestPM10"
        const val STATS_KEY_PRIVATE_GREATEST_PM2_5 = "PrivateGreatestPM2.5"
        const val STATS_KEY_PRIVATE_LEAST_PM10 = "PrivateLeastPM10"
        const val STATS_KEY_PRIVATE_LEAST_PM2_5 = "PrivateLeastPM2.5"

        const val STATS_KEY_AVERAGE_PM10 = "AveragePM10"
        const val STATS_KEY_AVERAGE_PM2_5 = "AveragePM2.5"
        const val STATS_KEY_GREATEST_PM10 = "GreatestPM10"
        const val STATS_KEY_GREATEST_PM2_5 = "GreatestPM2.5"
    }

    override fun doInBackground(vararg params_TimeStamps: Void): StatsMap {
        Log.d("Download Statistics", "개시")
        val mapStats = mutableMapOf<String, Double>()

        val userName = context.getSharedPreferences(MainPageActivity.PREFERENCE_USER, Context.MODE_PRIVATE)
            .getString("name", "")

        if(userName == "")
            return mapStats

        val tailMyStats = "statistics/mystats/$userName"
        val tailOtherStats = "statistics/others/$userName"

        runBlocking {
            val dataMyStats =
                async(start = CoroutineStart.DEFAULT) { getStatsData(tailMyStats) }.await()
            val dataOtherStats =
                async(start = CoroutineStart.DEFAULT) { getStatsData(tailOtherStats) }.await()

            try {
                dataMyStats?.let {
                    mapStats["PrivateGreatestPM10"] = dataMyStats.getDouble("bigInDust")
                    mapStats["PrivateGreatestPM2.5"] = dataMyStats.getDouble("bigInFineDust")
                    mapStats["PrivateLeastPM10"] = dataMyStats.getDouble("smallInDust")
                    mapStats["PrivateLeastPM2.5"] = dataMyStats.getDouble("smallInFineDust")
                }
                dataOtherStats?.let{
                    mapStats["AveragePM10"] = dataOtherStats.getDouble("dustAverage")
                    mapStats["AveragePM2.5"] = dataOtherStats.getDouble("finedustAverage")
                    mapStats["GreatestPM10"] = dataOtherStats.getDouble("topIndust")
                    mapStats["GreatestPM2.5"] = dataOtherStats.getDouble("topInfinedust")
                }
            }catch(e: JSONException){
                Log.d("Download Stats", "JSON Error", e)
            }
        }

        println("현재 StatsData : $mapStats")
        return mapStats
    }

    override fun onPostExecute(downloaded: StatsMap) {
        if(downloaded.isEmpty()) return

        doingWithStats(downloaded)
    }

    suspend fun getStatsData(urlTail: String) : JSONObject? {
        var conn: HttpURLConnection? = null
        var statsData: JSONObject? = null

        try{
            val url = URL("http://115.86.172.10:3000/$urlTail")

            conn = url.openConnection() as HttpURLConnection

            if(conn.responseCode == HttpURLConnection.HTTP_OK){
                statsData = getStatsJSONFromURL(conn)
            }
        }
        catch(e: UnknownHostException){
            Log.d("Download Statistics", "Connection Failed", e)
        }
        catch(e: Exception){
            Log.d("Download Statistics", "Exception", e)
        }
        finally{
            conn?.disconnect()
        }

        return statsData
    }

    private fun getStatsJSONFromURL(conn: HttpURLConnection) : JSONObject? {
        var nowStatsData: JSONObject?
        val rawJSONBuffer = StringBuffer()

        try{
            val isr = InputStreamReader(conn.inputStream, "UTF-8")
            val reader = BufferedReader(isr)

            reader.forEachLine {
                rawJSONBuffer.append(it)
            }

            nowStatsData = JSONObject(rawJSONBuffer.toString())
        }
        catch(e: JSONException){
            println("Contents got is invalid json : ${rawJSONBuffer}")
            throw e
        }

        return nowStatsData
    }
}