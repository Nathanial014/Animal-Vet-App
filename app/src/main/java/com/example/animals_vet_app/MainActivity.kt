package com.example.animals_vet_app

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var listLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUserInterface()
    }

    override fun onResume() {
        super.onResume()

        if (::listLayout.isInitialized) {
            displayAnimals()
        }
    }

    private fun createUserInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 320, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Animals"
            textSize = 28f
            gravity = Gravity.CENTER
        }

        val addButton = Button(this).apply {
            text = "Add Animal"
            setOnClickListener {
                val intent = Intent(this@MainActivity, EditAnimals::class.java)
                startActivity(intent)
            }
        }

        listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            addButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            listLayout,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(root)

        displayAnimals()
    }

    private fun displayAnimals() {

        listLayout.removeAllViews()

        val animals = AppData.animals.findAll()

        if (animals.isEmpty()) {

            val emptyText = TextView(this).apply {
                text = "No animals yet."
                textSize = 18f
                setPadding(0, 40, 0, 40)
            }

            listLayout.addView(emptyText)

            return
        }

        for (animal in animals) {

            val animalLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 20, 0, 20)
            }

            val animalTitle = TextView(this).apply {
                text = "${animal.id}: ${animal.name} (${animal.type})"
                textSize = 20f
            }

            val animalDescription = TextView(this).apply {
                text = animal.features.description
                textSize = 16f
            }

            val animalFeatures = TextView(this).apply {
                text = "Lifespan: ${animal.features.lifespan}\n" +
                    "Preferred diet: ${animal.features.dietPreferred}"
                textSize = 14f
            }

            val editButton = Button(this).apply {
                text = "Edit"

                setOnClickListener {
                    val intent = Intent(
                        this@MainActivity,
                        EditAnimals::class.java
                    )

                    intent.putExtra("id", animal.id)

                    startActivity(intent)
                }
            }

            val deleteButton = Button(this).apply {
                text = "Delete"

                setOnClickListener {
                    AppData.animals.delete(animal.id)
                    displayAnimals()
                }
            }

            animalLayout.addView(animalTitle)
            animalLayout.addView(animalDescription)
            animalLayout.addView(animalFeatures)
            animalLayout.addView(editButton)
            animalLayout.addView(deleteButton)

            listLayout.addView(animalLayout)
        }
    }
}
