package com.example.myapplication

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.view.textservice.TextInfo
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_mainview.*

class MainView : AppCompatActivity() {
    var fragments = ArrayList<Fragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainview)

        val view_AmountInfo = Amount_Info()
        val view_TextInfo = Text_Info()
        fragments.add(view_AmountInfo)
        fragments.add(view_TextInfo)
        view_AmountInfo.addInspirationObserver(view_TextInfo)
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

    override fun onResume(){
        super.onResume()

        setNowAirState()
    }

    fun setNowAirState(){
        if(GPSTimelineManager.gpsTimeline.size > 0) {
            var pm10data = GPSTimelineManager.gpsTimeline[0].airInfo?.getString("pm10Value")
            var pm25data = GPSTimelineManager.gpsTimeline[0].airInfo?.getString("pm25Value")

            //key값의 키워드 정보가 잘 제공되어 쓸 수 있는지 확인할 수 있는 HashMap
            var isPossibleInfos: HashMap<String, Boolean> = HashMap<String, Boolean>()
            isPossibleInfos.set("pm10Value", pm10data != "-")
            isPossibleInfos.set("pm25Value", pm25data != "-")

            if(isPossibleInfos.getValue("pm10Value")){
                var ppm10data: Int = pm10data?.toInt() ?: 0

                when(ppm10data) {
                    in 1..30 -> textView6.setText("좋음")
                    in 31..80 -> textView6.setText("보통")
                    in 81..150 -> textView6.setText("나쁨")
                    else -> textView6.setText("매우나쁨")
                }
                when(ppm10data) {
                    in 1..30 -> textView6.setTextColor(Color.parseColor("#51A0F8"))
                    in 31..80 -> textView6.setTextColor((Color.parseColor("#52B24A")))
                    in 81..150 -> textView6.setTextColor((Color.parseColor("#DC915D")))
                    else -> textView6.setTextColor((Color.parseColor("#ED655F")))
                }

                changeview.setImageResource(
                    when (ppm10data) {
                        in 1..30 -> R.drawable.verygood
                        in 31..80 -> R.drawable.normal
                        in 81..150 -> R.drawable.bad
                        else -> R.drawable.verybad
                    }
                )
            }
            else{
                //pm10Value 값을 정상적으로 받지 못했을 때
            }


            if(isPossibleInfos.getValue("pm25Value")){
                var ppm25data: Int = pm25data?.toInt() ?: 0

                when(ppm25data) {
                    in 1..15 -> textView4.setText("좋음")
                    in 16..35 -> textView4.setText("보통")
                    in 36..75 -> textView4.setText("나쁨")
                    else -> textView4.setText("매우나쁨")
                }

                when(ppm25data) {
                    in 1..15 -> textView4.setTextColor(Color.parseColor("#51A0F8"))
                    in 16..35 -> textView4.setTextColor((Color.parseColor("#52B24A")))
                    in 36..75 -> textView4.setTextColor((Color.parseColor("#DC915D")))
                    else -> textView4.setTextColor((Color.parseColor("#ED655F")))
                }

                changeview2.setImageResource(
                    when (ppm25data) {
                        in 1..15 -> R.drawable.verygood
                        in 16..35 -> R.drawable.normal
                        in 36..75 -> R.drawable.bad
                        else -> R.drawable.verybad
                    }
                )
            }
            else{
                //pm10Value 값을 정상적으로 받지 못했을 때
            }
        }
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
