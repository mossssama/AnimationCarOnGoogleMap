package com.mindorks.example.ubercaranimation.Util.Logic

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.mindorks.example.ubercaranimation.R
import com.mindorks.example.ubercaranimation.Util.UI.AnimationUtils.carAnimator
import com.mindorks.example.ubercaranimation.Util.UI.CameraUtils
import kotlin.math.abs
import kotlin.math.atan

object MapUtils {

    private var movingCabMarker: Marker? = null
    private var previousLatLng: LatLng? = null
    private var currentLatLng: LatLng? = null
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    /* M Osama: get the car image after adjusting it to fit screen */
    fun getCarBitmap(context: Context): Bitmap {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_car)
        return Bitmap.createScaledBitmap(bitmap, 50, 100, false)
    }

    /* M Osama: build a squared vertex to be used for location & destination*/
    fun getOriginDestinationMarkerBitmap(): Bitmap {
        val height = 20
        val width = 20
        val bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }

    /* M Osama: responsible for car rotations */
    private fun getRotation(start: LatLng, end: LatLng): Float {
        val latDifference: Double = abs(start.latitude - end.latitude)
        val lngDifference: Double = abs(start.longitude - end.longitude)
        var rotation = -1F
        when {
            start.latitude < end.latitude && start.longitude < end.longitude -> { rotation = Math.toDegrees(atan(lngDifference / latDifference)).toFloat() }
            start.latitude >= end.latitude && start.longitude < end.longitude -> { rotation = (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 90).toFloat() }
            start.latitude >= end.latitude && start.longitude >= end.longitude -> { rotation = (Math.toDegrees(atan(lngDifference / latDifference)) + 180).toFloat() }
            start.latitude < end.latitude && start.longitude >= end.longitude -> { rotation = (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 270).toFloat() }
        }
        return rotation
    }

    /* M Osama: function used to update car position on Map */
    private fun updateCarLocation(googleMap: GoogleMap,context: Context, latLng: LatLng) {
        if (movingCabMarker == null) { movingCabMarker = PathUtils.addCarMarkerAndGet(googleMap, latLng, context) }

        if (previousLatLng == null) {
            currentLatLng = latLng
            previousLatLng = currentLatLng
            movingCabMarker?.position = currentLatLng
            movingCabMarker?.setAnchor(0.5f, 0.5f)
            CameraUtils.animateCamera(googleMap, currentLatLng!!)
        } else {
            previousLatLng = currentLatLng
            currentLatLng = latLng
            val valueAnimator = carAnimator()
            valueAnimator.addUpdateListener { va: ValueAnimator ->
                if (currentLatLng != null && previousLatLng != null) {
                    val multiplier = va.animatedFraction
                    val nextLocation = LatLng(
                        multiplier * currentLatLng!!.latitude + (1 - multiplier) * previousLatLng!!.latitude,
                        multiplier * currentLatLng!!.longitude + (1 - multiplier) * previousLatLng!!.longitude
                    )
                    movingCabMarker?.position = nextLocation
                    val rotation = getRotation(previousLatLng!!, nextLocation)
                    if (!rotation.isNaN()) { movingCabMarker?.rotation = rotation }

                    movingCabMarker?.setAnchor(0.5f, 0.5f)
                    CameraUtils.animateCamera(googleMap, nextLocation)
                }
            }
            valueAnimator.start()
        }
    }

    /* M Osama: used to show car movement on Map*/
    fun showMovingCab(googleMap: GoogleMap,context: Context,cabLatLngList: ArrayList<LatLng>) {
        handler = Handler()
        var index = 0
        runnable = Runnable {
            run {
                if (index < getListOfLocations().size) {
                    updateCarLocation(googleMap,context,cabLatLngList[index])
                    handler.postDelayed(runnable, 3000)
                    ++index
                } else {
                    handler.removeCallbacks(runnable)
                    Toast.makeText(context, "Trip Ends", Toast.LENGTH_LONG).show()
                }
            }
        }
        handler.postDelayed(runnable, 5000)
    }


    /* M Osama: locations used to build the path; each location is a vertex */
    fun getListOfLocations(): ArrayList<LatLng> {
        val locationList = ArrayList<LatLng>()
        locationList.add(LatLng(30.02735999999999,31.2559448))  /* Location */
        locationList.add(LatLng(30.0306686, 31.232628))         /* Inner location */
        locationList.add(LatLng(30.0211564, 31.2256465))        /* Inner location */
        locationList.add(LatLng(30.0154402, 31.2118712))        /* Destination */
        return locationList
    }
    /* ToBeEdited*/

}