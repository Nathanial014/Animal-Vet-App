package com.example.animals_vet_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import android.text.InputType
import androidx.appcompat.app.AlertDialog
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import android.net.Uri


class AnimalEditActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var lifespanInput: EditText
    private lateinit var dietPreferredInput: EditText
    private lateinit var typeImagePreview: ImageView
    private var selectedImage = ""
    private var fallbackImageName = ""
    private lateinit var typeInput: Spinner
    private lateinit var toolbar: MaterialToolbar
    private var initialState = ""

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

        findViewById<Button>(R.id.chooseImageButton).setOnClickListener {
            showImageUrlDialog()
        }

        findViewById<Button>(R.id.saveButton).setOnClickListener { saveAnimal() }
        findViewById<Button>(R.id.cancelButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        toolbar.setTitle(
            if (editingId == null) R.string.add_animal_title else R.string.edit_animal_title
        )
        editingId?.let(::loadExistingAnimal)
        savedInstanceState?.getString(STATE_IMAGE)?.let { imageUri ->
            selectedImage = imageUri
            showSelectedImage()
        }
        initialState = formState()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (formState() == initialState) finish() else confirmDiscard()
            }
        })
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        nameInput = findViewById(R.id.nameInput)
        typeInput = findViewById(R.id.typeInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        lifespanInput = findViewById(R.id.lifespanInput)
        dietPreferredInput = findViewById(R.id.dietPreferredInput)
        typeImagePreview = findViewById(R.id.typeImagePreview)

        typeInput.adapter = ArrayAdapter(
            this@AnimalEditActivity,
            android.R.layout.simple_spinner_item,
            ANIMAL_TYPES
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun formState() = listOf(
        nameInput.text.toString().trim(),
        typeInput.selectedItemPosition.toString(),
        descriptionInput.text.toString().trim(),
        lifespanInput.text.toString().trim(),
        dietPreferredInput.text.toString().trim(),
        selectedImage
    ).joinToString("\u0000")

    private fun confirmDiscard() {
        AlertDialog.Builder(this)
            .setTitle(R.string.discard_changes_title)
            .setMessage(R.string.discard_changes_message)
            .setPositiveButton(R.string.discard) { _, _ -> finish() }
            .setNegativeButton(R.string.keep_editing, null)
            .show()
    }

    private fun showImageUrlDialog() {
        val urlInput = EditText(this).apply {
            hint = getString(R.string.image_url_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
            setText(selectedImage.takeIf { it.startsWith("https://", ignoreCase = true) }.orEmpty())
            setSelection(text.length)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.image_url_title)
            .setView(urlInput)
            .setPositiveButton(R.string.use_image_url, null)
            .setNeutralButton(R.string.clear_image, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val enteredUrl = urlInput.text.toString().trim()
                if (enteredUrl.isNotEmpty()) {
                    val uri = Uri.parse(enteredUrl)
                    if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank()) {
                        urlInput.error = getString(R.string.image_url_invalid)
                        return@setOnClickListener
                    }
                }

                selectedImage = enteredUrl
                showSelectedImage()
                dialog.dismiss()
            }
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                selectedImage = ""
                showSelectedImage()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showSelectedImage() {
        AnimalImages.show(typeImagePreview, selectedImage, fallbackImageName) {
            Toast.makeText(this, R.string.image_load_failed, Toast.LENGTH_SHORT).show()
        }
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
        fallbackImageName = animal.features.typeImage
        selectedImage = animal.imageUri
            ?.takeIf { it.isNotBlank() }
            ?: animal.features.imageUri.orEmpty()
        showSelectedImage()
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
                typeImage = fallbackImageName,
                imageUri = selectedImage
            ),
            imageUri = selectedImage
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
