package com.laohu.kit.http

import com.laohu.kit.http.interceptors.HeaderInterceptor
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.net.Proxy
import java.util.concurrent.TimeUnit

const val NeedAuthKey = "needAuth"
const val NeedAuthValue = "1"

const val AUTO_AUTH = "$NeedAuthKey: $NeedAuthValue"

private const val TIME_OUT = 60L

object OkHttpFactory {

    fun create(cache: Cache?, interceptors: List<Interceptor> = emptyList(), enabledProxy: Boolean = true): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(HeaderInterceptor())
            .apply { interceptors.forEach { addInterceptor(it) } }
            .apply {
                if (!enabledProxy) {
                    proxy(Proxy.NO_PROXY)
                }
            }

        return okHttpClientBuilder.build()
    }
}