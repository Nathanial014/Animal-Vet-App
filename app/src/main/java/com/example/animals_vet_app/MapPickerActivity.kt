package com.example.animals_vet_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.appbar.MaterialToolbar
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class MapPickerActivity : AppCompatActivity() {
    private lateinit var map: MapView
    private lateinit var selectedPoint: GeoPoint
    private lateinit var marker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VetMap.configure(this)
        setContentView(R.layout.activity_map_picker)
        findViewById<View>(R.id.root).applySystemBarsPadding()
        val title = intent.getStringExtra(EXTRA_TITLE)?.takeIf { it.isNotBlank() }
            ?: getString(R.string.selected_location)
        findViewById<MaterialToolbar>(R.id.toolbar).apply {
            subtitle = title
            setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        map = findViewById(R.id.pickerMap)
        VetMap.setup(map)

        val latitude = intent.getDoubleExtra(EXTRA_LATITUDE, Double.NaN)
        val longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN)
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            Toast.makeText(this, R.string.vet_update_failed, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        selectedPoint = GeoPoint(
            savedInstanceState?.getDouble(STATE_SELECTED_LATITUDE) ?: latitude,
            savedInstanceState?.getDouble(STATE_SELECTED_LONGITUDE) ?: longitude
        )
        map.controller.setZoom(savedInstanceState?.getDouble(STATE_ZOOM) ?: 16.0)
        map.controller.setCenter(
            GeoPoint(
                savedInstanceState?.getDouble(STATE_CENTER_LATITUDE) ?: latitude,
                savedInstanceState?.getDouble(STATE_CENTER_LONGITUDE) ?: longitude
            )
        )
        map.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                selectPoint(point)
                return true
            }

            override fun longPressHelper(point: GeoPoint): Boolean {
                selectPoint(point)
                return true
            }
        }))
        marker = Marker(map).apply {
            this.title = title
            icon = requireNotNull(
                AppCompatResources.getDrawable(this@MapPickerActivity, R.drawable.ic_selected_location_marker)
            )
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        map.overlays.add(marker)
        selectPoint(selectedPoint)

        findViewById<Button>(R.id.confirmLocationButton).setOnClickListener {
            setResult(
                Activity.RESULT_OK,
                Intent()
                    .putExtra(EXTRA_LATITUDE, selectedPoint.latitude)
                    .putExtra(EXTRA_LONGITUDE, selectedPoint.longitude)
            )
            finish()
        }
        findViewById<Button>(R.id.cancelLocationButton).setOnClickListener { finish() }
    }

    private fun selectPoint(point: GeoPoint) {
        selectedPoint = point
        marker.position = point
        map.invalidate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::selectedPoint.isInitialized) {
            outState.putDouble(STATE_SELECTED_LATITUDE, selectedPoint.latitude)
            outState.putDouble(STATE_SELECTED_LONGITUDE, selectedPoint.longitude)
            outState.putDouble(STATE_CENTER_LATITUDE, map.mapCenter.latitude)
            outState.putDouble(STATE_CENTER_LONGITUDE, map.mapCenter.longitude)
            outState.putDouble(STATE_ZOOM, map.zoomLevelDouble)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        map.onPause()
        super.onPause()
    }

    companion object {
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_TITLE = "title"
        private const val STATE_SELECTED_LATITUDE = "selected_latitude"
        private const val STATE_SELECTED_LONGITUDE = "selected_longitude"
        private const val STATE_CENTER_LATITUDE = "center_latitude"
        private const val STATE_CENTER_LONGITUDE = "center_longitude"
        private const val STATE_ZOOM = "zoom"
    }
}
