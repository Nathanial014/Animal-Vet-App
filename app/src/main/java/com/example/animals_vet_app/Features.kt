package com.example.animals_vet_app

data class Features(
    var description: String = "",
    var lifespan: String = "",
    var dietPreferred: String = "",
    var typeImage: String = "",
    var imageUri: String? = null
) {
    fun validate() {
        require(description.isNotBlank()) {
            "Description must not be empty."
        }

        require(lifespan.isNotBlank()) {
            "Lifespan must not be empty."
        }
    }
}
