package com.carto.androidtest.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.carto.androidtest.R
import com.carto.androidtest.databinding.FragmentMapBinding
import com.carto.androidtest.ui.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    private val binding: FragmentMapBinding
        get() = _binding!!
    private var _binding: FragmentMapBinding? = null

    private val callback = OnMapReadyCallback { googleMap ->
        val fakeCurrentLocation = LatLng(-33.3000802,149.0913524)
        val blueDotIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_bluedot)

        val currentLocationMarker = MarkerOptions()
            .position(fakeCurrentLocation)
            .icon(blueDotIcon)

        googleMap.addMarker(currentLocationMarker)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationMarker.position, 5f))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        initializeMap()

        return binding.root
    }

    private fun initializeMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}
