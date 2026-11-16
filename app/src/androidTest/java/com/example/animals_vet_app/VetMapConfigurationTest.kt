package com.example.animals_vet_app

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.views.MapView

@RunWith(AndroidJUnit4::class)
class VetMapConfigurationTest {
    @Test
    fun mapUsesIdentifiableUserAgentAndNewCacheNamespace() {
        ActivityScenario.launch(Vets::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val version = activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
                assertEquals(
                    "AnimalsVetApp/$version (+https://github.com/Nathanial014/Animal-Vet-App)",
                    Configuration.getInstance().userAgentValue
                )
                val source = activity.findViewById<MapView>(R.id.vetMap)
                    .tileProvider.tileSource as OnlineTileSourceBase
                assertEquals("AnimalsVetMapV2", source.name())
                assertFalse(source.tileSourcePolicy.normalizesUserAgent())
                assertFalse(source.tileSourcePolicy.acceptsBulkDownload())
                assertFalse(source.tileSourcePolicy.acceptsPreventive())
                assertTrue(source.tileSourcePolicy.acceptsUserAgent(Configuration.getInstance().userAgentValue))
            }
        }
    }
}
