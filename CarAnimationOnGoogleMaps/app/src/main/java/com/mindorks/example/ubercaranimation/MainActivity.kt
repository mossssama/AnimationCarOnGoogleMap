package com.mindorks.example.ubercaranimation

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mindorks.example.ubercaranimation.util.AnimationUtils
import com.mindorks.example.ubercaranimation.util.MapUtils

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var defaultLocation: LatLng
    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var grayPolyline: Polyline? = null
    private var blackPolyline: Polyline? = null
    private var movingCabMarker: Marker? = null
    private var previousLatLng: LatLng? = null
    private var currentLatLng: LatLng? = null
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /* M Osama: moves googleMaps Camera to passed LatLng */
    private fun moveCamera(latLng: LatLng) { googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)) }

    /* M Osama: animate Camera transversing between location to another */
    private fun animateCamera(latLng: LatLng) {
        val cameraPosition = CameraPosition.Builder().target(latLng).zoom(15.5f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    /* M Osama: add Car on Map */
    private fun addCarMarkerAndGet(latLng: LatLng): Marker {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(this))
        return googleMap.addMarker(MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor))
    }

    /* M Osama: used to add the squared vertex at location & destination */
    private fun addOriginDestinationMarkerAndGet(latLng: LatLng): Marker {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getOriginDestinationMarkerBitmap())
        return googleMap.addMarker(MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor))
    }

    /* M Osama: movees the camera to the default location */
    private fun showDefaultLocationOnMap(latLng: LatLng) {
        moveCamera(latLng)
        animateCamera(latLng)
    }

    /* M Osama: function used to draw the path on Map */
    private fun showPath(latLngList: ArrayList<LatLng>) {
        val builder = LatLngBounds.Builder()
        for (latLng in latLngList) { builder.include(latLng) }

        /* M Osama: adjust camera bounds */
        val bounds = builder.build()
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 2))

        /* M Osama: draw the lineBackGround */
        val polylineOptions = PolylineOptions()
        polylineOptions.color(Color.GRAY)
        polylineOptions.width(5f)
        polylineOptions.addAll(latLngList)
        grayPolyline = googleMap.addPolyline(polylineOptions)

        /* M Osama: draw the lineForeGround */
        val blackPolylineOptions = PolylineOptions()
        blackPolylineOptions.color(Color.BLACK)
        blackPolylineOptions.width(5f)
        blackPolyline = googleMap.addPolyline(blackPolylineOptions)

        /* M Osama: draw location/destination vertices */
        originMarker = addOriginDestinationMarkerAndGet(latLngList[0])
        originMarker?.setAnchor(0.5f, 0.5f)
        destinationMarker = addOriginDestinationMarkerAndGet(latLngList[latLngList.size - 1])
        destinationMarker?.setAnchor(0.5f, 0.5f)

        /* M Osama: animation for drawing blackLine over greyLine */
        val polylineAnimator = AnimationUtils.polylineAnimator()
        polylineAnimator.addUpdateListener { valueAnimator ->
            val percentValue = (valueAnimator.animatedValue as Int)
            val index = (grayPolyline?.points!!.size) * (percentValue / 100.0f).toInt()
            blackPolyline?.points = grayPolyline?.points!!.subList(0, index)
        }
        polylineAnimator.start()
    }

    /* M Osama: function used to update car position on Map */
    private fun updateCarLocation(latLng: LatLng) {
        if (movingCabMarker == null) { movingCabMarker = addCarMarkerAndGet(latLng) }

        if (previousLatLng == null) {
            currentLatLng = latLng
            previousLatLng = currentLatLng
            movingCabMarker?.position = currentLatLng
            movingCabMarker?.setAnchor(0.5f, 0.5f)
            animateCamera(currentLatLng!!)
        } else {
            previousLatLng = currentLatLng
            currentLatLng = latLng
            val valueAnimator = AnimationUtils.carAnimator()
            valueAnimator.addUpdateListener { va ->
                if (currentLatLng != null && previousLatLng != null) {
                    val multiplier = va.animatedFraction
                    val nextLocation = LatLng(
                        multiplier * currentLatLng!!.latitude + (1 - multiplier) * previousLatLng!!.latitude,
                        multiplier * currentLatLng!!.longitude + (1 - multiplier) * previousLatLng!!.longitude
                    )
                    movingCabMarker?.position = nextLocation
                    val rotation = MapUtils.getRotation(previousLatLng!!, nextLocation)
                    if (!rotation.isNaN()) { movingCabMarker?.rotation = rotation }

                    movingCabMarker?.setAnchor(0.5f, 0.5f)
                    animateCamera(nextLocation)
                }
            }
            valueAnimator.start()
        }
    }

    /* M Osama: used to show car movement on Map*/
    private fun showMovingCab(cabLatLngList: ArrayList<LatLng>) {
        handler = Handler()
        var index = 0
        runnable = Runnable {
            run {
                if (index < MapUtils.getListOfLocations().size) {
                    updateCarLocation(cabLatLngList[index])
                    handler.postDelayed(runnable, 3000)
                    ++index
                } else {
                    handler.removeCallbacks(runnable)
                    Toast.makeText(this@MainActivity, "Trip Ends", Toast.LENGTH_LONG).show()
                }
            }
        }
        handler.postDelayed(runnable, 5000)
    }

    /* M Osama: called once; only when the map is loaded on the screen */
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        defaultLocation = LatLng(28.435350000000003, 77.11368)
        showDefaultLocationOnMap(defaultLocation)

        Handler().postDelayed(Runnable {
            showPath(MapUtils.getListOfLocations())
            showMovingCab(MapUtils.getListOfLocations())
        }, 3000)
    }

}
