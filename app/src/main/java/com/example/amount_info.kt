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
    private var userName: String? = ""
    private var userAge: Int? = 0
    private var userWeight: Int? = 0
    private var userInspiRate: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("amount_info 테스트")
        userName = arguments?.getString("name", userName)
        userAge = arguments?.getString("age", userAge.toString())?.toInt()
        userWeight = arguments?.getString("weight", userWeight.toString())?.toInt()

        if(userInspiRate == null) {
            userInspiRate = when (userAge) {
                in 0 .. 2 -> 33
                in 3.. 5 -> 25
                in 6 .. 9 -> 22
                in 10 until 20 -> 20
                in 20 until 65 -> 14
                in 65 until 80 -> 18
                in 80 .. Int.MAX_VALUE ->  22
                else -> -1
            }
        }

        return inflater.inflate(com.example.myapplication.R.layout.fragment_amount_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(userWeight == null || userInspiRate == null)
            return

        inspirationView.setText((7 * userWeight as Int * userInspiRate as Int).toString() + "mL")
    }
}
