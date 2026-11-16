package com.example.animals_vet_app

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@RunWith(AndroidJUnit4::class)
class VetLocationEditingTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private var originalLocations: String? = null

    @Before
    fun setUp() {
        val preferences = context.getSharedPreferences("Vets", Context.MODE_PRIVATE)
        originalLocations = preferences.getString("locations", null)
        preferences.edit().putString(
            "locations",
            """[{"name":"First vet","latitude":0.0,"longitude":0.0},{"name":"Second vet","latitude":30.0,"longitude":30.0}]"""
        ).commit()
    }

    @After
    fun tearDown() {
        context.getSharedPreferences("Vets", Context.MODE_PRIVATE)
            .edit().putString("locations", originalLocations).commit()
    }

    private fun openFirstVet() {
        onView(withId(R.id.vetMap)).perform(
            GeneralClickAction(Tap.SINGLE, { view ->
                val map = view as MapView
                val point = map.projection.toPixels(GeoPoint(0.0, 0.0), Point())
                val position = IntArray(2)
                map.getLocationOnScreen(position)
                floatArrayOf(
                    position[0] + point.x.toFloat(),
                    position[1] + point.y.toFloat() - 10 * view.resources.displayMetrics.density
                )
            }, Press.FINGER)
        )
        SystemClock.sleep(600)
        onView(withText(R.string.edit)).perform(click())
    }

    @Test
    fun editingStartsAtSavedPinAndPersistsOnlyConfirmedChange() {
        ActivityScenario.launch(Vets::class.java).use { scenario ->
            openFirstVet()
            onView(withText("First vet")).check(matches(isDisplayed()))
            onView(withId(R.id.cancelLocationButton)).perform(click())
            var stored = JSONArray(context.getSharedPreferences("Vets", Context.MODE_PRIVATE)
                .getString("locations", "[]"))
            assertEquals(0.0, stored.getJSONObject(0).getDouble("latitude"), 0.0)
            assertEquals(0.0, stored.getJSONObject(0).getDouble("longitude"), 0.0)

            openFirstVet()
            onView(withId(R.id.pickerMap)).perform(
                GeneralClickAction(Tap.SINGLE, { view ->
                    val position = IntArray(2)
                    view.getLocationOnScreen(position)
                    floatArrayOf(
                        position[0] + view.width * 0.75f,
                        position[1] + view.height * 0.75f
                    )
                }, Press.FINGER)
            )
            SystemClock.sleep(600)
            onView(withId(R.id.confirmLocationButton)).perform(click())
            scenario.recreate()
            stored = JSONArray(context.getSharedPreferences("Vets", Context.MODE_PRIVATE)
                .getString("locations", "[]"))
            assertNotEquals(0.0, stored.getJSONObject(0).getDouble("latitude"), 0.0)
            assertNotEquals(0.0, stored.getJSONObject(0).getDouble("longitude"), 0.0)
            assertEquals(30.0, stored.getJSONObject(1).getDouble("latitude"), 0.0)
            assertEquals(30.0, stored.getJSONObject(1).getDouble("longitude"), 0.0)
            scenario.onActivity { activity ->
                val markers = activity.findViewById<MapView>(R.id.vetMap)
                    .overlays.filterIsInstance<Marker>()
                assertEquals(2, markers.size)
                assertEquals(stored.getJSONObject(0).getDouble("latitude"), markers[0].position.latitude, 0.0)
                assertEquals(stored.getJSONObject(0).getDouble("longitude"), markers[0].position.longitude, 0.0)
            }
        }
    }

    @Test
    fun pickerStartsAtIntentCoordinatesAndRetainsSelectionOnRecreation() {
        val intent = Intent(context, MapPickerActivity::class.java)
            .putExtra(MapPickerActivity.EXTRA_LATITUDE, 52.245954)
            .putExtra(MapPickerActivity.EXTRA_LONGITUDE, -7.139539)
            .putExtra(MapPickerActivity.EXTRA_TITLE, "Test vet")
        ActivityScenario.launch<MapPickerActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val map = activity.findViewById<MapView>(R.id.pickerMap)
                val marker = map.overlays.filterIsInstance<Marker>().single()
                assertEquals(52.245954, marker.position.latitude, 0.000001)
                assertEquals(-7.139539, marker.position.longitude, 0.000001)
                assertEquals("Test vet", marker.title)
                assertEquals("Test vet", activity.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).subtitle)
                assertEquals(52.245954, map.mapCenter.latitude, 0.001)
                assertEquals(-7.139539, map.mapCenter.longitude, 0.001)
            }
            onView(withId(R.id.pickerMap)).perform(
                GeneralClickAction(Tap.SINGLE, { view ->
                    val position = IntArray(2)
                    view.getLocationOnScreen(position)
                    floatArrayOf(position[0] + view.width * 0.75f, position[1] + view.height * 0.75f)
                }, Press.FINGER)
            )
            SystemClock.sleep(600)
            var selected = GeoPoint(0.0, 0.0)
            scenario.onActivity { activity ->
                selected = activity.findViewById<MapView>(R.id.pickerMap)
                    .overlays.filterIsInstance<Marker>().single().position
            }
            assertNotEquals(52.245954, selected.latitude, 0.000001)
            scenario.recreate()
            scenario.onActivity { activity ->
                val marker = activity.findViewById<MapView>(R.id.pickerMap)
                    .overlays.filterIsInstance<Marker>().single()
                assertEquals(selected.latitude, marker.position.latitude, 0.000001)
                assertEquals(selected.longitude, marker.position.longitude, 0.000001)
            }
        }
    }
}
