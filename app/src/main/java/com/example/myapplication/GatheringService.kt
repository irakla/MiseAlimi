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
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*


class GatheringService : Service(){
    private var stamperInBackground: GPSStamper? = null
    private val mHandler: Handler = Handler()
    private var mTimer: Timer? = null

    companion object{
        class BootReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if(Intent.ACTION_BOOT_COMPLETED.equals(intent.action)) {
                    var serviceIntent = Intent(context, GatheringService:: class.java)

                    if(Build.VERSION.SDK_INT >= 26)
                        context.startForegroundService(serviceIntent)
                    else
                        context.startService(serviceIntent)
                }
            }
        }

        var isRunning = false
            private set(stateIsRunning) { field = stateIsRunning }

        val nameUsingPreference = "StampPeriod"
    }

    override fun onCreate() {
        if (mTimer != null) {
            mTimer?.cancel()
        } else {
            // recreate new
            mTimer = Timer()
        }
        // schedule task


        stamperInBackground = GPSStamper(this)

        val preference = applicationContext.getSharedPreferences(nameUsingPreference, Context.MODE_PRIVATE)
        val prevTime_GetLocation = preference.getLong(GPSStamper.prevStampTimeKey, 0)
        val passedTimeFromLastLocation = System.currentTimeMillis() - prevTime_GetLocation
        val periodSetted_LocationRefresh = GPSStamper.min_PeriodLocationRefresh * 60000

        mTimer?.scheduleAtFixedRate(TimeDisplayTimerTask(),
            if(passedTimeFromLastLocation < periodSetted_LocationRefresh)
                periodSetted_LocationRefresh - passedTimeFromLastLocation
            else
                0
        , periodSetted_LocationRefresh
        )

        startInForeground()
    }

    inner class TimeDisplayTimerTask: TimerTask() {

        override fun run() {
            // run on another thread
            mHandler.post(object : Runnable {
                override fun run() {
                    // display toast
                    Toast.makeText(
                        applicationContext, "위치수집 : " + getDateTime(),
                        Toast.LENGTH_SHORT
                    ).show()

                    stamperInBackground?.start_GetLocation()
                }
            })
        }

        private fun getDateTime(): String {
            // get date time in custom format
            val sdf = SimpleDateFormat("[yyyy/MM/dd - HH:mm:ss]")
            return sdf.format(Date())
        }
    }

    private fun startInForeground(){
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val remoteViews = RemoteViews(packageName, R.layout.view_foreground_notification)

        var builder: NotificationCompat.Builder
        if(Build.VERSION.SDK_INT >= 26){
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
        builder.setBadgeIconType(R.drawable.verygood)
        builder.setContentTitle("MiseAlimi")
        builder.setContentText("${GPSStamper.min_PeriodLocationRefresh}분마다 위치정보를 수집합니다.")
        builder.setDefaults(Notification.DEFAULT_VIBRATE)
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        //builder.setContent(remoteViews)
        builder.setContentIntent(pendingIntent)


        startForeground(100, builder.build())
    }

    override fun onBind(intent: Intent?): IBinder? {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        val restartServiceTask = Intent(getApplicationContext(), GatheringService.javaClass);
        restartServiceTask.setPackage(packageName);
        val restartPendingIntent = PendingIntent.getService(applicationContext, 1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        val myAlarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        myAlarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartPendingIntent);
        super.onTaskRemoved(rootIntent)
    }
}