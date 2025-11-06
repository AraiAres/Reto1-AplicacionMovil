package com.example.gymsync.data.local.database.entities

import com.google.firebase.firestore.DocumentReference

data class Series (
    var serieRef: DocumentReference? = null,
    var estimatedDuration: String = "",
    var name: String = "",
    var repetitionCount: String = "",
    var completedDate: String = "",
    var nameExcercise: String = "",
    var excerciseRef: DocumentReference? = null,
    val seriesIcon: String
)
