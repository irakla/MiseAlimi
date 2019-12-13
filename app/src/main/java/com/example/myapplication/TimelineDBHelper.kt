package com.example.myapplication

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class TimelineDBHelper(context: Context)
    : SQLiteOpenHelper(context.applicationContext,
    DB_NAME, null,
    DB_VER
){
    companion object{
        const val DB_VER = 1
        const val DB_NAME = "timeline.db"
        const val DB_TABLE_NAME = "timeline"
        const val DB_ATTR_TIME = "time_mil"
        const val DB_ATTR_LATITUDE = "latitude"
        const val DB_ATTR_LONGITUDE = "longitude"
        const val DB_ATTR_AIRJSON = "airJSON"
        const val DB_ATTR_PROVIDER = "provider"

        const val CREATE_SQL_VER1 = "CREATE TABLE IF NOT EXISTS timeline (" +
                "time_mil BIGINT PRIMARY KEY," +
                "latitude DOUBLE," +
                "longitude DOUBLE," +
                "airJSON VARCHAR," +
                "provider VARCHAR)"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        val createSQL = CREATE_SQL_VER1
        db?.execSQL(createSQL)
    }
    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }
}