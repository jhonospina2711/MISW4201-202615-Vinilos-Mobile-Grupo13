package com.example.vinilosmobile.coreui.models

data class DetailRowUiModel(
    val id: Long,
    val leading: String? = null,
    val title: String,
    val subtitle: String? = null,
    val trailing: String? = null,
)
