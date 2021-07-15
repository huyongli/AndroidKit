package com.laohu.kit.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.SparseArray
import androidx.annotation.Size
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "PermissionFragment"

private val REQUEST_CODE = AtomicInteger(100)
private fun getRequestCode() = REQUEST_CODE.incrementAndGet()

private fun checkGrantedPermission(context: Context, @Size(min = 1) vararg permissions: String): Boolean {
    // Always return true for SDK < M, let the system deal with the permissions
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        // DANGER ZONE!!! Changing this will break the library.
        return true
    }

    // Null context may be passed if we have detected Low API (less than M) so getting
    // to this point with a null context should not be possible.
    for (perm in permissions) {
        if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}

fun FragmentActivity.hasPermissions(vararg perms: String) = checkGrantedPermission(this, *perms)

fun FragmentActivity.requestPermission(
    vararg perms: String,
    rationale: String,
    onGranted: () -> Unit,
    onDenied: ((perms: List<String>) -> Unit)? = null
) {
    val fragmentManager = this.supportFragmentManager
    var permissionFragment = fragmentManager.findFragmentByTag(TAG) as PermissionFragment?
    if (permissionFragment == null) {
        permissionFragment = PermissionFragment()
        fragmentManager.beginTransaction()
            .add(permissionFragment, TAG)
            .commitNow()
    }

    val permissionRequest = PermissionRequest(perms)
        .apply {
            rationaleStr = rationale
            grantedCallback = onGranted
            deniedCallback = onDenied
        }

    permissionFragment.requestPermission(permissionRequest)
}

fun Fragment.hasPermissions(vararg perms: String) = checkGrantedPermission(this.activity!!, *perms)

fun Fragment.requestPermission(
    vararg perms: String,
    rationale: String,
    onGranted: () -> Unit,
    onDenied: ((perms: List<String>) -> Unit)? = null
) {
    val fragmentManager = this.childFragmentManager
    var permissionFragment = fragmentManager.findFragmentByTag(TAG) as PermissionFragment?
    if (permissionFragment == null) {
        permissionFragment = PermissionFragment()
        fragmentManager.beginTransaction()
            .add(permissionFragment, TAG)
            .commitNow()
    }

    val permissionRequest = PermissionRequest(perms)
        .apply {
            rationaleStr = rationale
            grantedCallback = onGranted
            deniedCallback = onDenied
        }

    permissionFragment.requestPermission(permissionRequest)
}

class PermissionFragment : Fragment() {
    private val permissionRequestMap = SparseArray<PermissionRequest>()
    private var requestCode: Int = REQUEST_CODE.get()

    fun requestPermission(permissionRequest: PermissionRequest) {
        val needRequestPerms = permissionRequest.perms.filter {
            !checkGrantedPermission(this.activity!!, it)
        }
        if (needRequestPerms.isEmpty()) {
            permissionRequest.grantedCallback?.invoke()
            return
        }
        requestCode = getRequestCode()
        permissionRequestMap.append(requestCode, permissionRequest)
        requestPermissions(needRequestPerms.toTypedArray(), requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // if request permission is called again before the user actions to the first request
        // onRequestPermissionsResult would be called immediately, and parameter permission is empty
        if (permissions.isEmpty()) {
            permissionRequestMap.remove(requestCode)
        } else {
            val permissionRequest = permissionRequestMap[requestCode]
            if (permissionRequest != null) {
                val deniedPermission = permissionRequest.perms.filter { requestPerm ->
                    for ((index, resultPerm) in permissions.withIndex()) {
                        if (resultPerm == requestPerm) {
                            return@filter grantResults[index] != PackageManager.PERMISSION_GRANTED
                        }
                    }
                    true
                }
                if (deniedPermission.isNullOrEmpty()) {
                    permissionRequest.grantedCallback?.invoke()
                } else {
                    permissionRequest.deniedCallback?.invoke(deniedPermission)
                }
                permissionRequestMap.remove(requestCode)
            }
        }
    }
}

class PermissionRequest(val perms: Array<out String>) {
    var rationaleStr: String = ""
    var grantedCallback: (() -> Unit)? = null
    var deniedCallback: ((perms: List<String>) -> Unit)? = null
}