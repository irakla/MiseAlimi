package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_mainview.*

class mainview : AppCompatActivity() {
    var viewList = ArrayList<Fragment>()
    //var viewList = ArrayList<View>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainview)
        //val air_infoView = layoutInflater.inflate(air_info::class.java, null)
        /*viewList.add(layoutInflater.inflate(R.layout.fragment_amount_info,null))
        viewList.add(layoutInflater.inflate(R.layout.fragment_text_info,null))
        viewList.add(air_infoView)*/

        viewList.add(amount_info())
        viewList.add(text_info())
        viewList.add(air_info())

        val preference = getSharedPreferences("User", Context.MODE_PRIVATE)
        preference.edit().putString("name",intent.getStringExtra("name")).apply()
        preference.edit().putString("age",intent.getStringExtra("age")).apply()
        preference.edit().putString("weight", intent.getStringExtra("weight")).apply()
        Toast.makeText(this, preference.getString("name","seokwon"), Toast.LENGTH_SHORT).show()
        viewPager.adapter = pagerAdapter(supportFragmentManager)
    }
    fun detail(view: View) {
        val intent = Intent(this, gps_list::class.java)
        startActivity(intent)
    }
    inner class pagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return viewList.size
        }

        override fun getItem(position: Int): Fragment {
            val fragment = viewList[position]

            return fragment
        }

        /*override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }*/

        /*override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var temp = viewList[position]
            viewPager.addView(temp)
            print("instantiate ex")
            return viewList[position]
        }*/

        /*override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            viewPager.removeView(obj as View)
        }

        override fun finishUpdate(container: ViewGroup) {
            //super.finishUpdate()

        }*/
    }
}
