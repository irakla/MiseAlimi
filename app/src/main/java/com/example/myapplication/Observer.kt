package com.example.myapplication

interface Observer {
    fun update(valueChanged: Any? = null)
}