package com.laohu.kit.util.cache

import android.content.Context
import com.laohu.kit.extensions.asTo

class LocalCache(context: Context, sharedPreferenceName: String = "SharedPreferencesStorageFile") {
    val storage: SharedPreferencesStorage =
        SharedPreferencesStorage(context.applicationContext ?: context, sharedPreferenceName)

    inline fun <reified T> cache(key: String, value: T) {
        storageCache(storage, key, value)
    }

    fun removeCache(key: String) {
        storage.remove(key)
    }

    inline fun <reified T> storageCache(storage: SharedPreferencesStorage,
                                        key: String, value: T) {
        when (T::class) {
            Int::class -> storage.store(key, value as Int)
            String::class -> storage.store(key, value as String)
            Long::class -> storage.store(key, value as Long)
            Boolean::class -> storage.store(key, value as Boolean)
            else -> throw IllegalArgumentException("invalid class type")
        }
    }

    inline fun <reified T> get(key: String): T? {
        return when (T::class) {
            Int::class -> storage.retrieveInteger(key)?.asTo()
            String::class -> storage.retrieveString(key)?.asTo()
            Long::class -> storage.retrieveLong(key)?.asTo()
            Boolean::class -> storage.retrieveBoolean(key)?.asTo()
            else -> null
        }
    }
}