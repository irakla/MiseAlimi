package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.R
import android.content.Intent
import com.example.misealimi.GPSTimelineManager
import com.example.myapplication.gps_list
import kotlinx.android.synthetic.main.fragment_amount_info.*

class amount_info : Fragment() {
    var userName: String? = ""
    var userAge: Int? = 0
    var userWeight: Int? = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("amount_info 테스트")
        userName = arguments?.getString("name", userName)
        userAge = arguments?.getString("age", userAge.toString())?.toInt()
        userWeight =  arguments?.getString("weight", userWeight.toString())?.toInt()

        //inspiration.setText(userAge userWeight)

        return inflater.inflate(com.example.myapplication.R.layout.fragment_amount_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


    }
}
