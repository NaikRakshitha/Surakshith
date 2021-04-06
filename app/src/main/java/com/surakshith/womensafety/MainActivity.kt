package com.surakshith.womensafety

import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.firebase.ui.auth.AuthUI
import com.surakshith.womensafety.databinding.ActivityMainBinding

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.net.Uri
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil.setContentView
import java.util.*
import java.util.jar.Manifest
import kotlin.math.sqrt


class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    private val REQUEST_PHONE_CALL = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("UNUSED_VARIABLE")


        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)



        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        Objects.requireNonNull(sensorManager)!!.registerListener(sensorListener, sensorManager!!
            .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        drawerLayout = binding.drawerLayout

        val navController = this.findNavController(R.id.myNavHostFragment)
        NavigationUI.setupActionBarWithNavController(this,navController,drawerLayout)

        NavigationUI.setupWithNavController(binding.navView, navController)

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.logout_user ->
                    AuthUI.getInstance().signOut(this)
                R.id.guardianInfo ->
                    navController.navigate(R.id.action_dashBoardFragment_to_guardianInfo)
                R.id.mapsActivity ->
                    navController.navigate(R.id.action_dashBoardFragment_to_mapsActivity)
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

    }

    override fun onSupportNavigateUp():Boolean{
        val navController = this.findNavController(R.id.myNavHostFragment)
        return NavigationUI.navigateUp(navController,drawerLayout)
    }


    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta
            if (acceleration > 12) {
            Toast.makeText(applicationContext,
                    getString(R.string.shake), Toast.LENGTH_LONG).show()

                startcall()
            }
        }


        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }
    private fun startcall(){
        val phone= "9902980323"

        if (ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.CALL_PHONE),REQUEST_PHONE_CALL)
        }
        else{
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:"+phone)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == REQUEST_PHONE_CALL)
            startcall()
    }
    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }
    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }
}
