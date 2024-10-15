package com.example.practica_09_10_2024

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.practica_09_10_2024.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private var minimumDistance = 30
    private val PERMISSION_LOCATION = 999
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 1000
            smallestDisplacement = minimumDistance.toFloat()
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                Log.e("APP 06", "${locationResult.lastLocation?.latitude}, ${locationResult.lastLocation?.longitude}")
            }
        }

        findViewById<View>(R.id.fab_open_google_maps).setOnClickListener { openGoogleMaps() }
        findViewById<View>(R.id.fab_open_route_google_maps).setOnClickListener { openRouteInGoogleMaps() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_LOCATION) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION, ignoreCase = true) &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_LOCATION)
        }

        val defaultLocation = LatLng(20.666667, -103.333333)
        marker = mMap.addMarker(MarkerOptions().position(defaultLocation).title("Centro de Guadalajara").snippet("Hueles a tierra mojada"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
        mMap.setOnMapClickListener(this)
    }

    fun map(view: View) {
        when (view.id) {
            R.id.activity_maps_map -> mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.activity_maps_terrain -> mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.activity_maps_hybrid -> mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.activity_maps_polylines -> openRouteInGoogleMapsWithNavigation()
        }
    }

    private fun startLocationUpdates() {
        try {
            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {
        }
    }

    private fun stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onMapClick(latLng: LatLng) {
        marker?.remove()
        marker = mMap.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun openGoogleMaps() {
        val gmmIntentUri = Uri.parse("geo:0,0?q=Guadalajara")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun openRouteInGoogleMaps() {
        marker?.let {
            val destination = "${it.position.latitude},${it.position.longitude}"
            val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destination")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }

    private fun openRouteInGoogleMapsWithNavigation() {
        marker?.let {
            val destination = "${it.position.latitude},${it.position.longitude}"
            val gmmIntentUri = Uri.parse("google.navigation:q=$destination&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }
}