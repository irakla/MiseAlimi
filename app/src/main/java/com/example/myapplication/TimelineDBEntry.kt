package com.example.myapplication

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.util.Log
import androidx.core.database.getStringOrNull
import com.example.myapplication.TimelineDBHelper.Companion.DB_ATTR_AIRJSON
import com.example.myapplication.TimelineDBHelper.Companion.DB_ATTR_LATITUDE
import com.example.myapplication.TimelineDBHelper.Companion.DB_ATTR_LONGITUDE
import com.example.myapplication.TimelineDBHelper.Companion.DB_ATTR_PROVIDER
import com.example.myapplication.TimelineDBHelper.Companion.DB_ATTR_TIME
import com.example.myapplication.TimelineDBHelper.Companion.DB_TABLE_NAME
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class TimelineDBEntry(private val context: Context) {
    private val timelineDBHelper = TimelineDBHelper(context)

    companion object{
        private var DB_INSTANCE: SQLiteDatabase? = null
    }

    init {
        synchronized(TimelineDBEntry::class) {
            DB_INSTANCE?.let {
                DB_INSTANCE =
                    context.applicationContext
                        .openOrCreateDatabase(TimelineDBHelper.DB_NAME, MODE_PRIVATE, null)
                //DB_INSTANCE.set
            }
        }
    }

    fun loadLatestTimelineInPeriod(thePeriod: Long) : List<Pair<Location, AirInfoType>> {
        val timelineDatabase = timelineDBHelper.readableDatabase
        val timeFrom = System.currentTimeMillis() - thePeriod

        Log.d("database", timelineDatabase.isOpen().toString())

        val resultTimeline = mutableListOf<Pair<Location, AirInfoType>>()

        GlobalScope.launch {
            DB_INSTANCE?.let { db ->
                val cursor = db.rawQuery(
                    "SELECT * FROM $DB_TABLE_NAME "
                    //+ "WHERE ${TimelineDBHelper.DB_ATTR_TIME} > $timeFrom"
                    , null
                )

                while (cursor.moveToNext()) {
                    val airInfoString = cursor.getStringOrNull(3)
                    resultTimeline.add(
                        Pair(
                            Location(cursor.getString(4)).apply {
                                //provider
                                time = cursor.getLong(0)
                                latitude = cursor.getDouble(1)
                                longitude = cursor.getDouble(2)
                            }
                            , if (airInfoString == "null") null else JSONObject(airInfoString))
                    )
                }

                timelineDatabase.close()
            }
        }

        return resultTimeline
    }

    fun insertTimeStamp(location: Location, airInfo: AirInfoType = null)
            = GlobalScope.launch {
        val insertStatement = "INSERT INTO $DB_TABLE_NAME" +
                "($DB_ATTR_TIME, $DB_ATTR_LATITUDE, $DB_ATTR_LONGITUDE, $DB_ATTR_AIRJSON, $DB_ATTR_PROVIDER) " +
                "VALUES(" +
                "${location.time}" +
                ", ${location.latitude}" +
                ", ${location.longitude}" +
                ", \'${if (airInfo == null) null else airInfo.toString()}\'" +
                ", \'${location.provider}\'" +
                ")"

        val timelineDatabase = timelineDBHelper.writableDatabase
        DB_INSTANCE?.let { db ->
            timelineDatabase.execSQL("BEGIN TRANSACTION")
            timelineDatabase.execSQL(insertStatement)
            timelineDatabase.execSQL("COMMIT TRANSACTION")
            timelineDatabase.close()
        } ?: Log.d("Database", "Instance가 null(insert)")
    }


    fun updateTimeStamp(timeTarget: Long, airInfo: AirInfoType)
            = GlobalScope.launch{
        airInfo?.let { newAirInfo ->
            val updateStatement = "UPDATE $DB_TABLE_NAME " +
                    "SET $DB_ATTR_AIRJSON = \'$newAirInfo\' " +
                    "WHERE $DB_ATTR_TIME = $timeTarget"

            val timelineDatabase = timelineDBHelper.writableDatabase
            DB_INSTANCE?.let{ db ->
                timelineDatabase.execSQL("BEGIN TRANSACTION")
                timelineDatabase.execSQL(updateStatement)
                timelineDatabase.execSQL("COMMIT TRANSACTION")
                timelineDatabase.close()
            } ?: Log.d("Database", "Instance가 null(update)")
        }
    }
}