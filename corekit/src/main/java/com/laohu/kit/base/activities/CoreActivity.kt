package com.laohu.kit.base.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData

open class CoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate(savedInstanceState)
    }

    fun <T> LiveData<T>.observeNonNull(block: (T) -> Unit) {
        observe(this@CoreActivity, { v -> v?.let { block(it) } })
    }
}