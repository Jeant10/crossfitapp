package com.jeantituana2024.tesis.storage

import android.content.Context
import android.content.SharedPreferences

import com.jeantituana2024.tesis.models.User


class SharedPrefManager private constructor(mCtx: Context) {
    private val mCtx: Context

    init {
        this.mCtx = mCtx
    }

    fun saveUser(user: User) {
        val sharedPreferences: SharedPreferences =
            mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("id", user.id)
        editor.putString("email", user.email)
        editor.putString("name", user.name)
        editor.apply()
    }

    val isLoggedIn: Boolean
        get() {
            val sharedPreferences: SharedPreferences =
                mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getInt("id", -1) != -1
        }
    val user: User
        get() {
            val sharedPreferences: SharedPreferences =
                mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            return User(
                sharedPreferences.getInt("id", -1),
                sharedPreferences.getString("email", null)!!,
                sharedPreferences.getString("name", null)!!
            )
        }

    fun clear() {
        val sharedPreferences: SharedPreferences =
            mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        private const val SHARED_PREF_NAME = "my_shared_preff"
        private var mInstance: SharedPrefManager? = null
        @Synchronized
        fun getInstance(mCtx: Context): SharedPrefManager? {
            if (mInstance == null) {
                mInstance = SharedPrefManager(mCtx)
            }
            return mInstance
        }
    }
}