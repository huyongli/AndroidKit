package com.laohu.kit.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.laohu.kit.extensions.asTo
import java.util.regex.Pattern

class TextFieldModel(
    private val emptyError: Int? = null,
    private val regex: String? = null,
    private val error: Int? = null,
    private val validateFun: ((String?) -> Boolean)? = null
) {
    private val _field = MutableLiveData<String>()
    var field: LiveData<String> = _field
    private val _validate = MutableLiveData<ValidationResult>()
    val validate: LiveData<ValidationResult> = _validate
    val isValidateSuccess: Boolean
        get() = validate.value.isSuccess()
    val validateErrorMessage: Int?
        get() = validate.value?.asTo<ValidationResult.Failure>()?.message

    init {
        runValidate()
    }

    fun bindAndValidate(value: String?) {
        this._field.value = value
        runValidate()
    }

    private fun runValidate() {
        val result = if (emptyError != null && field.value.isNullOrEmpty()) {
            ValidationResult.Failure(emptyError)
        } else {
            when {
                regex == null && validateFun == null -> ValidationResult.Success
                regex != null -> {
                    if (regexValidate(regex)) {
                        ValidationResult.Success
                    } else {
                        ValidationResult.Failure(error)
                    }
                }
                validateFun != null -> {
                    if (validateFun.invoke(field.value)) {
                        ValidationResult.Success
                    } else {
                        ValidationResult.Failure(error)
                    }
                }
                else -> ValidationResult.Failure(error)
            }
        }
        _validate.value = result
    }

    fun callValidateFun(): Boolean {
        if (field.value.isNullOrEmpty() || validateFun == null) {
            return false
        }
        _validate.value = if (validateFun.invoke(field.value)) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(error)
        }
        return true
    }

    fun regexValidate(regex: String): Boolean {
        return Pattern.compile(regex).matcher(field.value.orEmpty()).matches()
    }
}