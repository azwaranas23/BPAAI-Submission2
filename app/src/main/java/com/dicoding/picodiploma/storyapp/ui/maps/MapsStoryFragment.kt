package com.dicoding.picodiploma.storyapp.ui.maps

import android.content.ContentValues
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.dicoding.picodiploma.storyapp.R
import com.dicoding.picodiploma.storyapp.databinding.FragmentMapsStoryBinding
import com.dicoding.picodiploma.storyapp.network.StoryItem
import com.dicoding.picodiploma.storyapp.preference.SessionPreferences
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsStoryFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapsStoryBinding
    private lateinit var viewModel: MapsStoryViewModel
    private lateinit var session: SessionPreferences
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        session = SessionPreferences(view.context)
        val token = session.getAuthToken() ?: ""
        viewModel = ViewModelProvider(this, MapsStoryViewModelFactory(token))[MapsStoryViewModel::class.java]

        viewModel.isLoading.observe(viewLifecycleOwner) {
            showLoading(it)
        }
        binding.btnRetry.setOnClickListener {
            viewModel.getStory(token)
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            when {
                error.isError && error.type == MapsStoryViewModel.ErrorType.NO_DATA ->
                    showError(true, getString(R.string.no_data_to_display))
                error.isError ->
                    error.errorMsg?.let { showError(true, it) }
                else -> showError(false)
            }
        }

        mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        viewModel.listStory.observe(viewLifecycleOwner) { stories ->

            val markers = ArrayList<Marker>()

            stories.forEach { story ->
                story.lat?.also { lat -> story.lon?.also { lon ->
                    val latlng = LatLng(lat, lon)
                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .position(latlng)
                            .title(getString(R.string.owner_story, story.name))
                            .snippet(getString(R.string.tap_to_open))
                    )
                    if (marker != null) {
                        marker.tag = story
                        markers.add(marker)
                    }
                    Log.d("MapsStoryFragment", story.toString())
                }}
            }


            val builder = LatLngBounds.Builder()
            for (marker in markers) {
                builder.include(marker.position)
            }
            val bounds = builder.build()

            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels
            val padding = (width * 0.20).toInt()
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)

            mMap.animateCamera(cu)
        }

        mMap.setOnPoiClickListener { pointOfInterest ->
            val poiMarker = mMap.addMarker(
                MarkerOptions()
                    .position(pointOfInterest.latLng)
                    .title(pointOfInterest.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
            )
            poiMarker?.showInfoWindow()
        }

        mMap.setOnInfoWindowClickListener {
            showStory(it.tag as StoryItem)
        }

        setMapStyle()

    }

    private fun setMapStyle() {
        try {
            view?.context?.also {
                val success =  mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(it,
                    R.raw.map_style
                ))
                if (!success) {
                    Log.e(ContentValues.TAG, "Style parsing failed.")
                }
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(ContentValues.TAG, "Can't find style. Error: ", exception)
        }
    }

    private fun showStory(story: StoryItem) {
        val toDetailStoryFragment =
            MapsStoryFragmentDirections.actionMapsStoryFragmentToDetailStoryFragment(
                story
            )
        view?.findNavController()?.navigate(toDetailStoryFragment)
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.pbMapStory.visibility = View.VISIBLE
            mapFragment.view?.visibility = View.GONE
        } else {
            binding.pbMapStory.visibility = View.GONE
            mapFragment.view?.visibility = View.VISIBLE
        }
    }

    private fun showError(isError: Boolean, msg: String = "") {
        if (isError) {
            binding.tvErrorMsg.text = msg
            mapFragment.view?.visibility = View.GONE
            binding.tvErrorMsg.visibility = View.VISIBLE
            binding.btnRetry.visibility = View.VISIBLE
        } else {
            mapFragment.view?.visibility = View.VISIBLE
            binding.tvErrorMsg.visibility = View.GONE
            binding.btnRetry.visibility = View.GONE
        }
    }

}