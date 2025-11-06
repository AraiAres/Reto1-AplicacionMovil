package com.example.gymsync.data.local.database

import android.content.Context
import com.example.gymsync.data.local.database.entities.User
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gymsync.data.local.database.dao.UserDao

@Database(
    entities = [User::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    companion object {

        @Volatile
        private var instance : AppDatabase? = null

        private val LOCK = Any()

        operator fun invoke (context:Context) = instance?: synchronized(LOCK){
            instance?:buildDatabase (context).also { instance = it}
        }

        private fun buildDatabase (context: Context) = Room.databaseBuilder(context,
            AppDatabase::class.java,
            "myDataBase")
            .build()
    }
    abstract fun userDao(): UserDao
}