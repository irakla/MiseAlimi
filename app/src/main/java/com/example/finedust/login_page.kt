package com.example.finedust

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login_page.*

class login_page : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        imageButton2.setOnClickListener {
            val sign_up_page_move = Intent(this, sign_up_page::class.java)
            startActivity(sign_up_page_move)
        }
    }
}
