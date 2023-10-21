package com.example.citylink.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.citylink.R
import com.example.citylink.others.Constants.ACTION_PAUSE_SERVICE
import com.example.citylink.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.citylink.others.Constants.MAP_ZOOM
import com.example.citylink.others.Constants.POLYLINE_COLOR
import com.example.citylink.others.Constants.POLYLINE_WIDTH
import com.example.citylink.services.TrackingService
import com.example.citylink.services.Poly_line
import com.example.citylink.ui.viewmodels.MapsServiceViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class trackingFragment : Fragment(R.layout.fragment_tracking) {
    private val mapsServiceViewModel : MapsServiceViewModel by viewModels()

    private var isTracking = false
    private var pathpoints_fragmnet = mutableListOf<Poly_line>()
    private var fragMap :GoogleMap?=null
    private var mapFragment : SupportMapFragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
        mapFragment?.getMapAsync(mapReadyCallback)
        subscribeToObservers()
    }

    @SuppressLint("MissingPermission")
    private val mapReadyCallback = OnMapReadyCallback { map ->
        fragMap = map
        map.uiSettings.isMyLocationButtonEnabled = true
        map.isMyLocationEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isRotateGesturesEnabled = true

        addAllPolylines()
    }

    private fun subscribeToObservers(){

//        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
//            updateTracking(it)
//        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathpoints_fragmnet = it
            addLatestPolyline()
            moveCameraToUser()
        })

    }
    private fun toggleRun(){
        if(isTracking){
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }
        else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun moveCameraToUser(){
        if(pathpoints_fragmnet.isNotEmpty() && pathpoints_fragmnet.last().isNotEmpty()){

            val lastLoc = pathpoints_fragmnet.last().last()
            fragMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    lastLoc,
                    MAP_ZOOM
                )
            )

        }
    }
        private fun addAllPolylines(){
            for(polyline in pathpoints_fragmnet){
                val PolylineOptions = PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)
                    .addAll(polyline)
                fragMap?.addPolyline(PolylineOptions)
            }
        }
        private fun addLatestPolyline(){
           if(pathpoints_fragmnet.isNotEmpty() && pathpoints_fragmnet.last().size>1){
               val prelastlatlng:LatLng =pathpoints_fragmnet.last()[pathpoints_fragmnet.last().size-2]
               val lastlatlng:LatLng=pathpoints_fragmnet.last().last()
               val polylineOptions = PolylineOptions()
                   .color(POLYLINE_COLOR)
                   .width(POLYLINE_WIDTH)
                   .add(prelastlatlng)
                   .add(lastlatlng)
               fragMap?.addPolyline(polylineOptions)

        }}
    private fun sendCommandToService(action:String)=
            Intent(requireContext(), TrackingService ::class.java).also{
                it.action=action
                requireContext().startService(it)
            }

    override fun onResume() {
        super.onResume()
        mapFragment?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapFragment?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragment?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapFragment?.onLowMemory()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapFragment?.onSaveInstanceState(outState)
    }

}







