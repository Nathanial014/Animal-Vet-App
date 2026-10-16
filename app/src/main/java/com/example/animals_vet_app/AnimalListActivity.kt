package com.example.animals_vet_app

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AnimalListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AnimalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_animal_list)
        findViewById<View>(R.id.root).applySystemBarsPadding()

        recyclerView =
            findViewById(R.id.animalRecyclerView)

        adapter = AnimalAdapter(
            animals = AppData.animals.findAll(),
            onEdit = ::editAnimal,
            onDelete = ::deleteAnimal
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.returnButton).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {

            adapter.updateAnimals(AppData.animals.findAll())
        }
    }

    private fun editAnimal(animal: Animal) {
        startActivity(
            AnimalEditActivity.createIntent(this, animal.id)
        )
    }

    private fun deleteAnimal(animal: Animal) {
        if (AppData.animals.delete(animal.id)) {
            adapter.updateAnimals(AppData.animals.findAll())
        }
    }
}
