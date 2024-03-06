package com.example.transitapp.ui.dashboard


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.transitapp.R
import com.example.transitapp.SharedViewModel
import com.example.transitapp.databinding.FragmentDashboardBinding
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import android.widget.Toast


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var storedRoutesText: String = ""
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val busRoutes = resources.getStringArray(R.array.bus_route_ids)
        val buttonAdd = root.findViewById<Button>(R.id.buttonAdd)


        val adapter: ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            busRoutes
        )

        val textView = root.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        textView.setAdapter(adapter)
        textView.threshold = 1

        buttonAdd.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {

                if (textView.text.toString().isNotEmpty()) {
                    val selectedRoute = textView.text.toString()

                    if(busRoutes.contains(selectedRoute)) {

                        context?.openFileOutput("routes.txt", Context.MODE_APPEND).use {
                            it?.write((selectedRoute + ",").toByteArray())
                        }

                        textView.text.clear()
                        loadAndDisplayRoutes()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Sorry, not a valid bus route",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Sorry, you need to insert a route",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })


        return root
    }

    private fun displayRoutes(routesText: String) {
        val busListView = view?.findViewById<LinearLayout>(R.id.busListView)
        busListView?.removeAllViews()

        val routes = routesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        for (route in routes) {
            val routeItemView = LinearLayout(requireContext())
            routeItemView.orientation = LinearLayout.HORIZONTAL

            routeItemView.id = View.generateViewId()

            val test = routeItemView.id

            routeItemView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            Log.i("view", "$test")

            val textView = TextView(requireContext())
            textView.text = route
            textView.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )

            val removeButton = Button(requireContext())
            removeButton.text = "Remove"

            removeButton.setOnClickListener {
                removeRoute(route)
                loadAndDisplayRoutes()
            }

            routeItemView.addView(textView)
            routeItemView.addView(removeButton)

            busListView?.addView(routeItemView)

        }
    }




    private fun removeRoute(route: String) {
        try {
            val file = File(requireContext().filesDir, "routes.txt")
            if (file.exists()) {
                val fileContent = file.readText()

                // Remove the route from the content
                val updatedContent = fileContent.replace("$route,", "")

                // Write the updated content back to the file
                requireContext().openFileOutput("routes.txt", Context.MODE_PRIVATE).use {
                    it.write(updatedContent.toByteArray())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("DashboardFragment", "Error removing route: $e")
        }
    }


    private fun loadAndDisplayRoutes() {
        try {
            val file = File(requireContext().filesDir, "routes.txt")
            if (file.exists()) {
                val fileInputStream: FileInputStream = requireContext().openFileInput("routes.txt")
                val inputStreamReader = InputStreamReader(fileInputStream)
                val bufferedReader = BufferedReader(inputStreamReader)

                var line: String?
                storedRoutesText = ""

                // Read the content of the file
                while (bufferedReader.readLine().also { line = it } != null) {
                    storedRoutesText += line
                }

                // Close the streams
                inputStreamReader.close()
                fileInputStream.close()

                // Display routes in the busListView
                displayRoutes(storedRoutesText)

                sharedViewModel.routesText = storedRoutesText

                // Update the visibility of the title based on routes
                val myRoutesTitle = view?.findViewById<TextView>(R.id.textView2)
                myRoutesTitle?.visibility = if (storedRoutesText.isNotEmpty()) View.VISIBLE else View.GONE

            } else {
                Log.d("DashboardFragment", "File does not exist.")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("DashboardFragment", "Error loading and displaying routes: $e")
        }
    }


    override fun onResume() {
        super.onResume()

        loadAndDisplayRoutes()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}