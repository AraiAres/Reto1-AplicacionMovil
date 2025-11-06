package com.example.gymsync.data.local.database.entities

data class Workout(
    var excerciseCount: String = "",
    var level: String = "",
    var name: String = "",
    var videourl: String = "",
    var excercises: List<Excercise> = emptyList()

)