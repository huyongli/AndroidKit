package com.laohu.kit.extensions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <reified T : ViewModel> Fragment.getViewModel(noinline creator: (() -> T)? = null): T {
    return if (creator == null) {
        ViewModelProvider(this).get(T::class.java)
    } else {
        ViewModelProvider(this, SimpleViewModelFactory(creator)).get(T::class.java)
    }
}

inline fun <reified T : ViewModel> Fragment.getActivityViewModel(noinline creator: (() -> T)? = null): T {
    return activity!!.getViewModel(creator)
}

inline fun <reified T : ViewDataBinding> Fragment.dataBindingView(@LayoutRes layoutId: Int, parent: ViewGroup?): T {
    val inflater = LayoutInflater.from(this.context)
    val binding = DataBindingUtil.inflate<T>(inflater, layoutId, parent, false)
    binding.lifecycleOwner = viewLifecycleOwner
    return binding
}