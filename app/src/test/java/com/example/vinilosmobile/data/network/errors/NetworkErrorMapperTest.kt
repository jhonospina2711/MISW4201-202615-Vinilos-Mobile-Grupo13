package com.example.vinilosmobile.data.network.errors

import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.models.RequestFailureKind
import io.mockk.every
import io.mockk.mockk
import java.net.ConnectException
import java.net.SocketTimeoutException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import retrofit2.HttpException

class NetworkErrorMapperTest {
    private val mapper = NetworkErrorMapper()

    @Test fun mapsConnectionAndTimeoutSeparately() {
        assertEquals(RequestFailureKind.NO_CONNECTION, mapper.map(ConnectException()).kind)
        assertEquals(RequestFailureKind.TIMEOUT, mapper.map(SocketTimeoutException()).kind)
    }

    @Test fun mapsUnauthorizedAndServerResponses() {
        val unauthorized = mockk<HttpException>()
        val server = mockk<HttpException>()
        every { unauthorized.code() } returns 401
        every { server.code() } returns 503

        assertEquals(RequestFailureKind.UNAUTHORIZED, mapper.map(unauthorized).kind)
        assertEquals(RequestFailureKind.SERVER, mapper.map(server).kind)
    }

    @Test fun keepsAlreadyMappedFailure() {
        val cause = mockk<Throwable>()
        val failure = RequestFailure(RequestFailureKind.UNKNOWN, cause)

        assertSame(failure, mapper.map(failure))
    }
}
