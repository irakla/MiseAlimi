package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.DownloaderForStatistics
import com.example.myapplication.R
import com.example.myapplication.StatsMap
import kotlinx.android.synthetic.main.fragment_statistic.*
import org.jetbrains.anko.runOnUiThread

class StatisticFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val nowView = inflater.inflate(R.layout.fragment_statistic, container, false)

        context?.let { context ->
            DownloaderForStatistics(context) { statsMap ->
                context.runOnUiThread {
                    setFineDustValue(statsMap)
                }
            }.execute()

        }
        return nowView
    }

    private fun setFineDustValue(statsMap: StatsMap) {
        textValueMyGreatestPM10.text = String.format("미세먼지 : %.3f㎍/m³", statsMap[DownloaderForStatistics.STATS_KEY_PRIVATE_GREATEST_PM10])
        textValueMyGreatestPM25.text = String.format("초미세먼지 : %.3f㎍/m³", statsMap[DownloaderForStatistics.STATS_KEY_PRIVATE_GREATEST_PM2_5])
        textValueMyLeastPM10.text = String.format("미세먼지 : %.3f㎍/m³", statsMap[DownloaderForStatistics.STATS_KEY_PRIVATE_LEAST_PM10])
        textValueMyLeastPM25.text = String.format("초미세먼지 : %.3f㎍/m³", statsMap[DownloaderForStatistics.STATS_KEY_PRIVATE_LEAST_PM2_5])
        textValueUserAveragePM10.text = String.format("미세먼지 : %.3f㎍/m³", statsMap[DownloaderForStatistics.STATS_KEY_AVERAGE_PM10])
        textValueUserAveragePM25.text = String.format("초미세먼지 : %.3f㎍/m³", statsMap[DownloaderForStatistics.STATS_KEY_AVERAGE_PM2_5])
        textValueMaxInspirationPM10.text = String.format("미세먼지 : %.3f㎍/m³", statsMap[DownloaderForStatistics.STATS_KEY_GREATEST_PM10])
        textValueMaxInspirationPM25.text = String.format("초미세먼지 : %.3f㎍/m³", statsMap[DownloaderForStatistics.STATS_KEY_GREATEST_PM2_5])
    }

    override fun onResume() {
        super.onResume()
    }
}