package com.example.vinilosmobile.data.network.interceptors

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class RequestHeadersInterceptorTest {
    @Test fun addsJsonAcceptHeaderAndProceedsOnce() {
        val chain = mockk<Interceptor.Chain>()
        val response = mockk<Response>()
        val forwarded = slot<Request>()
        every { chain.request() } returns Request.Builder().url("https://example.org/albums").build()
        every { chain.proceed(capture(forwarded)) } returns response

        val result = RequestHeadersInterceptor().intercept(chain)

        assertSame(response, result)
        assertEquals("application/json", forwarded.captured.header("Accept"))
        verify(exactly = 1) { chain.proceed(any()) }
    }
}
