package com.laohu.kit.extensions

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "StartForResultFragment"
private val REQUEST_CODE = AtomicInteger(100)
private fun getRequestCode() = REQUEST_CODE.incrementAndGet()

fun FragmentActivity.requestActivityForResult(intent: Intent, callback: (isResultOk: Boolean, data: Intent?) -> Unit) {
    val fragmentManager = this.supportFragmentManager
    var startForResultFragment = fragmentManager.findFragmentByTag(TAG) as StartForResultFragment?
    if (startForResultFragment == null) {
        startForResultFragment = StartForResultFragment()
        fragmentManager.beginTransaction()
            .add(startForResultFragment, TAG)
            .commitNow()
    }
    startForResultFragment.requestActivityForResult(intent, callback)
}

fun Fragment.requestActivityForResult(intent: Intent, callback: (isResultOk: Boolean, data: Intent?) -> Unit) {
    val fragmentManager = this.childFragmentManager
    var startForResultFragment = fragmentManager.findFragmentByTag(TAG) as StartForResultFragment?
    if (startForResultFragment == null) {
        startForResultFragment = StartForResultFragment()
        fragmentManager.beginTransaction()
            .add(startForResultFragment, TAG)
            .commitNow()
    }

    startForResultFragment.requestActivityForResult(intent, callback)
}

class StartForResultFragment : Fragment() {
    private val requestsList: SparseArray<(Boolean, Intent?) -> Unit> = SparseArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun requestActivityForResult(intent: Intent, callback: ((Boolean, Intent?) -> Unit)) {
        val requestCode = getRequestCode()
        requestsList.append(requestCode, callback)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val listener = requestsList[requestCode]
        requestsList.remove(requestCode)
        listener?.invoke(resultCode == Activity.RESULT_OK, data)
    }
}

fun Activity.requestActivityForResult(intent: Intent, callback: (isResultOk: Boolean, data: Intent?) -> Unit) {
    val fragmentManager = this.fragmentManager
    var startForResultFragment = fragmentManager.findFragmentByTag(TAG) as StartForResultOriginFragment?
    if (startForResultFragment == null) {
        startForResultFragment = StartForResultOriginFragment()
        fragmentManager.beginTransaction()
            .add(startForResultFragment, TAG).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    commitNow()
                } else {
                    commit()
                }
            }
    }
    startForResultFragment.requestActivityForResult(intent, callback)
}

class StartForResultOriginFragment : android.app.Fragment() {
    private val requestsList: SparseArray<(Boolean, Intent?) -> Unit> = SparseArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun requestActivityForResult(intent: Intent, callback: ((Boolean, Intent?) -> Unit)) {
        val requestCode = getRequestCode()
        requestsList.append(requestCode, callback)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val listener = requestsList[requestCode]
        requestsList.remove(requestCode)
        listener?.invoke(resultCode == Activity.RESULT_OK, data)
    }
}