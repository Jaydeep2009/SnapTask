package com.jaydeep.kaamly.data.model

/**
 * Data class representing a location
 */
data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val city: String = "",
    val address: String? = null
)
