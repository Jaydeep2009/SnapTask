package com.jaydeep.kaamly.data.model

/**
 * Data class representing equipment needed for a task
 */
data class Equipment(
    val name: String = "",
    val providedBy: EquipmentProvider = EquipmentProvider.USER
)

/**
 * Enum representing who provides the equipment
 */
enum class EquipmentProvider {
    USER,
    WORKER
}
