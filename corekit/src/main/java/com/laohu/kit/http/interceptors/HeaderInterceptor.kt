package com.laohu.kit.http.interceptors

import android.os.Build
import okhttp3.Interceptor
import okhttp3.Response

const val PLATFORM_VALUE = "Android"

const val HEADER_KEY_TOKEN = "Authorization"
const val HEADER_KEY_PLATFORM = "Platform"
const val HEADER_KEY_DEVICE_MODEL = "DeviceModel"
const val HEADER_KEY_DEVICE_OS_VERSION = "OSVersion"

const val HEADER_KEY_USER_AGENT = "User-Agent"
const val USER_AGENT_PROPERTY = "http.agent"
val USER_AGENT_VALUE: String
    get() = System.getProperty(USER_AGENT_PROPERTY) ?: ""

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader(HEADER_KEY_PLATFORM, PLATFORM_VALUE)
            .header(HEADER_KEY_USER_AGENT, USER_AGENT_VALUE)
            .header(HEADER_KEY_DEVICE_MODEL, Build.MODEL)
            .header(HEADER_KEY_DEVICE_OS_VERSION, Build.VERSION.SDK_INT.toString())
            .build()
        return chain.proceed(request)
    }
}