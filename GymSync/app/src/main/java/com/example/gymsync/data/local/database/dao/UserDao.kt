package com.example.gymsync.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gymsync.data.local.database.entities.User

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: User)
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): User?
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}