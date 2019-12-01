package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.R
import kotlinx.android.synthetic.main.fragment_amount_info.*
import kotlinx.android.synthetic.main.fragment_text_info.*

class Text_Info : Fragment(), Observer {
    private var finedustInspiration: Double = 0.0
    private var isShowing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_text_info, container, false)
    }

    override fun onResume() {
        super.onResume()
        isShowing = true
        setContent()
    }

    override fun onPause(){
        super.onPause()
        isShowing = false
    }

    private fun setContent(){

        // 담배
        var ciga = 0.0
        ciga = finedustInspiration / 12000

        textView10.setText(
            String.format(
                "미세먼지를 %,.2fμg만큼 마시면 담배 %,.2f개비를 피운것과 같습니다.",
                finedustInspiration,
                ciga
            )
        )

        var kf94: Double = 0.0
        var kf80: Double = 0.0
        var realkf94: Double = 0.0
        var realkf80: Double = 0.0

        kf94 = finedustInspiration * 0.99
        kf80 = finedustInspiration * 0.8
        realkf94 = finedustInspiration - kf94
        realkf80 = finedustInspiration - kf80

        textView7.text = String.format("kf94 마스크 착용시 : %,.2fug", kf94)
        textView12.text = String.format("실제 흡입량 : %,.2fug", realkf94)
        textView9.text = String.format("kf80 마스크 착용시 : %,.2fug", kf80)
        textView13.text = String.format("실제 흡입량 : %,.2fug", realkf80)
    }

    override fun update(valueChanged: Any?) {
        if(valueChanged is Double)
            finedustInspiration = valueChanged
        else
            return

        if(isShowing)
            setContent()
    }
}