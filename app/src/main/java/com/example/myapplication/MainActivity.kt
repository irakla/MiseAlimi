package com.example.myapplication

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.webkit.WebChromeClient
import android.widget.RemoteViews

const val PERMISSIONCODE_Essential: Int = 1000
val permissionForEssential: Array<out String> = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.INTERNET,
    Manifest.permission.RECEIVE_BOOT_COMPLETED
)
    get() = field.clone()



class MainActivity : AppCompatActivity() {
    lateinit var myWebView: WebView
    @RequiresApi(Build.VERSION_CODES.M)
    val timeline = GPSTimelineManager.gpsTimeline
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        myWebView = findViewById(R.id.webview)
        myWebView.webChromeClient = WebChromeClient()
        myWebView.webViewClient = WebViewClient()
        myWebView.settings.javaScriptEnabled = true
        myWebView.addJavascriptInterface(WebAppInterface(this), "Android")
        myWebView.loadUrl("http://115.86.172.10:3000/")

        //권한 요청
        if(PermissionManager.isExist_deniedPermission(this, permissionForEssential)) {
            PermissionManager.showRequest(this,
                PermissionManager.deniedPermListOf(this, permissionForEssential), PERMISSIONCODE_Essential,
                "일중 이동경로 기반 호흡량 계산", "위치정보 수집")
        }

        GPSTimelineManager.initializeTimeline(this)

        //if service is dead
        if(!GatheringService.isRunning)
            startService(Intent(this, GatheringService::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.action_settings) {
            val sign_up_page_move = Intent(this, Preferences::class.java)
            startActivity(sign_up_page_move)
        }
        return super.onOptionsItemSelected(item)
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
        val intent = Intent(mContext, MainView::class.java)
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