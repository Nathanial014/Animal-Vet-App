package com.example.animals_vet_app

object AppData {
    val animals = AnimalRepository()

    init {
        seedAnimals()
    }

    private fun seedAnimals() {
        val samples = listOf(
            Triple("Lion", "Feline", Features(
                "Large social big cat that lives in prides across African savannas.",
                "10-14 years", "Meat (zebra, wildebeest, buffalo)")),
            Triple("Tiger", "Feline", Features(
                "Largest wild cat, a solitary striped hunter found across Asia.",
                "10-15 years", "Meat (deer, wild boar)")),
            Triple("Cheetah", "Feline", Features(
                "Fastest land animal, able to sprint at over 100 km/h.",
                "10-12 years", "Meat (gazelles, impala)")),
            Triple("Domestic Cat", "Feline", Features(
                "Popular small pet known for independence and agility.",
                "12-18 years", "Meat-based cat food")),
            Triple("Elephant", "Mammal", Features(
                "Largest land mammal with a long trunk and strong family bonds.",
                "60-70 years", "Grasses, leaves, bark and fruit")),
            Triple("Dog", "Mammal", Features(
                "Loyal domesticated companion bred for many roles.",
                "10-13 years", "Meat, grains and vegetables")),
            Triple("Giraffe", "Mammal", Features(
                "Tallest animal on Earth, using its long neck to reach treetops.",
                "20-25 years", "Acacia leaves and shoots")),
            Triple("Horse", "Mammal", Features(
                "Strong, fast herd animal kept for riding and work.",
                "25-30 years", "Grass, hay and grains")),
            Triple("Dolphin", "Aquatic", Features(
                "Intelligent marine mammal that communicates with clicks and whistles.",
                "20-45 years", "Fish and squid")),
            Triple("Great White Shark", "Aquatic", Features(
                "Powerful apex predator of coastal and offshore ocean waters.",
                "30-70 years", "Seals, fish and other marine animals")),
            Triple("Blue Whale", "Aquatic", Features(
                "Largest animal ever known, filter feeding in cold and temperate seas.",
                "80-90 years", "Krill")),
            Triple("Goldfish", "Aquatic", Features(
                "Hardy freshwater fish commonly kept in ponds and aquariums.",
                "10-15 years", "Flakes, pellets, algae and small insects"))
        )

        samples.forEach { (name, type, features) ->
            animals.create(Animal(name = name, type = type, features = features))
        }
    }
}
