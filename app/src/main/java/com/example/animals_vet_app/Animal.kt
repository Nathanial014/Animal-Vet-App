package com.example.animals_vet_app

data class Animal(
    var name: String,
    var type: String,
    var features: Features,
    var id: Long = 0L
)
