package com.example.animals_vet_app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar

class AnimalListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AnimalAdapter
    private lateinit var searchInput: EditText
    private lateinit var typeFilter: Spinner
    private lateinit var lifespanFilter: Spinner
    private lateinit var dietFilter: Spinner
    private lateinit var emptyText: View

    private var typeOptions: List<String> = emptyList()
    private var dietOptions: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_animal_list)
        findViewById<View>(R.id.root).applySystemBarsPadding()

        recyclerView = findViewById(R.id.animalRecyclerView)
        searchInput = findViewById(R.id.searchInput)
        typeFilter = findViewById(R.id.typeFilter)
        lifespanFilter = findViewById(R.id.lifespanFilter)
        dietFilter = findViewById(R.id.dietFilter)
        emptyText = findViewById(R.id.emptyText)

        adapter = AnimalAdapter(
            animals = emptyList(),
            onEdit = ::editAnimal,
            onDelete = ::deleteAnimal
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        lifespanFilter.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                getString(R.string.filter_any_lifespan),
                getString(R.string.filter_short_lifespan),
                getString(R.string.filter_medium_lifespan),
                getString(R.string.filter_long_lifespan)
            )
        )

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) =
                applyFilters()

            override fun onNothingSelected(p: AdapterView<*>?) = Unit
        }
        typeFilter.onItemSelectedListener = listener
        lifespanFilter.onItemSelectedListener = listener
        dietFilter.onItemSelectedListener = listener

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) = Unit
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) = Unit
            override fun afterTextChanged(s: Editable?) = applyFilters()
        })

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshFilterOptions()
        applyFilters()
    }

    // Rebuilds the type/diet dropdowns from current data, keeping the selection.
    private fun refreshFilterOptions() {
        val all = AppData.animals.findAll()
        val selectedType = typeOptions.getOrNull(typeFilter.selectedItemPosition)
        val selectedDiet = dietOptions.getOrNull(dietFilter.selectedItemPosition)

        typeOptions = listOf(getString(R.string.filter_all_types)) +
            all.map { it.type.trim() }.filter { it.isNotEmpty() }
                .distinctBy { it.lowercase() }.sortedBy { it.lowercase() }
        dietOptions = listOf(getString(R.string.filter_all_diets)) +
            all.map { it.features.dietPreferred.trim() }.filter { it.isNotEmpty() }
                .distinctBy { it.lowercase() }.sortedBy { it.lowercase() }

        typeFilter.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, typeOptions
        )
        dietFilter.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, dietOptions
        )
        typeFilter.setSelection(typeOptions.indexOf(selectedType).coerceAtLeast(0))
        dietFilter.setSelection(dietOptions.indexOf(selectedDiet).coerceAtLeast(0))
    }

    private fun applyFilters() {
        if (!::adapter.isInitialized || typeOptions.isEmpty()) return

        val query = searchInput.text.toString().trim()
        val type = typeOptions.getOrNull(typeFilter.selectedItemPosition)
            .takeIf { typeFilter.selectedItemPosition > 0 }
        val diet = dietOptions.getOrNull(dietFilter.selectedItemPosition)
            .takeIf { dietFilter.selectedItemPosition > 0 }
        val lifespanChoice = lifespanFilter.selectedItemPosition

        val result = AppData.animals.findAll().filter { animal ->
            (query.isEmpty() ||
                animal.name.contains(query, ignoreCase = true) ||
                animal.type.contains(query, ignoreCase = true) ||
                animal.features.description.contains(query, ignoreCase = true)) &&
                (type == null || animal.type.trim().equals(type, ignoreCase = true)) &&
                (diet == null ||
                    animal.features.dietPreferred.trim().equals(diet, ignoreCase = true)) &&
                matchesLifespan(animal.features.lifespan, lifespanChoice)
        }

        adapter.updateAnimals(result)
        emptyText.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE
    }

    // Lifespan is free text, so the first number in it is used (e.g. "10-12 years" -> 10).
    private fun matchesLifespan(text: String, choice: Int): Boolean {
        if (choice == 0) return true
        val years = Regex("\\d+(\\.\\d+)?").find(text)?.value?.toDoubleOrNull()
            ?: return false
        return when (choice) {
            1 -> years < 5
            2 -> years in 5.0..15.0
            else -> years > 15
        }
    }

    private fun editAnimal(animal: Animal) {
        startActivity(
            AnimalEditActivity.createIntent(this, animal.id)
        )
    }

    private fun deleteAnimal(animal: Animal) {
        val index = AppData.animals.indexOf(animal.id)
        if (AppData.animals.delete(animal.id)) {
            refreshFilterOptions()
            applyFilters()
            Snackbar.make(recyclerView, getString(R.string.animal_deleted, animal.name), Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) {
                    AppData.animals.restore(animal, index)
                    refreshFilterOptions()
                    applyFilters()
                }
                .show()
        }
    }
}
