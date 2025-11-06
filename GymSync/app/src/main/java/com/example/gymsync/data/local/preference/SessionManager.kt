package com.example.gymsync.data.local.preference

import android.content.Context
import android.content.SharedPreferences
import com.example.gymsync.data.local.database.entities.User
import com.google.gson.Gson
import androidx.core.content.edit

object SessionManager {

    private const val PREF_NAME = "user_session"
    private const val KEY_USER = "current_user"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(user: User) {
        val json = gson.toJson(user)
        prefs.edit { putString(KEY_USER, json) }
    }

    fun getUser(): User? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return gson.fromJson(json, User::class.java)
    }

    fun isLoggedIn(): Boolean = getUser() != null

    fun clearSession() {
        prefs.edit { clear() }
    }
}