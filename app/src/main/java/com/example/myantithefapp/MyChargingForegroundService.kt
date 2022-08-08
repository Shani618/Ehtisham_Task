package com.example.myantithefapp

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.myantithefapp.MyObject.onCreateObject
import com.example.myantithefapp.MyObject.startChargingDetection
import com.example.myantithefapp.MyObject.stopChargingDetection
import com.example.myantithefapp.MyObject.stopPocketDetection


class MyChargingForegroundService() : Service() {

    private val TAG = "Charging Service"
    var mPowerButtonReceiver: BroadcastReceiver? = null
    var mChargingReceiver: BroadcastReceiver? = null

    companion object {

        @SuppressLint("StaticFieldLeak")
        var mycontext: Context? = null
    }

    override fun onCreate() {
        Log.e(TAG, "Service")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            startMyOwnForeground()
        } else {
            startForeground(1, Notification())
        }
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        onCreateObject()
        startChargingDetection(this)

        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)

        mPowerButtonReceiver = ScreenReceiver()
        registerReceiver(mPowerButtonReceiver, filter)

        val chargingFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

        mChargingReceiver = PowerConnectionReceiver()
        registerReceiver(mChargingReceiver, chargingFilter)

        Log.wtf("In Service", "After")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.wtf("In Service", "onDestroy")
        stopChargingDetection()

        if(mPowerButtonReceiver != null) {
            unregisterReceiver(mPowerButtonReceiver)
        }
        if(mChargingReceiver != null) {
            unregisterReceiver(mChargingReceiver)
        }


    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "channel_charging"
        val channelName = "Charging ForeBackground Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.GREEN
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

        // Create an Intent for the activity you want to start
        val resultIntent = Intent(this, MainActivity::class.java)
// Create the TaskStackBuilder
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0,
                PendingIntent.FLAG_IMMUTABLE)
        }


        val notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(102, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(102, notification)
        }
    }

    class PowerConnectionReceiver : BroadcastReceiver() {
        private var isPerviouslyConnected = false
        override fun onReceive(context: Context, intent: Intent) {
            Log.wtf("In Reciver", "start")
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            if (isCharging) {
                isPerviouslyConnected = true
            }
            if (!isCharging && isPerviouslyConnected) {
                Log.wtf("In Recier", "IF")
                MyObject.myChargingRemovalDetected = true
                MyObject.myRingtone?.start()
            }
        }
    }


    class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e("LOB", "onReceive")
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                wasScreenOn = false
                Log.e("LOB", "wasScreenOn" + wasScreenOn)
            } else if (intent.action == Intent.ACTION_SCREEN_ON) {
                wasScreenOn = true
                if (MyObject.myChargingRemovalDetected) {
                    MyObject.myChargingRemovalDetected = false
                    MyObject.myRingtone?.stop()
//                    val serviceIntent = Intent(mycontext, MyChargingForegroundService::class.java)
//                    mycontext!!.stopService(serviceIntent)
                }
            }
        }

        companion object {
            var wasScreenOn = true
        }
    }
}