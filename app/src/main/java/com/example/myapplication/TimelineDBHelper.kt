package com.example.myapplication

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

//TODO : DB에서 삭제하는 기능 구현
class TimelineDBHelper(context: Context)
    : SQLiteOpenHelper(context,
    DB_NAME, null,
    DB_VER
){
    companion object{
        const val DB_VER = 1
        const val DB_NAME = "timeline.db"
        const val CREATE_SQL_VER1 = "CREATE TABLE timeline (" +
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
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // task for upgrade by version up.
    }
}