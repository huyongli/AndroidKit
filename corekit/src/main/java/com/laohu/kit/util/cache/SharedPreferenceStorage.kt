package com.laohu.kit.util.cache

import android.content.Context
import android.content.SharedPreferences

private const val SHARED_PREFERENCES_NAME = "AppSharedPreference"

class SharedPreferencesStorage @JvmOverloads constructor(
    context: Context,
    sharedPreferencesName: String = SHARED_PREFERENCES_NAME
) : Storage {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        sharedPreferencesName, Context.MODE_PRIVATE)

    override fun store(name: String, value: Long?) {
        if (value == null) {
            sharedPreferences.edit().remove(name).apply()
        } else {
            sharedPreferences.edit().putLong(name, value).apply()
        }
    }

    override fun store(name: String, value: Int?) {
        if (value == null) {
            sharedPreferences.edit().remove(name).apply()
        } else {
            sharedPreferences.edit().putInt(name, value).apply()
        }
    }

    override fun store(name: String, value: String?) {
        if (value == null) {
            sharedPreferences.edit().remove(name).apply()
        } else {
            sharedPreferences.edit().putString(name, value).apply()
        }
    }

    override fun store(name: String, value: Boolean?) {
        if (value == null) {
            sharedPreferences.edit().remove(name).apply()
        } else {
            sharedPreferences.edit().putBoolean(name, value).apply()
        }
    }

    override fun retrieveLong(name: String): Long? {
        return if (!sharedPreferences.contains(name)) {
            null
        } else return try {
            sharedPreferences.getLong(name, 0)
        } catch (e: ClassCastException) {
            0
        }
    }

    override fun retrieveString(name: String): String? {
        return if (!sharedPreferences.contains(name)) {
            null
        } else return try {
            sharedPreferences.getString(name, null)
        } catch (e: ClassCastException) {
            null
        }
    }

    override fun retrieveInteger(name: String): Int? {
        return if (!sharedPreferences.contains(name)) {
            null
        } else return try {
            sharedPreferences.getInt(name, 0)
        } catch (e: ClassCastException) {
            0
        }
    }

    override fun retrieveBoolean(name: String): Boolean? {
        return if (!sharedPreferences.contains(name)) {
            null
        } else return try {
            sharedPreferences.getBoolean(name, false)
        } catch (e: ClassCastException) {
            false
        }
    }

    override fun remove(name: String) {
        sharedPreferences.edit().remove(name).apply()
    }
}