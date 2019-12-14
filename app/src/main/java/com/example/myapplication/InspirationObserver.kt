package com.example.myapplication

interface InspirationObserver {
    fun update(pm10: Double, pm25: Double, getInTime: Long, getOutTime: Long)
}