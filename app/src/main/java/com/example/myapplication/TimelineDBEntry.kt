package com.example.myapplication

import android.content.ContentValues
import android.content.Context
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
import org.json.JSONObject

class TimelineDBEntry(private val context: Context) {
    companion object{
        private var DB_CONNECTION_INSTANCE: TimelineDBHelper? = null
    }

    init {
        synchronized(TimelineDBEntry::class) {
            DB_CONNECTION_INSTANCE?: let {
                DB_CONNECTION_INSTANCE = TimelineDBHelper(context.applicationContext)
            }
        }
    }

    fun loadLatestTimelineInPeriod(
        thePeriod: Long
        , doingWithTheTimeline: (List<Pair<Location, AirInfoType>>) -> Unit
    ) {
        var timelineDatabase: SQLiteDatabase?
        synchronized(TimelineDBEntry::class){
            timelineDatabase = DB_CONNECTION_INSTANCE?.readableDatabase
        }
        val timeFrom = System.currentTimeMillis() - thePeriod

        val resultTimeline = mutableListOf<Pair<Location, AirInfoType>>()

        GlobalScope.launch {
            timelineDatabase?.let { db ->
                val cursor = db.rawQuery(
                    "SELECT * FROM $DB_TABLE_NAME "
                    + "WHERE ${TimelineDBHelper.DB_ATTR_TIME} > $timeFrom"
                    , null
                )
                Log.d("database", "data count : ${cursor.count}")

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

                db.close()
                doingWithTheTimeline(resultTimeline)
            }
        }
    }

    fun insertTimeStamp(location: Location, airInfo: AirInfoType = null)
            = GlobalScope.launch {
        val insertStatement = "INSERT INTO $DB_TABLE_NAME" +
                "($DB_ATTR_TIME, $DB_ATTR_LATITUDE, $DB_ATTR_LONGITUDE, $DB_ATTR_AIRJSON, $DB_ATTR_PROVIDER) " +
                "VALUES(" +
                "${location.time}" +
                ", ${location.latitude}" +
                ", ${location.longitude}" +
                ", \'${airInfo?.toString()}\'" +
                ", \'${location.provider}\'" +
                ")"

        var timelineDatabase: SQLiteDatabase?
        synchronized(TimelineDBEntry::class){
            timelineDatabase = DB_CONNECTION_INSTANCE?.writableDatabase
        }
        timelineDatabase?.let { db ->
            val timestamp = ContentValues()
            timestamp.put(DB_ATTR_TIME, location.time)
            timestamp.put(DB_ATTR_LATITUDE, location.latitude)
            timestamp.put(DB_ATTR_LONGITUDE, location.longitude)
            if(airInfo == null) timestamp.put(DB_ATTR_AIRJSON, "null")
            else timestamp.put(DB_ATTR_AIRJSON, airInfo.toString())
            timestamp.put(DB_ATTR_PROVIDER, location.provider)

            db.execSQL("BEGIN TRANSACTION")
            //db.execSQL(insertStatement)
            db.insertOrThrow(DB_TABLE_NAME, null, timestamp)
            db.execSQL("COMMIT TRANSACTION")
            db.close()
        } ?: Log.d("Database", "Instance가 null(insert)")
    }


    fun updateTimeStamp(timeTarget: Long, airInfo: AirInfoType)
            = GlobalScope.launch{
        airInfo?.let { newAirInfo ->
            val updateStatement = "UPDATE $DB_TABLE_NAME " +
                    "SET $DB_ATTR_AIRJSON = \'$newAirInfo\' " +
                    "WHERE $DB_ATTR_TIME = $timeTarget"

            var timelineDatabase: SQLiteDatabase?
            synchronized(TimelineDBEntry::class){
                timelineDatabase = DB_CONNECTION_INSTANCE?.writableDatabase
            }
            timelineDatabase?.let{ db ->
                val targetAirJSON = ContentValues()
                targetAirJSON.put(DB_ATTR_AIRJSON, airInfo.toString())

                db.execSQL("BEGIN TRANSACTION")
                //db.execSQL(updateStatement)
                db.update(DB_TABLE_NAME, targetAirJSON
                    , "$DB_ATTR_TIME = ?", arrayOf(timeTarget.toString()))
                db.execSQL("COMMIT TRANSACTION")
                db.close()
            } ?: Log.d("Database", "Instance가 null(update)")
        }
    }
}