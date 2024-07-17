package com.jeantituana2024.tesis.storage

import android.content.Context
import android.content.SharedPreferences

class TokenPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("token_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_VERIFICATION_TOKEN = "token"
    }

    fun saveToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_VERIFICATION_TOKEN, token)
        editor.apply()
    }

    open fun getToken(): String? {
        return sharedPreferences.getString(KEY_VERIFICATION_TOKEN, null)
    }

    fun clearToken() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_VERIFICATION_TOKEN)
        editor.apply()
    }
}