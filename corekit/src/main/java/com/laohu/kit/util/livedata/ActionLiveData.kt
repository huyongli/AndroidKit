package com.laohu.kit.util.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

open class ActionLiveData<T> : MutableLiveData<T> {
    constructor() : super()
    constructor(value: T) : super(value)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer { data: T ->
            data?.let {
                observer.onChanged(it)
                value = null
            }
        })
    }

    /**
     * Simple method with naming that makes sense. It only sets the data of the live data.
     *
     * @param data The data to be sent.
     */
    fun sendAction(data: T) {
        value = data
    }
}