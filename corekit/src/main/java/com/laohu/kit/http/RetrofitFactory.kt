package com.laohu.kit.http

import com.laohu.kit.extensions.gson
import com.laohu.kit.http.interceptors.OkHttpLoggingInterceptor
import okhttp3.Cache
import okhttp3.Interceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFactory {
    lateinit var retrofit: Retrofit

    @JvmStatic
    fun <IService> create(service: Class<IService>): IService {
        return retrofit.create(service)
    }

    @JvmStatic
    fun init(baseUrl: String, cache: Cache? = null, isProdRelease: Boolean = false, interceptors: List<Interceptor> = emptyList(), converterFactory: Converter.Factory? = null) {
        val interceptorList = mutableListOf<Interceptor>().apply {
            addAll(interceptors)
        }
        if (!isProdRelease) {
            interceptorList.add(OkHttpLoggingInterceptor())
        }
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpFactory.create(cache, interceptorList))
            .addConverterFactory(converterFactory ?: GsonConverterFactory.create(gson))
            .build()
    }
}