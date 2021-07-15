package com.laohu.kit.util.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.laohu.kit.form.ValidationResult
import com.laohu.kit.form.isSuccess

class FormValidateLiveData(liveDataList: List<LiveData<ValidationResult>>) : MediatorLiveData<ValidationResult>() {
    private val resultList = mutableMapOf<Int, ValidationResult>()

    init {
        liveDataList.forEachIndexed { index, liveData ->
            resultList[index] = liveData.value ?: ValidationResult.Failure(null)
            addSource(liveData) {
                resultList[index] = it
                updateValue()
            }
        }
        updateValue()
        this.onActive()
    }

    private fun updateValue() {
        val falseItem = resultList.values.find { !it.isSuccess() }
        value = falseItem ?: ValidationResult.Success
    }
}