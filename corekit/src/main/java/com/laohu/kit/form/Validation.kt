package com.laohu.kit.form

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Failure(val message: Int?) : ValidationResult()
}

fun ValidationResult?.isSuccess() = this is ValidationResult.Success

fun ValidationResult?.message() = if (this is ValidationResult.Failure) this.message else null

interface Validation {
    val message: String
    fun validate(input: String): ValidationResult
}