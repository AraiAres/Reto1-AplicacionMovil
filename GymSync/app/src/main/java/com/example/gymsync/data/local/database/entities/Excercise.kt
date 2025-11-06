package com.example.gymsync.data.local.database.entities

import com.google.firebase.firestore.DocumentReference

data class Excercise(
    var excerciseDescription: String = "",
    var excerciseName: String = "",
    var workoutRef: DocumentReference? = null,
    var breakTime: String = "",
    var nameWorkout: String = "",
    var series: List<Series> = emptyList()
)