package com.example.myapplication

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_mainview.*

class MainView : AppCompatActivity() {
    var fragments = ArrayList<Fragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainview)

        fragments.add(Amount_Info())
        fragments.add(Text_Info())
        fragments.add(Air_Info())

        val preference = getSharedPreferences("User", Context.MODE_PRIVATE)
        preference.edit().putString("name",intent.getStringExtra("name")).apply()
        preference.edit().putString("age",intent.getStringExtra("age")).apply()
        preference.edit().putString("weight", intent.getStringExtra("weight")).apply()
        Toast.makeText(this, preference.getString("name","seokwon"), Toast.LENGTH_SHORT).show()

        var bundleFromLogin = Bundle()
        bundleFromLogin.putString("name", preference.getString("name", ""))
        bundleFromLogin.putString("age", preference.getString("age", "0"))
        bundleFromLogin.putString("weight", preference.getString("weight", "0"))

        viewPager.adapter = pagerAdapter(supportFragmentManager, bundleFromLogin)
    }

    inner class pagerAdapter(fm: FragmentManager, val savedInstanceState: Bundle?) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return fragments.size
        }

        override fun getItem(position: Int): Fragment {
            val fragment = fragments[position]
            fragment.arguments = savedInstanceState

            return fragment
        }
    }
}