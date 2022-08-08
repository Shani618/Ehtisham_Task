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
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.myantithefapp.MyObject.onCreateObject
import com.example.myantithefapp.MyObject.startPocketDetection
import com.example.myantithefapp.MyObject.stopPocketDetection
import java.math.BigDecimal


class MyPocketForegroundService() : Service() {

    private val TAG = "Service"
    var mReceiver: BroadcastReceiver? = null

    companion object {

        var pocket = 0

        fun onDetect(prox: Float, light: Float, g: FloatArray, inc: Int) {
            if (prox < 1 && light < 2 && g[1] < -0.6 && (inc > 75 || inc < 100)) {
                pocket = 1
            }
            if (prox >= 1 && light >= 2 && g[1] >= -0.7) {
                if (pocket == 1) {
                    MyObject.myPocketDetected = true
                    val mode = mycontext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    mode.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    MyObject.myRingtone?.start()
                    Log.wtf("Service", "Ringing")
                    pocket = 0
                }
            }
        }

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
        startPocketDetection(this)

        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        mReceiver = ScreenReceiver()
        registerReceiver(mReceiver, filter)

        Log.wtf("In Service", "After")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.wtf("In Service", "onDestroy")
        super.onDestroy()
        stopPocketDetection()

        if(mReceiver != null) {
            unregisterReceiver(mReceiver)
        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "channel_pocket"
        val channelName = "Pocket ForeBackground Service"
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
            startForeground(101, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(101, notification)
        }
    }

    class SensorListenerPocket(context: Context) : SensorEventListener {
        private val mySensorManager: SensorManager
        var proximitySensor: Sensor?
        var lightSensor: Sensor?
        var accSensor: Sensor?
        var rp = -1f
        var rl = -1f
        var g = floatArrayOf(0f, 0f, 0f)
        var inclination = -1

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                g = FloatArray(3)
                g = event.values.clone()
                val norm_Of_g = Math.sqrt((g[0] * g[0] + g[1] * g[1] + g[2] * g[2]).toDouble())
                g[0] = (g[0] / norm_Of_g).toFloat()
                g[1] = (g[1] / norm_Of_g).toFloat()
                g[2] = (g[2] / norm_Of_g).toFloat()
                inclination = Math.round(
                    Math.toDegrees(
                        Math.acos(
                            g[2].toDouble()
                        )
                    )
                ).toInt()
            }
            if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                rp = event.values[0]
            }
            if (event.sensor.type == Sensor.TYPE_LIGHT) {
                rl = event.values[0]
            }
            if (rp != -1f && rl != -1f && inclination != -1) {
                // Log.wtf("Line 154", "In Detector")
                onDetect(rp, rl, g, inclination)

            }
        }

        override fun onAccuracyChanged(sensor: Sensor, i: Int) {}

        init {
            mySensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            proximitySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
            lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)


            if (accSensor == null) {
            } else {
                mySensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
            if (proximitySensor != null) {
                mySensorManager.registerListener(
                    this,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
            if (lightSensor != null) {

                mySensorManager.registerListener(
                    this,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
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
                if (MyObject.myPocketDetected) {
                    MyObject.myPocketDetected = false
                    MyObject.myRingtone?.stop()
//                    val serviceIntent = Intent(mycontext, MyPocketForegroundService::class.java)
//                    mycontext!!.stopService(serviceIntent)
                }
            }
        }

        companion object {
            var wasScreenOn = true
        }
    }

}