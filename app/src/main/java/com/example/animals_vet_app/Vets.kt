package com.example.animals_vet_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class Vets : AppCompatActivity() {
    private data class VetLocation(val name: String, val latitude: Double, val longitude: Double)

    private lateinit var map: MapView
    private val locations = mutableListOf<VetLocation>()
    private var locationsAvailable = true
    private var editingIndex: Int? = null
    private val mapPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val index = editingIndex
        editingIndex = null
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val latitude = data?.getDoubleExtra(MapPickerActivity.EXTRA_LATITUDE, Double.NaN)
            val longitude = data?.getDoubleExtra(MapPickerActivity.EXTRA_LONGITUDE, Double.NaN)
            if (index == null || index !in locations.indices ||
                latitude == null || latitude !in -90.0..90.0 ||
                longitude == null || longitude !in -180.0..180.0
            ) {
                Toast.makeText(this, R.string.vet_update_failed, Toast.LENGTH_LONG).show()
            } else {
                val updated = locations.toMutableList()
                updated[index] = updated[index].copy(latitude = latitude, longitude = longitude)
                if (saveLocations(updated)) {
                    locations[index] = updated[index]
                    refreshMarkers()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VetMap.configure(this)
        setContentView(R.layout.activity_vets)
        findViewById<View>(R.id.root).applySystemBarsPadding()
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        map = findViewById(R.id.vetMap)
        VetMap.setup(map)
        editingIndex = savedInstanceState?.getInt(STATE_EDITING_INDEX, -1)?.takeIf { it >= 0 }
        map.controller.setZoom(savedInstanceState?.getDouble("zoom") ?: 2.0)
        map.controller.setCenter(
            GeoPoint(
                savedInstanceState?.getDouble("latitude") ?: 0.0,
                savedInstanceState?.getDouble("longitude") ?: 0.0
            )
        )
        map.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                if (locationsAvailable) addLocation(point)
                return true
            }

            override fun longPressHelper(point: GeoPoint): Boolean = false
        }))

        loadLocations()
        locations.forEach(::addMarker)
    }

    private fun loadLocations() {
        val saved = getPreferences(MODE_PRIVATE).getString("locations", "[]") ?: "[]"
        try {
            val array = JSONArray(saved)
            val loaded = mutableListOf<VetLocation>()
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val name = item.getString("name")
                val latitude = item.getDouble("latitude")
                val longitude = item.getDouble("longitude")
                if (name.isBlank() || latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
                    throw JSONException("Invalid vet location")
                }
                loaded.add(VetLocation(name, latitude, longitude))
            }
            locations.addAll(loaded)
        } catch (error: JSONException) {
            locationsAvailable = false
            Toast.makeText(this, R.string.vet_load_failed, Toast.LENGTH_LONG).show()
        }
    }

    private fun saveLocations(updated: List<VetLocation>): Boolean {
        val array = JSONArray()
        updated.forEach { location ->
            array.put(JSONObject().apply {
                put("name", location.name)
                put("latitude", location.latitude)
                put("longitude", location.longitude)
            })
        }
        val preferences = getPreferences(MODE_PRIVATE)
        val previous = preferences.getString("locations", "[]")
        if (!preferences.edit().putString("locations", array.toString()).commit()) {
            preferences.edit().putString("locations", previous).commit()
            Toast.makeText(this, R.string.vet_save_failed, Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun addLocation(point: GeoPoint) {
        val input = EditText(this).apply {
            setHint(R.string.vet_name)
            setSingleLine()
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.add_vet_title)
            .setView(input)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    input.error = getString(R.string.vet_name_required)
                } else {
                    val location = VetLocation(name, point.latitude, point.longitude)
                    if (saveLocations(locations + location)) {
                        locations.add(location)
                        addMarker(location)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun addMarker(location: VetLocation) {
        val marker = Marker(map).apply {
            position = GeoPoint(location.latitude, location.longitude)
            title = location.name
            icon = requireNotNull(AppCompatResources.getDrawable(this@Vets, R.drawable.ic_vet_marker))
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            setOnMarkerClickListener { _, _ ->
                AlertDialog.Builder(this@Vets)
                    .setTitle(location.name)
                    .setMessage(R.string.vet_pin_actions)
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.edit) { _, _ ->
                        val index = locations.indexOfFirst { it === location }
                        if (index >= 0) {
                            editingIndex = index
                            mapPicker.launch(
                                Intent(this@Vets, MapPickerActivity::class.java)
                                    .putExtra(MapPickerActivity.EXTRA_LATITUDE, location.latitude)
                                    .putExtra(MapPickerActivity.EXTRA_LONGITUDE, location.longitude)
                                    .putExtra(MapPickerActivity.EXTRA_TITLE, location.name)
                            )
                        } else {
                            Toast.makeText(this@Vets, R.string.vet_update_failed, Toast.LENGTH_LONG).show()
                        }
                    }
                    .setPositiveButton(R.string.delete) { _, _ ->
                        AlertDialog.Builder(this@Vets)
                            .setTitle(location.name)
                            .setMessage(R.string.delete_vet_message)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.delete) { _, _ ->
                                val index = locations.indexOfFirst { it === location }
                                if (index >= 0) {
                                    val updated = locations.toMutableList()
                                    updated.removeAt(index)
                                    if (saveLocations(updated)) {
                                        locations.removeAt(index)
                                        map.overlays.remove(this)
                                        map.invalidate()
                                    }
                                } else {
                                    Toast.makeText(this@Vets, R.string.vet_update_failed, Toast.LENGTH_LONG).show()
                                }
                            }
                            .show()
                    }
                    .show()
                true
            }
        }
        map.overlays.add(marker)
        map.invalidate()
    }

    private fun refreshMarkers() {
        map.overlays.removeAll { it is Marker }
        locations.forEach(::addMarker)
        map.invalidate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putDouble("latitude", map.mapCenter.latitude)
        outState.putDouble("longitude", map.mapCenter.longitude)
        outState.putDouble("zoom", map.zoomLevelDouble)
        editingIndex?.let { outState.putInt(STATE_EDITING_INDEX, it) }
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
        private const val STATE_EDITING_INDEX = "editing_index"
    }
}
