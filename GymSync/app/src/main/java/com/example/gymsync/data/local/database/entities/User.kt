package com.example.gymsync.data.local.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    var roomid: Int = 0,
    var name: String = "",
    var lastname: String = "",
    var password: String = "",
    var email: String = "",
    var birthdate: String = "",
    var level: String = "0",
    var trainer: Boolean = false,

    @Ignore
    var useridfirebase: String = ""
)
