package com.example.animals_vet_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnimalAdapter(
    private var animals: List<Animal>,
    private val onEdit: (Animal) -> Unit,
    private val onDelete: (Animal) -> Unit
) : RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder>() {

    class AnimalViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val animalImage: ImageView =
            itemView.findViewById(R.id.animalImage)

        val titleText: TextView =
            itemView.findViewById(R.id.titleText)

        val descriptionText: TextView =
            itemView.findViewById(R.id.descriptionText)

        val detailsText: TextView =
            itemView.findViewById(R.id.detailsText)

        val editButton: Button =
            itemView.findViewById(R.id.editButton)

        val deleteButton: Button =
            itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AnimalViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_animal,
                parent,
                false
            )

        return AnimalViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AnimalViewHolder,
        position: Int
    ) {

        val animal = animals[position]

        holder.animalImage.setImageResource(
            AnimalImages.resourceFor(holder.itemView.context, animal.features.typeImage)
        )

        holder.titleText.text =
            "${animal.name} (${animal.type})"

        holder.descriptionText.text =
            animal.features.description

        holder.detailsText.text =
            holder.itemView.context.getString(
                R.string.animal_details,
                animal.id,
                animal.features.lifespan,
                animal.features.dietPreferred
            )

        holder.editButton.setOnClickListener {
            onEdit(animal)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(animal)
        }
    }

    override fun getItemCount(): Int {
        return animals.size
    }

    fun updateAnimals(newAnimals: List<Animal>) {
        animals = newAnimals
        notifyDataSetChanged()
    }
}