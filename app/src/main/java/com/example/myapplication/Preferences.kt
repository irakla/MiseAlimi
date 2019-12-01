package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

const val VERYGOODMAX_PM10 = 30
const val NORMALMAX_PM10 = 80
const val BADMAX_PM10 = 150

const val VERYGOODMAX_PM25 = 15
const val NORMALMAX_PM25 = 35
const val BADMAX_PM25 = 75
/*
* you can use this like:
* when(pm25value){
*   in 1..VERYGOODMAX_PM10 -> //좋음
*   in VERYGOODMAX_PM10 + 1 .. NORMALMAX_PM10 -> //보통
*   in NORMALMAX_PM10 + 1 .. BADMAX_PM10 -> //나쁨
*   else -> //매우나쁨
* }
*
 */

class Preferences : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preferences)
    }
}
