package com.example.citylink.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.citylink.R
import com.example.citylink.adapters.RetrofitInterface
import com.example.citylink.others.Constants
import com.example.citylink.others.TrackingUtility.hasPermissions
import com.example.citylink.ui.viewmodels.MapsServiceViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BusStopsNearMe : Fragment(R.layout.fragment_maps) {

    private lateinit var currentLocation: LatLng
    @Inject lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var map : GoogleMap
    private lateinit var request: LocationRequest
    private var busStopIcon : BitmapDescriptor? = null
    private var userIcon : BitmapDescriptor? = null
    private var vBreakdownIcon : BitmapDescriptor? = null
    private var hTrafficIcon : BitmapDescriptor? = null
    private var rMaintenanceIcon : BitmapDescriptor? = null
    private var userMarker :  Marker? = null
    @Inject lateinit var retrofitInterface: RetrofitInterface

    private val mapsServiceViewModel : MapsServiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MobileAds.initialize(requireContext()) {}
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        val mAdView = view.findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = object: AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Timber.tag("Admob").d("Ad clicked")
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                // Code to be executed when an ad request fails.
                Timber.tag("Admob").d("Ad failed ${adError}")
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Timber.tag("Admob").d("Ad loaded")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }

    }

    private val callback = OnMapReadyCallback { googleMap ->

        map = googleMap
        var scaler = BitmapFactory.decodeResource(resources,R.drawable.bus_stop_placeholder)
        var scaledBitmap = Bitmap.createScaledBitmap(scaler, 70, 70, false)
        busStopIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

        scaler = BitmapFactory.decodeResource(resources,R.drawable.user_placeholder)
        scaledBitmap = Bitmap.createScaledBitmap(scaler, 70, 70, false)
        userIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

        scaler = BitmapFactory.decodeResource(resources,R.drawable.car_repair_placeholder)
        scaledBitmap = Bitmap.createScaledBitmap(scaler, 70, 70, false)
        vBreakdownIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

        scaler = BitmapFactory.decodeResource(resources,R.drawable.traffic_lights_placeholder)
        scaledBitmap = Bitmap.createScaledBitmap(scaler, 70, 70, false)
        hTrafficIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

        scaler = BitmapFactory.decodeResource(resources,R.drawable.road_maintenance_placeholder)
        scaledBitmap = Bitmap.createScaledBitmap(scaler, 70, 70, false)
        rMaintenanceIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

        map.clear()
        map.uiSettings.isMyLocationButtonEnabled = true
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isRotateGesturesEnabled = true

        map.clear()

        setupRoutes()
        setupRoadBlocks()
        setupBusStops()
        setupUser()

    }



    private fun setupUser() {
        if(hasPermissions(requireContext())){
            requestUserLocationUpdate()
        }else{
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(19.0, 73.0), 9f))
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestUserLocationUpdate() {
        request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            Constants.LOCATION_UPDATE_INTERVAL
        )
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(Constants.FASTEST_LOCATION_INTERVAL)
            .build()
        fusedLocationProviderClient.requestLocationUpdates(
            request, locationCallBack, Looper.getMainLooper()
        )
    }

    private val locationCallBack = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.lastLocation?.let { location ->
                userMarker?.remove()
                currentLocation = LatLng(location.latitude, location.longitude)
                userMarker = map.addMarker(MarkerOptions()
                    .position(currentLocation)
                    .icon(userIcon)
                    .title("Here you are"))!!
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                userMarker!!.showInfoWindow()
            }
        }
    }

    private fun setupRoutes() {
        mapsServiceViewModel.getBusRoutesLD()
        mapsServiceViewModel.busRoutesLiveData.observe(this){ Routes->
            for(route in Routes){
                val stepList: MutableList<LatLng> = ArrayList()
                val options = PolylineOptions().apply {
                    width(10f)
                    color(Color.BLUE)
                    geodesic(true)
                    clickable(true)
                    visible(true)
                    pattern(listOf(Dash(30f)))
                }
                Timber.tag("Route").d("Title :%s Route :%s", route.title, route.route)
                val decodedList = decode(route.route)
                for (latLng in decodedList) {
                    stepList.add(
                        LatLng(
                            latLng.latitude,
                            latLng.longitude
                        )
                    )
                }
                options.addAll(stepList)
                map.addPolyline(options)
            }
        }
    }

    private fun setupBusStops() {
        mapsServiceViewModel.getBusStopsLD()
        mapsServiceViewModel.busStopsLiveData.observe(this) {
            for (busStop in it) {
                map.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            busStop.latitude,
                            busStop.longitude
                        )
                    ).title(busStop.title).icon(busStopIcon)
                )
            }
        }
    }

    private fun setupRoadBlocks() {
        mapsServiceViewModel.getRoadBlocksLD()
        mapsServiceViewModel.roadBlockLiveData.observe(this){
            for (roadBlocks in it) {
                var icon : BitmapDescriptor? = null
                when(roadBlocks.reason){
                    "Vehicle Breakdown" -> {
                        icon = vBreakdownIcon
                    }
                    "Heavy Traffic" -> {
                        icon = hTrafficIcon
                    }
                    "Road Maintenance" -> {
                        icon = rMaintenanceIcon
                    }
                }
                map.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            roadBlocks.latitude,
                            roadBlocks.longitude
                        )
                    ).title(roadBlocks.title).icon(icon)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
    }

    private fun decode(points: String): List<com.google.android.gms.maps.model.LatLng> {
        val len = points.length
        val path: MutableList<com.google.android.gms.maps.model.LatLng> = java.util.ArrayList(len / 2)
        var index = 0
        var lat = 0
        var lng = 0
        while (index < len) {
            var result = 1
            var shift = 0
            var b: Int
            do {
                b = points[index++].toInt() - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            result = 1
            shift = 0
            do {
                b = points[index++].toInt() - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            path.add(com.google.android.gms.maps.model.LatLng(lat * 1e-5, lng * 1e-5))
        }
        return path
    }

}