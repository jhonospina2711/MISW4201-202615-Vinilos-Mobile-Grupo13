package com.example.vinilosmobile.domain.repository

import com.example.vinilosmobile.domain.models.CollectorDetail
import com.example.vinilosmobile.domain.models.CollectorSummary
import com.example.vinilosmobile.domain.models.DataResult
import kotlinx.coroutines.flow.Flow

interface CollectorRepository {
    fun getCollectors(): Flow<DataResult<List<CollectorSummary>>>
    fun getCollector(id: Long): Flow<DataResult<CollectorDetail>>
}
