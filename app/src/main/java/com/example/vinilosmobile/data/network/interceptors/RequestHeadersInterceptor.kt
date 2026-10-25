package com.example.vinilosmobile.data.network.interceptors

import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

/** Common headers for BackVynils requests. */
class RequestHeadersInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
