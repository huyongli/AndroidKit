package com.laohu.kit.util.livedata


class ErrorActionLiveData<T> : ActionLiveData<ErrorAction<T>> {
    /**
     * constructor
     */
    constructor() : super()

    /**
     * constructor
     * @param value init value
     */
    constructor(value: ErrorAction<T>) : super(value)
}

data class ErrorAction<T> (val errorType: T, val errorMessage: String?)