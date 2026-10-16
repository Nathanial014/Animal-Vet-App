package com.example.animals_vet_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AnimalEditActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var lifespanInput: EditText
    private lateinit var dietPreferredInput: EditText
    private lateinit var typeImagePreview: ImageView
    private var selectedImage = ""
    private lateinit var typeInput: Spinner
    private lateinit var formTitle: TextView

    private var editingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editingId = if (intent.hasExtra(EXTRA_ANIMAL_ID)) {
            intent.getLongExtra(EXTRA_ANIMAL_ID, NO_ANIMAL_ID)
                .takeIf { it != NO_ANIMAL_ID }
        } else {
            null
        }

        setContentView(R.layout.activity_add_edit)
        findViewById<View>(R.id.root).applySystemBarsPadding()
        bindViews()
        findViewById<Button>(R.id.saveButton).setOnClickListener { saveAnimal() }
        findViewById<Button>(R.id.cancelButton).setOnClickListener { finish() }
        formTitle.setText(
            if (editingId == null) R.string.add_animal_title else R.string.edit_animal_title
        )
        editingId?.let(::loadExistingAnimal)
        setSelectedImage(savedInstanceState?.getString(STATE_IMAGE) ?: selectedImage)
    }

    private fun bindViews() {
        formTitle = findViewById(R.id.formTitle)
        nameInput = findViewById(R.id.nameInput)
        typeInput = findViewById(R.id.typeInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        lifespanInput = findViewById(R.id.lifespanInput)
        dietPreferredInput = findViewById(R.id.dietPreferredInput)
        typeImagePreview = findViewById(R.id.typeImagePreview)
        findViewById<Button>(R.id.chooseImageButton).setOnClickListener { showImagePicker() }
        typeImagePreview.setOnClickListener { showImagePicker() }

        typeInput.adapter = ArrayAdapter(
            this@AnimalEditActivity,
            android.R.layout.simple_spinner_item,
            ANIMAL_TYPES
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun showImagePicker() {
        val names = AnimalImages.names(this)
        val grid = layoutInflater.inflate(R.layout.dialog_image_picker, null) as GridView

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.animal_image)
            .setView(grid)
            .setNeutralButton(R.string.clear_image) { _, _ -> setSelectedImage("") }
            .setNegativeButton(R.string.cancel, null)
            .create()

        grid.adapter = object : BaseAdapter() {
            override fun getCount() = names.size
            override fun getItem(position: Int) = names[position]
            override fun getItemId(position: Int) = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView as? ImageView
                    ?: layoutInflater.inflate(R.layout.item_image_choice, parent, false) as ImageView
                view.setImageResource(AnimalImages.resourceFor(this@AnimalEditActivity, names[position]))
                return view
            }
        }
        grid.setOnItemClickListener { _, _, position, _ ->
            setSelectedImage(names[position])
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setSelectedImage(name: String) {
        selectedImage = name
        typeImagePreview.setImageResource(AnimalImages.resourceFor(this, name))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_IMAGE, selectedImage)
    }

    private fun loadExistingAnimal(id: Long) {
        val animal = AppData.animals.findOne(id)
        if (animal == null) {
            Toast.makeText(this, R.string.animal_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        nameInput.setText(animal.name)
        val typePosition = ANIMAL_TYPES.indexOf(animal.type)
        typeInput.setSelection(typePosition.coerceAtLeast(0))
        descriptionInput.setText(animal.features.description)
        lifespanInput.setText(animal.features.lifespan)
        dietPreferredInput.setText(animal.features.dietPreferred)
        setSelectedImage(animal.features.typeImage)
    }

    private fun saveAnimal() {
        val name = nameInput.text.toString().trim()
        if (name.isEmpty()) {
            nameInput.error = getString(R.string.animal_name_required)
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
                typeImage = selectedImage
            )
        )

        if (editingId == null) {
            AppData.animals.create(animal)
            Toast.makeText(this, R.string.animal_added, Toast.LENGTH_SHORT).show()
        } else if (AppData.animals.update(animal)) {
            Toast.makeText(this, R.string.animal_updated, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.animal_update_failed, Toast.LENGTH_SHORT).show()
            return
        }

        finish()
    }

    companion object {
        const val EXTRA_ANIMAL_ID = "id"
        const val NO_ANIMAL_ID = -1L
        private const val STATE_IMAGE = "selected_image"
        val ANIMAL_TYPES = listOf("Mammal", "Feline", "Aquatic")

        fun createIntent(context: Context, animalId: Long) =
            Intent(context, AnimalEditActivity::class.java).apply {
                putExtra(EXTRA_ANIMAL_ID, animalId)
            }
    }
}

