package com.example.animals_vet_app

import android.content.Context
import android.os.SystemClock
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@RunWith(AndroidJUnit4::class)
class VetsInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private var originalLocations: String? = null

    @Before
    fun clearLocations() {
        val preferences = context.getSharedPreferences("Vets", Context.MODE_PRIVATE)
        originalLocations = preferences.getString("locations", null)
        preferences.edit().remove("locations").commit()
    }

    @After
    fun restoreLocations() {
        context.getSharedPreferences("Vets", Context.MODE_PRIVATE)
            .edit().putString("locations", originalLocations).commit()
    }

    @Test
    fun addedLocationSurvivesRecreationAndCanBeRemoved() {
        ActivityScenario.launch(Vets::class.java).use { scenario ->
            onView(withId(R.id.vetMap)).perform(click())
            SystemClock.sleep(600)
            onView(isAssignableFrom(EditText::class.java)).perform(replaceText("Test vet"))
            onView(withText(R.string.save)).perform(click())

            scenario.recreate()
            scenario.onActivity { activity ->
                val markers = activity.findViewById<MapView>(R.id.vetMap)
                    .overlays.filterIsInstance<Marker>()
                assertEquals(1, markers.size)
                assertEquals("Test vet", markers.single().title)
            }

            onView(withId(R.id.vetMap)).perform(
                GeneralClickAction(Tap.SINGLE, { view ->
                    val position = IntArray(2)
                    view.getLocationOnScreen(position)
                    floatArrayOf(
                        position[0] + view.width / 2f,
                        position[1] + view.height / 2f - 10 * view.resources.displayMetrics.density
                    )
                }, Press.FINGER)
            )
            SystemClock.sleep(600)
            onView(withText(R.string.delete)).perform(click())
            onView(withText(R.string.delete)).perform(click())
            scenario.recreate()
            scenario.onActivity { activity ->
                assertEquals(
                    0,
                    activity.findViewById<MapView>(R.id.vetMap)
                        .overlays.filterIsInstance<Marker>().size
                )
            }
        }

    }
}
