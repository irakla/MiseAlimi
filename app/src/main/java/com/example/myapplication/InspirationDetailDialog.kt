package com.example.myapplication

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.dialog_amount_detail.*

class InspirationDetailDialog(
    context: Context?,
    private val timeMilliGetOut: Long,
    private val timeMilliGetIn: Long,
    private val finedustInspiration: Double,
    private val finedust25Inspiration: Double
) : AlertDialog(context){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_amount_detail)

    }

    override fun onStart() {
        super.onStart()

        outTimeView.text = TimeSupporter.translateTimeToString(timeMilliGetOut)
        inTimeView.text = TimeSupporter.translateTimeToString(timeMilliGetIn)
        pm10View.text = String.format("%,.2fμg", finedustInspiration)
        pm25View.text = String.format("%,.2fμg", finedust25Inspiration)
    }
}