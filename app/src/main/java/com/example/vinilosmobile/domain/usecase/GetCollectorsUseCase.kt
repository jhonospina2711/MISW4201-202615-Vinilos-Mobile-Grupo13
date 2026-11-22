package com.example.vinilosmobile.domain.usecase

import com.example.vinilosmobile.domain.repository.CollectorRepository
import javax.inject.Inject

class GetCollectorsUseCase @Inject constructor(private val repository: CollectorRepository) {
    operator fun invoke() = repository.getCollectors()
}
