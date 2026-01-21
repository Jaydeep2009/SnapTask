package com.jaydeep.kaamly.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.jaydeep.kaamly.data.model.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Implementation of LocationRepository using Google Play Services
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationRepository {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    companion object {
        private const val EARTH_RADIUS_KM = 6371.0
    }

    override suspend fun getCurrentLocation(): BaseRepository.Result<Location> {
        return try {
            // Check for location permission
            if (!hasLocationPermission()) {
                return BaseRepository.Result.Error(
                    SecurityException("Location permission not granted")
                )
            }

            // Get current location
            val cancellationTokenSource = CancellationTokenSource()
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (location != null) {
                // Get city name from coordinates
                val city = getCityFromCoordinates(location.latitude, location.longitude)
                
                BaseRepository.Result.Success(
                    Location(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        city = city ?: "Unknown",
                        address = null
                    )
                )
            } else {
                BaseRepository.Result.Error(
                    Exception("Unable to get current location")
                )
            }
        } catch (e: SecurityException) {
            BaseRepository.Result.Error(e)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override suspend fun searchLocation(query: String): BaseRepository.Result<List<LocationResult>> {
        return try {
            if (query.isBlank()) {
                return BaseRepository.Result.Success(emptyList())
            }

            val results = mutableListOf<LocationResult>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Use new API for Android 13+
                geocoder.getFromLocationName(query, 5) { addresses ->
                    addresses.forEach { address ->
                        results.add(
                            LocationResult(
                                name = address.featureName ?: address.locality ?: query,
                                city = address.locality ?: address.subAdminArea ?: "",
                                address = address.getAddressLine(0),
                                latitude = address.latitude,
                                longitude = address.longitude
                            )
                        )
                    }
                }
            } else {
                // Use deprecated API for older versions
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 5)
                addresses?.forEach { address ->
                    results.add(
                        LocationResult(
                            name = address.featureName ?: address.locality ?: query,
                            city = address.locality ?: address.subAdminArea ?: "",
                            address = address.getAddressLine(0),
                            latitude = address.latitude,
                            longitude = address.longitude
                        )
                    )
                }
            }

            BaseRepository.Result.Success(results)
        } catch (e: Exception) {
            BaseRepository.Result.Error(e)
        }
    }

    override fun getDistanceBetween(loc1: Location, loc2: Location): Double {
        val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
        val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(loc1.latitude)) * cos(Math.toRadians(loc2.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return EARTH_RADIUS_KM * c
    }

    /**
     * Check if location permission is granted
     */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get city name from coordinates using reverse geocoding
     */
    private suspend fun getCityFromCoordinates(latitude: Double, longitude: Double): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var cityName: String? = null
                geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                    cityName = addresses.firstOrNull()?.locality
                        ?: addresses.firstOrNull()?.subAdminArea
                }
                cityName
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.locality
                    ?: addresses?.firstOrNull()?.subAdminArea
            }
        } catch (e: Exception) {
            null
        }
    }
}
