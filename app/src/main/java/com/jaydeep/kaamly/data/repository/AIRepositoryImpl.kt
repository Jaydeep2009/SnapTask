package com.jaydeep.kaamly.data.repository

import com.jaydeep.kaamly.data.model.AITaskSuggestion
import com.jaydeep.kaamly.data.model.PriceRange
import com.jaydeep.kaamly.data.model.TaskCategory
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mock implementation of AIRepository for task generation
 * Uses template-based generation for hackathon demo
 */
@Singleton
class AIRepositoryImpl @Inject constructor() : AIRepository {

    private val templates = mapOf(
        "clean" to AITaskSuggestion(
            title = "Professional Cleaning Service",
            description = "Need thorough cleaning of my home/office space. This includes dusting, mopping, vacuuming, and sanitizing all areas. Please bring your own cleaning supplies.",
            category = TaskCategory.CLEANING,
            requiredEquipment = listOf("Vacuum cleaner", "Mop", "Cleaning supplies", "Sanitizer"),
            estimatedDuration = 120,
            suggestedPriceRange = PriceRange(500.0, 1500.0)
        ),
        "repair" to AITaskSuggestion(
            title = "Home Repair Service",
            description = "Need repair work done at my property. This may include fixing broken items, minor maintenance, or general handyman services. Please assess the work and provide an estimate.",
            category = TaskCategory.REPAIR,
            requiredEquipment = listOf("Basic tools", "Screwdriver set", "Hammer", "Measuring tape"),
            estimatedDuration = 90,
            suggestedPriceRange = PriceRange(300.0, 1000.0)
        ),
        "delivery" to AITaskSuggestion(
            title = "Delivery Service",
            description = "Need items delivered from one location to another within the city. Items are properly packed and ready for transport. Please handle with care.",
            category = TaskCategory.DELIVERY,
            requiredEquipment = listOf("Vehicle", "Rope/straps for securing items"),
            estimatedDuration = 60,
            suggestedPriceRange = PriceRange(200.0, 800.0)
        ),
        "plumb" to AITaskSuggestion(
            title = "Plumbing Service Required",
            description = "Need plumbing work done including fixing leaks, unclogging drains, or installing fixtures. Professional plumber with experience preferred.",
            category = TaskCategory.PLUMBING,
            requiredEquipment = listOf("Plumbing tools", "Pipe wrench", "Plunger", "Sealant"),
            estimatedDuration = 90,
            suggestedPriceRange = PriceRange(400.0, 1500.0)
        ),
        "electric" to AITaskSuggestion(
            title = "Electrical Work Required",
            description = "Need electrical work including wiring, fixture installation, or troubleshooting electrical issues. Licensed electrician preferred for safety.",
            category = TaskCategory.ELECTRICAL,
            requiredEquipment = listOf("Electrical tools", "Multimeter", "Wire stripper", "Insulation tape"),
            estimatedDuration = 120,
            suggestedPriceRange = PriceRange(500.0, 2000.0)
        ),
        "paint" to AITaskSuggestion(
            title = "Painting Service",
            description = "Need professional painting service for interior/exterior walls. Surface preparation, priming, and two coats of paint required. Please bring all painting supplies.",
            category = TaskCategory.PAINTING,
            requiredEquipment = listOf("Paint brushes", "Rollers", "Paint trays", "Drop cloths", "Ladder"),
            estimatedDuration = 240,
            suggestedPriceRange = PriceRange(1000.0, 3000.0)
        ),
        "garden" to AITaskSuggestion(
            title = "Gardening Service",
            description = "Need gardening work including lawn mowing, trimming, weeding, and general garden maintenance. Please bring your own gardening tools.",
            category = TaskCategory.GARDENING,
            requiredEquipment = listOf("Lawn mower", "Trimmer", "Gardening gloves", "Rake", "Pruning shears"),
            estimatedDuration = 120,
            suggestedPriceRange = PriceRange(400.0, 1200.0)
        ),
        "move" to AITaskSuggestion(
            title = "Moving Service",
            description = "Need help moving furniture and household items from one location to another. Items need to be carefully packed and transported. Vehicle required.",
            category = TaskCategory.MOVING,
            requiredEquipment = listOf("Moving truck/vehicle", "Dolly", "Moving blankets", "Straps"),
            estimatedDuration = 180,
            suggestedPriceRange = PriceRange(800.0, 2500.0)
        ),
        "assemble" to AITaskSuggestion(
            title = "Furniture Assembly",
            description = "Need help assembling furniture items. All parts and instructions are included. Please bring basic tools for assembly work.",
            category = TaskCategory.ASSEMBLY,
            requiredEquipment = listOf("Screwdriver set", "Allen keys", "Hammer", "Level"),
            estimatedDuration = 90,
            suggestedPriceRange = PriceRange(300.0, 1000.0)
        ),
        "install" to AITaskSuggestion(
            title = "Installation Service",
            description = "Need professional installation service for appliances, fixtures, or equipment. Please ensure proper installation according to manufacturer guidelines.",
            category = TaskCategory.INSTALLATION,
            requiredEquipment = listOf("Installation tools", "Drill", "Level", "Measuring tape"),
            estimatedDuration = 120,
            suggestedPriceRange = PriceRange(500.0, 1500.0)
        )
    )

    override suspend fun generateTaskDetails(briefDescription: String): BaseRepository.Result<AITaskSuggestion> {
        return try {
            // Simulate API call delay
            delay(1500)
            
            // Find matching template based on keywords
            val lowerDescription = briefDescription.lowercase()
            val matchedTemplate = templates.entries.firstOrNull { (keyword, _) ->
                lowerDescription.contains(keyword)
            }?.value
            
            // If no match found, generate a generic task
            val suggestion = matchedTemplate ?: generateGenericTask(briefDescription)
            
            BaseRepository.Result.Success(suggestion)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    /**
     * Generate a generic task when no template matches
     */
    private fun generateGenericTask(description: String): AITaskSuggestion {
        return AITaskSuggestion(
            title = "Task: ${description.take(50)}${if (description.length > 50) "..." else ""}",
            description = "Task description: $description\n\nPlease provide details about the work required, timeline, and any specific requirements.",
            category = TaskCategory.OTHER,
            requiredEquipment = listOf("Basic tools"),
            estimatedDuration = 60,
            suggestedPriceRange = PriceRange(200.0, 1000.0)
        )
    }
}
