package com.example.animals_vet_app

import java.util.concurrent.atomic.AtomicLong

class AnimalRepository {
    private val animals = ArrayList<Animal>()
    private val lastId = AtomicLong(0L)

    fun findAll(): List<Animal> {
        return animals
    }

    fun create(animal: Animal) {
        animal.id = lastId.incrementAndGet()
        animals.add(animal)
    }

    fun update(animal: Animal): Boolean {
        val foundAnimal = findOne(animal.id)
        return if (foundAnimal != null) {
            foundAnimal.name = animal.name
            foundAnimal.type = animal.type
            foundAnimal.features = animal.features
            true
        } else {
            false
        }
    }

    fun delete(id: Long): Boolean {
        val foundAnimal = findOne(id)
        return if (foundAnimal != null) {
            animals.remove(foundAnimal)
            true
        } else {
            false
        }
    }

    fun findOne(id: Long): Animal? {
        return animals.find { p -> p.id == id }
    }
}
