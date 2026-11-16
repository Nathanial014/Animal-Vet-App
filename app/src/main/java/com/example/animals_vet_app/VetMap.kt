package com.example.animals_vet_app

import android.content.Context
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.MapView

object VetMap {
    fun configure(context: Context) {
        Configuration.getInstance().load(
            context.applicationContext,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        Configuration.getInstance().userAgentValue =
            "AnimalsVetApp/$version (+https://github.com/Nathanial014/Animal-Vet-App)"
    }

    fun setup(map: MapView) {
        val osm = TileSourceFactory.MAPNIK
        // The old Mapnik cache can contain OSM's blocked-access image as a successful tile.
        map.setTileSource(
            XYTileSource(
                "AnimalsVetMapV2",
                osm.minimumZoomLevel,
                osm.maximumZoomLevel,
                osm.tileSizePixels,
                osm.imageFilenameEnding(),
                arrayOf(osm.baseUrl),
                osm.copyrightNotice,
                TileSourcePolicy(
                    osm.tileSourcePolicy.maxConcurrent,
                    TileSourcePolicy.FLAG_NO_BULK or TileSourcePolicy.FLAG_NO_PREVENTIVE or
                        TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                )
            )
        )
        map.setMultiTouchControls(true)
    }
}
