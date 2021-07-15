package com.laohu.kit.base.fragment

import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.laohu.kit.extensions.getColorCompat
import com.laohu.kit.extensions.resetStatusBar
import com.laohu.kit.extensions.updateStatusBarColor

interface IBackPressView {
    fun onBackPressed(): Boolean
}

open class CoreFragment : Fragment(), IBackPressView {

    override fun onBackPressed(): Boolean {
        return false
    }

    fun <T> LiveData<T>.observe(block: (T) -> Unit) {
        this.observe(viewLifecycleOwner, {
            block.invoke(it)
        })
    }

    fun <T> LiveData<T>.observeNonNull(block: (T) -> Unit) {
        observe(viewLifecycleOwner, { v -> v?.let { block(it) } })
    }

    fun updateStatusBarColor(@ColorRes id: Int) {
        val activity = this.activity ?: return
        activity.resetStatusBar()
        activity.updateStatusBarColor(activity.getColorCompat(id))
    }
}