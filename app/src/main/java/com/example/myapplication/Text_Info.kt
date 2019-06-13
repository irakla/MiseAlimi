package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.R
import kotlinx.android.synthetic.main.fragment_amount_info.*
import kotlinx.android.synthetic.main.fragment_text_info.*
var ciga: Double = 0.0
class Text_Info : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_text_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        if (finedustByInspirationNow != null) {

            ciga = finedustByInspirationNow / 12000

            textView10.setText(String.format("미세먼지를 %,.2fμg만큼 마시면 담배 %,.2f개비를 피운것과 같습니다.", finedustByInspirationNow, ciga))

        }
    }
}