package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.repository.CollectorRepository
import javax.inject.Inject

class GetCollectorUseCase @Inject constructor(private val repository: CollectorRepository) {
    operator fun invoke(id: Long) = repository.getCollector(id)
}
