package com.example.animals_vet_app

data class Animal(
    var name: String,
    var type: String,
    var features: Features,
    var id: Long = 0L,
    var imageUri: String? = null
) {
    fun validate() {
        require(name.isNotBlank()) {
            "Name must not be empty."
        }

        require(type.isNotBlank()) {
            "Name must not be empty."
        }
    }
}
