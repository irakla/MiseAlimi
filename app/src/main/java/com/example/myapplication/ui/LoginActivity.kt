package com.example.myapplication.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.util.Log
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.webkit.WebChromeClient
import com.example.myapplication.GPSTimelineManager
import com.example.myapplication.GatheringService
import com.example.myapplication.R
import kotlinx.android.synthetic.main.content_main.*

class LoginActivity : AppCompatActivity() {
    private lateinit var myWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        setLoginView()

        //if service is dead
        if(!GatheringService.isRunning)
            startService(Intent(this, GatheringService::class.java))
    }

    private fun setLoginView() {
        myWebView = webview
        myWebView.webChromeClient = WebChromeClient()
        myWebView.webViewClient = WebViewClient()
        myWebView.settings.javaScriptEnabled = true
        myWebView.addJavascriptInterface(
            WebAppInterface(
                this
            ), "Android")
        myWebView.loadUrl("http://115.86.172.10:3000/")

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack()) {
            myWebView.goBack()
            return true
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //////for log
        Log.i(this.javaClass.name + ".PermissionResult","requestCode : $requestCode")
        println()
        for(index: Int in 0..grantResults.size - 1)
            Log.i(this.javaClass.name + ".PermissionResult", permissions[index] + " : " + if(grantResults[index] == 0) "허가됨" else "불허")
    }
}
    private class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (view != null) {
                view.loadUrl(url)
            }
            return false
        }
}
class WebAppInterface(private val mContext: Context) {
    /** Show a toast from the web page  */
    @JavascriptInterface
    fun showToast(hey:String, hi:String, hello:String) {
        Toast.makeText(mContext, hey, Toast.LENGTH_SHORT).show()
        val intent = Intent(mContext, MainPageActivity::class.java)
        intent.putExtra("name", hey)
        intent.putExtra("age", hi)
        intent.putExtra("weight", hello)
        mContext.startActivity(intent)
    }

    @JavascriptInterface
    fun getUser(name:String, age:Int, weight:Int){
        Toast.makeText(mContext, name, Toast.LENGTH_SHORT).show()
    }
}