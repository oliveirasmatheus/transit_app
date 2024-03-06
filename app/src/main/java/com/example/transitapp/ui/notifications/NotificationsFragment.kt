package com.example.transitapp.ui.notifications

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.transitapp.R
import com.example.transitapp.databinding.FragmentNotificationsBinding
import com.google.transit.realtime.GtfsRealtime
import java.net.URL

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val alertsUrl = "https://gtfs.halifax.ca/realtime/Alert/Alerts.pb"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        fetchAndDisplayAlerts()

        return root
    }
    private fun fetchAndDisplayAlerts() {
        Thread {
            try {
                val url = URL(alertsUrl)
                val feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream())

                activity?.runOnUiThread {
                    displayAlerts(feed)
                }
            } catch (e: Exception) {
                Log.e("ERROR", "Error fetching alerts", e)
            }
        }.start()
    }

    private fun displayAlerts(feed: GtfsRealtime.FeedMessage) {
        val alertsContainer = binding.scrollView
        alertsContainer.removeAllViews()

        for (entity in feed.entityList) {
            if (entity.hasAlert()) {
                val alertMessage = entity.alert.headerText.translationList[0].text

                val cardView = CardView(requireContext())
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(16, 8, 16, 8)
                cardView.layoutParams = layoutParams

                // Set background drawable from resources
                val drawable: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.border_background)
                cardView.background = drawable

                val alertText = TextView(requireContext())
                alertText.text = alertMessage
                alertText.setTextColor(Color.BLACK)
                alertText.setPadding(16)

                cardView.addView(alertText)
                alertsContainer.addView(cardView)
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}