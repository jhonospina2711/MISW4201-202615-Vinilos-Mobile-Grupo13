package com.example.vinilosmobile.data.repository

import com.example.vinilosmobile.data.mappers.toDetail
import com.example.vinilosmobile.data.mappers.toSummary
import com.example.vinilosmobile.data.network.errors.NetworkErrorMapper
import com.example.vinilosmobile.data.network.services.CollectorApiService
import com.example.vinilosmobile.di.IoDispatcher
import com.example.vinilosmobile.domain.models.CollectorDetail
import com.example.vinilosmobile.domain.models.CollectorSummary
import com.example.vinilosmobile.domain.models.DataError
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.repository.CollectorRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import retrofit2.HttpException

class CollectorRepositoryImpl @Inject constructor(
    private val service: CollectorApiService,
    private val errorMapper: NetworkErrorMapper,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CollectorRepository {
    override fun getCollectors(): Flow<DataResult<List<CollectorSummary>>> = flow {
        emit(DataResult.Loading)
        try {
            emit(DataResult.Success(service.getCollectors().mapNotNull { it.toSummary() }))
        } catch (exception: CancellationException) {
            if (!currentCoroutineContext().isActive) throw exception
            emit(DataResult.Cancelled)
        } catch (exception: Exception) {
            emit(DataResult.Error(DataError.Request(errorMapper.map(exception).kind)))
        }
    }.flowOn(ioDispatcher)

    override fun getCollector(id: Long): Flow<DataResult<CollectorDetail>> = flow {
        emit(DataResult.Loading)
        try {
            val collector = service.getCollector(id)
            // BackVynils does not include the album relation in GET /collectors/{id}.
            val albums = try {
                service.getCollectorAlbums(id)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                null
            }
            val detail = collector.toDetail(albums)
            if (detail == null) emit(DataResult.Error(DataError.NotFound))
            else emit(DataResult.Success(detail))
        } catch (exception: CancellationException) {
            if (!currentCoroutineContext().isActive) throw exception
            emit(DataResult.Cancelled)
        } catch (exception: HttpException) {
            val error = if (exception.code() == 404) DataError.NotFound
            else DataError.Request(errorMapper.map(exception).kind)
            emit(DataResult.Error(error))
        } catch (exception: Exception) {
            emit(DataResult.Error(DataError.Request(errorMapper.map(exception).kind)))
        }
    }.flowOn(ioDispatcher)
}
