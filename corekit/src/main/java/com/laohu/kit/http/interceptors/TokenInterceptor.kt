package com.laohu.kit.http.interceptors

import com.laohu.kit.http.NeedAuthKey
import com.laohu.kit.http.NeedAuthValue
import okhttp3.Interceptor
import okhttp3.Response

abstract class TokenInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        val isNeedToken = originRequest.header(NeedAuthKey) == NeedAuthValue
        var requestToken = ""
        val newRequest =
            if (isNeedToken) {
                requestToken = getToken()
                originRequest.newBuilder()
                    .removeHeader(NeedAuthKey)
                    .addHeader(HEADER_KEY_TOKEN, requestToken)
                    .build()
            } else originRequest

        val response = chain.proceed(newRequest)
        if (isNeedToken) {
            handleTokenExpired(requestToken, response)
        }
        return response
    }

    abstract fun getToken(): String

    abstract fun handleTokenExpired(requestToken: String, response: Response)
}