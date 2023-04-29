package com.mindorks.example.ubercaranimation

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.trackuseryarabkotlin.HelperFunctions.Tracker.Companion.REQUEST_LOCATION_PERMISSION
import com.example.trackuseryarabkotlin.HelperFunctions.Tracker.Companion.checkPermissions
import com.example.trackuseryarabkotlin.HelperFunctions.Tracker.Companion.requestLocationUpdates
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mindorks.example.ubercaranimation.Util.UI.CameraUtils.Companion.showDefaultLocationOnMap
import com.mindorks.example.ubercaranimation.Util.Logic.MapUtils
import com.mindorks.example.ubercaranimation.Util.Logic.PathUtils.Companion.showExpectedPath

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        lateinit var googleMap: GoogleMap
        var listOfActualPathNodes = ArrayList<LatLng>()
    }

    private lateinit var defaultLocation: LatLng                            /* M Osama: default location to move camera to */
    private lateinit var fusedLocationClient: FusedLocationProviderClient   // M Osama: used to return user location
    private lateinit var handler: Handler                                   // M Osama: handler that runs a timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        listOfActualPathNodes.add(LatLng(30.02735999999999,31.2559448)) /* M Osama: will be substituted with location taken from our API */

        /* M Osama: buildingMap for tracking user's location(Car) */
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /* M Osama: tracking user's latLng */
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        handler = Handler()
        checkPermissions(fusedLocationClient, applicationContext, handler, this)

    }

    override fun onResume() {
        super.onResume()
        checkPermissions(fusedLocationClient, applicationContext, handler, this)
    }

    /* M Osama: called once; only when the map is loaded on the screen */
    override fun onMapReady(googleMap: GoogleMap) {
        MainActivity.googleMap = googleMap
        defaultLocation = LatLng(30.0444, 31.2357)
        showDefaultLocationOnMap(googleMap,defaultLocation)

        Handler().postDelayed({
            showExpectedPath(googleMap, MapUtils.getListOfLocations(), Color.BLACK)
//            showMovingCab(googleMap, applicationContext, listOfActualPathNodes)
        }, 3000)
    }


    /* If permissions is refused; fire a toast
     If permissions is granted; start requesting location updates */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { requestLocationUpdates(fusedLocationClient, applicationContext, handler) }
            else { Toast.makeText(this, "No permission accepted till now", Toast.LENGTH_SHORT).show() }
        }
    }


}
