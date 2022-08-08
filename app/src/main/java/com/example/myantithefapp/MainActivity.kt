package com.example.myantithefapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myantithefapp.databinding.ActivityMainBinding

class MainActivity : Activity() {
    private var binding: ActivityMainBinding? = null
    private val TAG = "MyMain"

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(
            layoutInflater
        )
        val view = binding?.root
        setContentView(view)


        if (MyObject.myPocketServiceRunning) {
            val text = "Detecting"
            binding?.let { binding ->
                binding.statusTVMain.text = text
                binding.pocketSwitchMain.isChecked = true
            }
        }
        if (MyObject.myChargingServiceRunning) {
            val text = "Detecting"
            binding?.let { binding ->
                binding.statusTVMain.text = text
                binding.chargingSwitchMain.isChecked = true
            }
        }

        if (MyObject.myShakeServiceRunning) {
            val text = "Detecting"
            binding?.let { binding ->
                binding.statusTVMain.text = text
                binding.shakeSwitchMain.isChecked = true
            }
        }


        binding?.let { binding ->

            binding.shakeSwitchMain.setOnCheckedChangeListener { _, isChecked ->
                Log.wtf(TAG, "In Shake Switch")
                if (isChecked) {
                    Log.wtf(TAG, "Is Check")
                    val text = "Detecting"
                    binding.statusTVMain.text = text
                    if (!MyObject.myShakeDetected) {
                        MyShakeForegroundService.mycontext = this@MainActivity
                        val serviceIntent = Intent(this, MyShakeForegroundService::class.java)
                        applicationContext.startForegroundService(serviceIntent)
//                    Timer().schedule(timerTask {
//                        finish()
//                    }, 1000)

                        Log.wtf(TAG, "Service Started")
                    }
                } else {
                    Log.wtf(TAG, "Not Is Check")
                    val text = "Not Detecting"
                    binding.statusTVMain.text = text
                    val serviceIntent = Intent(this, MyShakeForegroundService::class.java)
                    stopService(serviceIntent)
                    MyObject.myRingtone?.stop()
                }
            }


            binding.chargingSwitchMain.setOnCheckedChangeListener { _, isChecked ->
                Log.wtf(TAG, "In Charging Switch")
                if (isChecked) {
                    Log.wtf(TAG, "Is Check")
                    val text = "Detecting"
                    binding.statusTVMain.text = text
                    if (!MyObject.myChargingRemovalDetected) {
                        MyChargingForegroundService.mycontext = this@MainActivity
                        val serviceIntent = Intent(this, MyChargingForegroundService::class.java)
                        applicationContext.startForegroundService(serviceIntent)
//                    Timer().schedule(timerTask {
//                        finish()
//                    }, 1000)

                        Log.wtf(TAG, "Service Started")
                    }
                } else {
                    Log.wtf(TAG, "Not Is Check")
                    val text = "Not Detecting"
                    binding.statusTVMain.text = text
                    val serviceIntent = Intent(this, MyChargingForegroundService::class.java)
                    stopService(serviceIntent)
                    MyObject.myRingtone?.stop()
                }
            }

            binding.pocketSwitchMain.setOnCheckedChangeListener { _, isChecked ->
                Log.wtf(TAG, "In Switch")
                if (isChecked) {
                    Log.wtf(TAG, "Is Check")
                    if (!MyObject.myPocketDetected) {
                        val text = "Detecting"
                        binding.statusTVMain.text = text

                        MyPocketForegroundService.mycontext = this@MainActivity
                        val serviceIntent = Intent(this, MyPocketForegroundService::class.java)
                        applicationContext.startForegroundService(serviceIntent)
//                        Timer().schedule(timerTask {
//                            finish()
//                        }, 1000)

                        Log.wtf(TAG, "Service Started")
                    }
                } else {
                    Log.wtf(TAG, "Not Is Check")
                    MyObject.myRingtone?.stop()
                    val text = "Not Detecting"
                    binding.statusTVMain.text = text
                    val serviceIntent = Intent(this, MyPocketForegroundService::class.java)
                    stopService(serviceIntent)
                }
            }


            if (MyObject.myPocketDetected) {
                val text = "Pocket Removal Detect"
                binding.statusTVMain.text = text

                binding.StopRingingBtnMain.visibility = View.VISIBLE
                binding.StopRingingBtnMain.setOnClickListener {
                    MyObject.myRingtone?.stop()
                    MyObject.myPocketDetected = false
                    binding.StopRingingBtnMain.visibility = View.GONE
                    val text = "Not Detecting"
                    binding.statusTVMain.text = text
                    binding.pocketSwitchMain.isChecked = false
                    val serviceIntent = Intent(this, MyPocketForegroundService::class.java)
                    stopService(serviceIntent)
                }
            }

            if (MyObject.myChargingRemovalDetected) {
                val text = "Charging Removal Detect"
                binding.statusTVMain.text = text

                binding.StopRingingBtnMain.visibility = View.VISIBLE
                binding.StopRingingBtnMain.setOnClickListener {
                    MyObject.myRingtone?.stop()
                    MyObject.myChargingRemovalDetected = false
                    binding.StopRingingBtnMain.visibility = View.GONE
                    val text = "Not Detecting"
                    binding.statusTVMain.text = text
                    binding.chargingSwitchMain.isChecked = false
                    val serviceIntent = Intent(this, MyChargingForegroundService::class.java)
                    stopService(serviceIntent)
                }
            }

            if (MyObject.myShakeDetected) {
                val text = "Shake Detect"
                binding.statusTVMain.text = text

                binding.StopRingingBtnMain.visibility = View.VISIBLE
                binding.StopRingingBtnMain.setOnClickListener {
                    MyObject.myRingtone?.stop()
                    MyObject.myShakeDetected = false
                    binding.StopRingingBtnMain.visibility = View.GONE
                    val text = "Not Detecting"
                    binding.statusTVMain.text = text
                    binding.shakeSwitchMain.isChecked = false
                    val serviceIntent = Intent(this, MyShakeForegroundService::class.java)
                    stopService(serviceIntent)
                }
            }
        }
        Log.wtf("60 ", "Bottom")
    }
}

