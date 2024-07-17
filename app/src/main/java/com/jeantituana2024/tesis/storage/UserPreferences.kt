package com.jeantituana2024.tesis.storage

import android.content.Context
import android.content.SharedPreferences
import com.jeantituana2024.tesis.models.UserLogin

class UserPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Guardar datos del usuario
    fun saveUser(user: UserLogin) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER_ID, user.id)
        editor.putString(KEY_USER_NAME, user.name)
        editor.putString(KEY_USER_EMAIL, user.email)
        editor.putString(KEY_USER_ROLE, user.role)
        editor.putString(KEY_USER_IMAGE, user.image)
        editor.apply()
    }

    // Obtener datos del usuario
    fun getUser(): UserLogin? {
        val id = sharedPreferences.getString(KEY_USER_ID, null)
        val name = sharedPreferences.getString(KEY_USER_NAME, null)
        val email = sharedPreferences.getString(KEY_USER_EMAIL, null)
        val role = sharedPreferences.getString(KEY_USER_ROLE, null)
        val image = sharedPreferences.getString(KEY_USER_IMAGE, null)

        return if (id != null && name != null && email != null && role != null) {
            UserLogin(id, email, name, role, image)
        } else {
            null
        }
    }

    // Eliminar datos del usuario (por ejemplo, al cerrar sesión)
    fun clearUserData() {
        val editor = sharedPreferences.edit()
        editor.clear() // O usar editor.remove(key) para eliminar campos específicos
        editor.apply()
    }

    companion object {
        private const val PREFS_NAME = "UserPrefs"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "name"
        private const val KEY_USER_EMAIL = "email"
        private const val KEY_USER_ROLE = "role"
        private const val KEY_USER_IMAGE = "image"
    }
}