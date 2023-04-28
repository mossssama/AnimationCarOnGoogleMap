package com.mindorks.example.ubercaranimation

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mindorks.example.ubercaranimation.Util.UI.CameraUtils.Companion.showDefaultLocationOnMap
import com.mindorks.example.ubercaranimation.Util.Logic.MapUtils
import com.mindorks.example.ubercaranimation.Util.Logic.MapUtils.showMovingCab
import com.mindorks.example.ubercaranimation.Util.Logic.PathUtils.Companion.showPath

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    /* M Osama: variables for maps & car */
    private lateinit var googleMap: GoogleMap
    private lateinit var defaultLocation: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* M Osama: buildingMap for tracking user's location(Car) */
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /* M Osama: called once; only when the map is loaded on the screen */
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        defaultLocation = LatLng(28.435350000000003, 77.11368)
        showDefaultLocationOnMap(googleMap,defaultLocation)

        Handler().postDelayed(Runnable {
            showPath(googleMap, MapUtils.getListOfLocations())
            showMovingCab(googleMap, applicationContext, MapUtils.getListOfLocations())
        }, 3000)
    }


}
