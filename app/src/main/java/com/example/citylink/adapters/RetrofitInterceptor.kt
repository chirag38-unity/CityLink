package com.example.citylink.adapters

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import com.example.citylink.BuildConfig.serverApiKey

class RetrofitInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        request.addHeader("X-Api-Key", serverApiKey)

        return chain.proceed(request.build())
    }
}