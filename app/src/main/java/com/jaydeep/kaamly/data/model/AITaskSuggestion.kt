package com.jaydeep.kaamly.data.model

/**
 * Data class representing AI-generated task suggestions
 */
data class AITaskSuggestion(
    val title: String = "",
    val description: String = "",
    val category: TaskCategory = TaskCategory.OTHER,
    val requiredEquipment: List<String> = emptyList(),
    val estimatedDuration: Int = 0, // in minutes
    val suggestedPriceRange: PriceRange = PriceRange()
)

/**
 * Data class representing a price range
 */
data class PriceRange(
    val min: Double = 0.0,
    val max: Double = 0.0
)
