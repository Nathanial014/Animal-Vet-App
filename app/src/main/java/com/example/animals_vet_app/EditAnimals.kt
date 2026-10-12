package com.example.animals_vet_app

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditAnimals : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var lifespanInput: EditText
    private lateinit var dietPreferredInput: EditText
    private lateinit var typeImageInput: EditText
    private lateinit var typeInput: Spinner

    private var editingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUserInterface()

        editingId = if (intent.hasExtra(EXTRA_ANIMAL_ID)) {
            intent.getLongExtra(EXTRA_ANIMAL_ID, NO_ANIMAL_ID)
                .takeIf { it != NO_ANIMAL_ID }
        } else {
            null
        }

        editingId?.let(::loadExistingAnimal)
    }

    private fun createUserInterface() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        nameInput = addTextInput(content, "Animal name")
        typeInput = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@EditAnimals,
                android.R.layout.simple_spinner_item,
                ANIMAL_TYPES
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        }
        content.addView(typeInput)

        descriptionInput = addTextInput(content, "Description")
        lifespanInput = addTextInput(content, "Lifespan")
        dietPreferredInput = addTextInput(content, "Preferred diet")
        typeImageInput = addTextInput(content, "Type image URL (Google API)")

        val saveButton = Button(this).apply {
            text = "Save"
            setOnClickListener { saveAnimal() }
        }
        content.addView(saveButton)

        val cancelButton = Button(this).apply {
            text = "Cancel"
            setOnClickListener { finish() }
        }
        content.addView(cancelButton)

        setContentView(ScrollView(this).apply {
            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        })
    }

    private fun addTextInput(parent: LinearLayout, hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            parent.addView(
                this,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun loadExistingAnimal(id: Long) {
        val animal = AppData.animals.findOne(id)
        if (animal == null) {
            Toast.makeText(this, "Animal not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        nameInput.setText(animal.name)
        val typePosition = ANIMAL_TYPES.indexOf(animal.type)
        typeInput.setSelection(typePosition.coerceAtLeast(0))
        descriptionInput.setText(animal.features.description)
        lifespanInput.setText(animal.features.lifespan)
        dietPreferredInput.setText(animal.features.dietPreferred)
        typeImageInput.setText(animal.features.typeImage)
    }

    private fun saveAnimal() {
        val name = nameInput.text.toString().trim()
        if (name.isEmpty()) {
            nameInput.error = "Animal name is required"
            return
        }

        val animal = Animal(
            id = editingId ?: NO_ANIMAL_ID,
            name = name,
            type = typeInput.selectedItem.toString(),
            features = Features(
                description = descriptionInput.text.toString().trim(),
                lifespan = lifespanInput.text.toString().trim(),
                dietPreferred = dietPreferredInput.text.toString().trim(),
                typeImage = typeImageInput.text.toString().trim()
            )
        )

        if (editingId == null) {
            AppData.animals.create(animal)
            Toast.makeText(this, "Animal added", Toast.LENGTH_SHORT).show()
        } else if (AppData.animals.update(animal)) {
            Toast.makeText(this, "Animal updated", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Animal could not be updated", Toast.LENGTH_SHORT).show()
            return
        }

        finish()
    }

    private companion object {
        const val EXTRA_ANIMAL_ID = "id"
        const val NO_ANIMAL_ID = -1L
        val ANIMAL_TYPES = listOf("Mammal", "Feline", "Aquatic")
    }
}
