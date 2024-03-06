package com.example.transitapp.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.transitapp.databinding.FragmentHomeBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.example.transitapp.MainActivity
import com.mapbox.maps.MapView
import com.example.transitapp.R
import com.example.transitapp.SharedViewModel
import com.example.transitapp.databinding.AnnotationViewBinding
import com.google.transit.realtime.GtfsRealtime
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import java.net.URL

var mapView: MapView? = null


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var viewAnnotationManager : ViewAnnotationManager
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private lateinit var myRoutes: String
    private lateinit var routeList: List<String>

    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval = 20 * 1000L

    private val refreshRunnable = object : Runnable {
        override fun run() {
            // Fetch GTFS data and update bus positions
            viewAnnotationManager.removeAllViewAnnotations()
            refreshBusPositions()
            // Schedule the next refresh
            handler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Access the MapView from the activity
        mapView = root.findViewById(R.id.mapView)

        val latitude = arguments?.getDouble("latitude") ?: 0
        val longitude = arguments?.getDouble("longitude") ?: 0

        viewAnnotationManager = binding.mapView.viewAnnotationManager

        myRoutes = sharedViewModel.routesText
        routeList = myRoutes.split(",")

        mapView.getMapboxMap().setCamera(
            com.mapbox.maps.CameraOptions.Builder()
                .center(Point.fromLngLat(longitude.toDouble(), latitude.toDouble()))
                .zoom(12.0)
                .build()
        )

        // Load the Mapbox style
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

        Thread {
            try {
                val url = URL("https://gtfs.halifax.ca/realtime/Vehicle/VehiclePositions.pb")
                val feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream())

                viewAnnotationManager = mapView.viewAnnotationManager

                for (entity in feed.entityList) {
                    if (entity.hasVehicle()) {
                        val vehicleInfo = entity.vehicle
                        val routeId = vehicleInfo.trip.routeId
                        val latitude = vehicleInfo.position.latitude.toDouble()
                        val longitude = vehicleInfo.position.longitude.toDouble()

                        val point = Point.fromLngLat(longitude, latitude)

                        addViewAnnotation(point, routeId)
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR", "Error fetching data", e)
            }
        }.start()

        handler.postDelayed(refreshRunnable, refreshInterval)

        return root
    }

    private fun refreshBusPositions() {
        Thread {
            try {
                val url = URL("https://gtfs.halifax.ca/realtime/Vehicle/VehiclePositions.pb")
                val feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream())

                viewAnnotationManager = mapView.viewAnnotationManager

                for (entity in feed.entityList) {
                    if (entity.hasVehicle()) {
                        val vehicleInfo = entity.vehicle
                        val routeId = vehicleInfo.trip.routeId
                        val latitude = vehicleInfo.position.latitude.toDouble()
                        val longitude = vehicleInfo.position.longitude.toDouble()

                        val point = Point.fromLngLat(longitude, latitude)

                        activity?.runOnUiThread {
                            addViewAnnotation(point, routeId)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR", "Error fetching data", e)
            }
        }.start()
    }

    private fun addViewAnnotation(point: Point, routeId: String) {
        val resId = if (routeList.contains(routeId)) {
            R.layout.highlight_view
        } else {
            R.layout.annotation_view
        }

        val viewAnnotation = viewAnnotationManager.addViewAnnotation(
            resId = resId,
            options = viewAnnotationOptions {
                geometry(point)
            }
        )

        val binding = AnnotationViewBinding.bind(viewAnnotation)
        binding.annotation.text = routeId
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

//    private fun addViewAnnotation(point: Point, routeId: String) {
//        // Define the view annotation
//        val viewAnnotation = viewAnnotationManager.addViewAnnotation(
//
//            resId = if (routeList.contains(routeId)) {
//                R.layout.highlight_view
//            } else {
//                R.layout.annotation_view
//            }
//            // Set any view annotation options
//            ,options = viewAnnotationOptions {
//                geometry(point)
//            }
//        )
//        val binding = AnnotationViewBinding.bind(viewAnnotation)
//        binding.annotation.text = routeId
//    }
//
//    override fun onDestroyView() {
//        handler.removeCallbacksAndMessages(null)
//        super.onDestroyView()
//        _binding = null
//    }
//
//    override fun onStart() {
//        super.onStart()
//        mapView?.onStart()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        mapView?.onStop()
//    }
//
//    override fun onLowMemory() {
//        super.onLowMemory()
//        mapView?.onLowMemory()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mapView?.onDestroy()
//    }

}