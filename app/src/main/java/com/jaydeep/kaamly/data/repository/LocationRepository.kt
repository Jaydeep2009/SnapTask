package com.jaydeep.kaamly.data.repository

import com.jaydeep.kaamly.data.model.Location

/**
 * Repository interface for location operations
 */
interface LocationRepository {
    /**
     * Get the current location using GPS
     * @return Result containing the current location
     */
    suspend fun getCurrentLocation(): BaseRepository.Result<Location>

    /**
     * Search for locations by query
     * @param query The search query (city name, address, etc.)
     * @return Result containing list of location results
     */
    suspend fun searchLocation(query: String): BaseRepository.Result<List<LocationResult>>

    /**
     * Calculate distance between two locations
     * @param loc1 First location
     * @param loc2 Second location
     * @return Distance in kilometers
     */
    fun getDistanceBetween(loc1: Location, loc2: Location): Double
}

/**
 * Data class representing a location search result
 */
data class LocationResult(
    val name: String,
    val city: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double
)
