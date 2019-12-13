package com.example.myapplication

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import android.util.Log
import java.util.*


class GatheringService : Service(){
    private val mHandler: Handler = Handler()
    private var stamperInBackground: GPSStamper? = null

    companion object{
        class BootReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if(Intent.ACTION_BOOT_COMPLETED.equals(intent.action)) {
                    val serviceIntent = Intent(context, GatheringService:: class.java)

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        context.startForegroundService(serviceIntent)
                    else
                        context.startService(serviceIntent)
                }
            }
        }


        var min_PeriodLocationRefresh:Long = 20
        var isRunning = false
            private set(stateIsRunning) { field = stateIsRunning }
        private var gatheringTimer: Timer? = null
    }

    override fun onCreate() {
        if (gatheringTimer != null) {
            gatheringTimer?.cancel()
        } else {
            // recreate new
            gatheringTimer = Timer()
        }
        // schedule task

        stamperInBackground = GPSStamper(applicationContext)

        val preference = applicationContext.getSharedPreferences(GPSStamper.nameUsingPreference, Context.MODE_PRIVATE)
        val prevTimeGetLocation = preference.getLong(GPSStamper.prevStampTimeKey, 0)
        Log.i(this.javaClass.name + ".prevGetLocationTime", prevTimeGetLocation.toString())
        val passedTimeFromLastLocation = System.currentTimeMillis() - prevTimeGetLocation
        val periodSettedLocationRefresh = min_PeriodLocationRefresh * MINUTE_BY_MILLI_SEC

        gatheringTimer?.scheduleAtFixedRate(PeriodicLocationGatheringTask(),
            if(passedTimeFromLastLocation < periodSettedLocationRefresh)
                periodSettedLocationRefresh - passedTimeFromLastLocation
            else
                0
        , periodSettedLocationRefresh
        )

        startInForeground()
    }

    private inner class PeriodicLocationGatheringTask: TimerTask() {

        override fun run() {
            // run on another thread
            mHandler.post { gathering() }
        }
    }

    private fun gathering(){
        stamperInBackground?.startGetLocation()
    }

    private fun startInForeground(){
        val notificationIntent = Intent(this, LoginActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val builder: NotificationCompat.Builder
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel_id = "timeline_gathering_channel"
            val channel = NotificationChannel(channel_id, "Timeline Gathering Channel",
                NotificationManager.IMPORTANCE_DEFAULT)

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).
                createNotificationChannel(channel)

            builder = NotificationCompat.Builder(this, channel_id)
        }
        else
            builder = NotificationCompat.Builder(this)

        builder.setSmallIcon(R.drawable.verygood)
        builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.verygood))
        builder.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
        builder.setContentTitle("MiseAlimi")
        builder.setContentText("${min_PeriodLocationRefresh}분마다 위치정보를 수집합니다.")
        builder.setDefaults(Notification.DEFAULT_VIBRATE)
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        builder.setContentIntent(pendingIntent)


        startForeground(100, builder.build())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceTask = Intent(applicationContext, GatheringService::class.java)
        restartServiceTask.setPackage(packageName)
        val restartPendingIntent = PendingIntent.getService(applicationContext, 1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT)
        val myAlarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        myAlarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartPendingIntent)
        super.onTaskRemoved(rootIntent)
    }
}