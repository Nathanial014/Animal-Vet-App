package com.example.animals_vet_app

import android.content.Context
import androidx.annotation.DrawableRes

object AnimalImages {
    fun names(context: Context): List<String> =
        context.resources.getStringArray(R.array.animal_image_names).toList()

    @DrawableRes
    fun resourceFor(context: Context, name: String): Int {
        if (name.isEmpty()) return R.drawable.ic_animal_placeholder
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        return if (id != 0) id else R.drawable.ic_animal_placeholder
    }
}
