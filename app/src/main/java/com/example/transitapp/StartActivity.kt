package com.example.transitapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class StartActivity : AppCompatActivity() {

    var fusedLocationProviderClient: FusedLocationProviderClient? = null

    val REQUEST_CODE = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            }
        }
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission was granted
            fusedLocationProviderClient?.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
                ?.addOnSuccessListener { location ->
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val intent = Intent(this@StartActivity, MainActivity::class.java)
                    intent.putExtra("latitude", latitude)
                    intent.putExtra("longitude", longitude)
                    startActivity(intent)
                }
        } else {
            // permission denied
            askPermission()
        }
    }

    open fun askPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE
        )
    }
}