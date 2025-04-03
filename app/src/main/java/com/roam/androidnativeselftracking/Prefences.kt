package com.roam.androidnativeselftracking

import android.content.Context
import android.content.SharedPreferences

object Preferences {
    private const val sharedPrefer = "roam"

    private fun getInstance(context: Context): SharedPreferences {
        return context.getSharedPreferences(sharedPrefer, Context.MODE_PRIVATE)
    }

    private fun setBoolean(context: Context, key: String, value: Boolean) {
        val editor = getInstance(context).edit()
        editor.putBoolean(key, value)
        editor.apply()
        editor.commit()
    }

    private fun getBoolean(context: Context?, key: String): Boolean {
        return getInstance(context!!).getBoolean(key, false)
    }

    private fun setInt(context: Context, key: String, value: Int) {
        val editor = getInstance(context).edit()
        editor.putInt(key.uppercase(), value)
        editor.apply()
        editor.commit()
    }

    private fun getInt(context: Context, key: String): Int {
        return getInstance(context).getInt(key.uppercase(), 0)
    }

    private fun setString(context: Context, key: String, value: String) {
        val editor = getInstance(context).edit()
        editor.putString(key.uppercase(), value)
        editor.apply()
        editor.commit()
    }

    private fun getString(context: Context?, key: String): String {
        return getInstance(context!!).getString(key.uppercase(), "") ?: ""
    }

    fun removeItem(context: Context, key: String) {
        val editor = getInstance(context).edit()
        editor.remove(key)
        editor.apply()
        editor.commit()
    }

    fun setUserId(context: Context, value: String) {
        setString(context, Logger.USER, value)
    }

    fun getUserId(context: Context?): String {
        return getString(context, Logger.USER)
    }

    fun setMockLocation(context: Context, value: Boolean) {
        setBoolean(context, Logger.MOCK, value)
    }

    fun getMockLocation(context: Context?): Boolean {
        return getBoolean(context, Logger.MOCK)
    }


    //example of a Logger object.
    object Logger {
        const val USER = "USER"
        const val SELF_LOGIN = "SELF_LOGIN"
        const val MOCK = "MOCK"
    }

}