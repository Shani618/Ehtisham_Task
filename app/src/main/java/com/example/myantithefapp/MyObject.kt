package com.example.myantithefapp

import android.content.Context
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.provider.Settings
import android.util.Log

object MyObject {
    private val TAG = "MyObject"
    var sensorListenerPocket: MyPocketForegroundService.SensorListenerPocket? = null


    var myRingtone: MediaPlayer? = null

    var myPocketDetected = false
    var myPocketServiceRunning = false

    var myChargingRemovalDetected = false
    var myChargingServiceRunning = false

    var myShakeDetected = false
    var myShakeServiceRunning = false

    fun Context.onCreateObject() {
        myRingtone = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
    }

    fun Context.startPocketDetection(context: Context) {
        Log.wtf(TAG, "Detection Started")
        myPocketDetected = false
        myPocketServiceRunning  = true
        sensorListenerPocket = MyPocketForegroundService.SensorListenerPocket(this);
        Log.wtf(TAG, "Detection Bottom")
    }

    fun Context.startChargingDetection(context: Context) {
        Log.wtf(TAG, "Charging Detection Started")
        myChargingRemovalDetected = false
        myChargingServiceRunning = true
        Log.wtf(TAG, "Detection Bottom")
    }

    fun Context.startShakeDetection(context: Context) {
        Log.wtf(TAG, "Charging Detection Started")
        myShakeDetected = false
        myShakeServiceRunning = true
        Log.wtf(TAG, "Detection Bottom")
    }

    fun stopPocketDetection() {
        sensorListenerPocket = null
        myPocketServiceRunning = false
        Log.wtf(TAG, "Stopping")
    }

    fun stopChargingDetection() {
        myChargingServiceRunning = false
        Log.wtf(TAG, "Stopping")
    }

    fun stopShakeDetection() {
        myShakeServiceRunning = false
        Log.wtf(TAG, "Stopping")
    }
}