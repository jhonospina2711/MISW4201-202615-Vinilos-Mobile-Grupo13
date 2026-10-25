package com.example.vinilosmobile.domain.models

enum class UserRole(val storedValue: String) {
    VISITOR("visitor"),
    COLLECTOR("collector");

    companion object {
        fun fromStoredValue(value: String?): UserRole? = entries.firstOrNull {
            it.storedValue == value
        }
    }
}
