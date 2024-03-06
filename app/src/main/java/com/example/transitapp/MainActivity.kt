package com.example.transitapp

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.transitapp.databinding.ActivityMainBinding
import com.example.transitapp.ui.home.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val mapView = findViewById<MapView>(R.id.mapView)
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

        // get location from Start Activity Intent
        val intent = intent
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        // Navigate to HomeFragment with arguments
        val bundle = Bundle().apply {
            putDouble("latitude", latitude)
            putDouble("longitude", longitude)
        }

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_map, R.id.navigation_routes, R.id.navigation_alerts))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.navigate(R.id.navigation_map, bundle)
    }
}