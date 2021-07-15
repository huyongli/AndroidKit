package com.laohu.kit.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations

/**
 * Convenience function for using [Transformations.switchMap]
 * ```
 * // Before
 * Transformations.switchMap(source) { }
 * // After
 * source.switchMap { }
 * ```
 */
fun <X, Y> LiveData<X>.switchMap(func: (X) -> LiveData<Y>): LiveData<Y> {
    return Transformations.switchMap(this, func)
}

/**
 * Convenience function for using [Transformations.map]
 * ```
 * // Before
 * Transformations.map(source) {  }
 * // After
 * source.map { }
 * ```
 */
fun <X, Y> LiveData<X>.map(function: (X) -> Y): LiveData<Y> {
    return Transformations.map(this, function)
}

/**
 * Convenience function for adding an observer and not having to unwrap the optional, reducing:
 * ```
 * // Before
 * liveData.observe(this, Observer : {})
 * // After
 * liveData.observe(this) {}
 * ```
 */
fun <T> LiveData<T>.observe(owner: LifecycleOwner, observer: (T?) -> Unit) {
    observe(owner, Observer<T> { v -> observer(v) })
}

/**
 * Convenience function for adding an observer and not having to unwrap the optional, reducing:
 * ```
 * // Before
 * liveData.observe(this, Observer : { it?.doStuff() })
 * // After
 * liveData.observe(this) { it.doStuff() }
 * ```
 */
fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(owner, Observer<T> { v -> v?.let { observer(it) } })
}