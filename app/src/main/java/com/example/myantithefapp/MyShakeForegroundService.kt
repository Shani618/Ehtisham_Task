package com.example.myantithefapp

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.myantithefapp.MyObject.myRingtone
import com.example.myantithefapp.MyObject.myShakeDetected
import com.example.myantithefapp.MyObject.onCreateObject
import com.example.myantithefapp.MyObject.startChargingDetection
import com.example.myantithefapp.MyObject.startShakeDetection
import com.example.myantithefapp.MyObject.stopChargingDetection
import java.util.*
import kotlin.math.sqrt


class MyShakeForegroundService() : Service() {

    private val TAG = "Charging Service"
    var mPowerButtonReceiver: BroadcastReceiver? = null
    var sensorManagerShake: SensorManager? = null
    private var mAccelLast = 0f
    private var mAccelCurrent = 0f
    private var mAccel = 0f

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
        startShakeDetection(this)

        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)

        mPowerButtonReceiver = ScreenReceiver()
        registerReceiver(mPowerButtonReceiver, filter)

        sensorManagerShake = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        Objects.requireNonNull(sensorManagerShake)!!.registerListener(
            sensorListener, sensorManagerShake!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        mAccel = 10f
        mAccelCurrent = SensorManager.GRAVITY_EARTH
        mAccelLast = SensorManager.GRAVITY_EARTH


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
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "channel_shake"
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

        val mainIntent = Intent(this, MainActivity::class.java)

        val mainPendingIntent: PendingIntent =
            PendingIntent.getActivity(
                this, 12, mainIntent,
                PendingIntent.FLAG_IMMUTABLE
            )


        val notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(103, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(103, notification)
        }
    }


    private val sensorListener: SensorEventListener = object : SensorEventListener {


        override fun onSensorChanged(se: SensorEvent) {
            val x: Float = se.values[0]
            val y: Float = se.values[1]
            val z: Float = se.values[2]
            mAccelLast = mAccelCurrent
            mAccelCurrent = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = mAccelCurrent - mAccelLast
            mAccel = mAccel * 0.9f + delta // perform low-cut filter

            if (mAccel > 12) {
                myRingtone?.start()
                myShakeDetected = true
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e("LOB", "onReceive")
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                wasScreenOn = false
                Log.e("LOB", "wasScreenOn" + wasScreenOn)
            } else if (intent.action == Intent.ACTION_SCREEN_ON) {
                wasScreenOn = true
                if (MyObject.myShakeDetected) {
                    MyObject.myShakeDetected = false
                    MyObject.myRingtone?.stop()
//                    val serviceIntent = Intent(mycontext, MyShakeForegroundService::class.java)
//                    mycontext!!.stopService(serviceIntent)
                }
            }
        }

        companion object {
            var wasScreenOn = true
        }
    }
}