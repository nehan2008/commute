package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.example.data.ChennaiTransitRepository
import com.example.model.NearestTransitPoint
import com.example.model.Station
import com.example.model.TransitMode
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.*

data class UserLocationResult(
    val localityName: String,
    val localityNameTa: String,
    val latitude: Double,
    val longitude: Double,
    val nearestStation: Station?,
    val nearestBoardingPoint: NearestTransitPoint,
    val isGpsAccurate: Boolean
)

object ChennaiLocationService {

    private const val TAG = "ChennaiLocationService"

    // Reference Chennai Hubs with Coordinates (lat, lng)
    private val CHENNAI_HUBS = listOf(
        Triple("ecr_neelankarai", 12.9495, 80.2583),
        Triple("ecr_thiruvanmiyur", 12.9830, 80.2594),
        Triple("ecr_kottivakkam", 12.9660, 80.2580),
        Triple("ecr_palavakkam", 12.9580, 80.2581),
        Triple("ecr_akkarai", 12.9080, 80.2450),
        Triple("guindy", 13.0067, 80.2025),
        Triple("central", 13.0827, 80.2707),
        Triple("egmore", 13.0826, 80.2607),
        Triple("triplicane", 13.0587, 80.2757),
        Triple("mylapore", 13.0339, 80.2677),
        Triple("marina_beach", 13.0500, 80.2824),
        Triple("cmbt", 13.0694, 80.1948),
        Triple("airport", 12.9815, 80.1636),
        Triple("alandur", 13.0039, 80.2014),
        Triple("tnagar", 13.0418, 80.2341),
        Triple("tambaram", 12.9249, 80.1000),
        Triple("velachery", 12.9815, 80.2180),
        Triple("omr_tidel_park", 12.9895, 80.2470),
        Triple("omr_sholinganallur", 12.9010, 80.2279)
    )

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): UserLocationResult? = withContext(Dispatchers.IO) {
        try {
            var bestLocation: Location? = null

            // 1. Try system LocationManager
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                val gpsLoc = try { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: Throwable) { null }
                val netLoc = try { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: Throwable) { null }
                bestLocation = gpsLoc ?: netLoc
            }

            // 2. Try Google Play Services Fused Location if available
            if (bestLocation == null) {
                try {
                    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                    val fusedLoc: Location? = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                        fusedClient.lastLocation
                            .addOnSuccessListener { loc -> if (cont.isActive) cont.resumeWith(Result.success(loc)) }
                            .addOnFailureListener { if (cont.isActive) cont.resumeWith(Result.success(null)) }
                    }
                    if (fusedLoc != null) {
                        bestLocation = fusedLoc
                    }
                } catch (e: Throwable) {
                    Log.d(TAG, "Fused location unavailable: ${e.message}")
                }
            }

            // If emulator or device has location
            val lat: Double
            val lng: Double
            val isGpsReal: Boolean

            if (bestLocation != null && isWithinTamilNadu(bestLocation.latitude, bestLocation.longitude)) {
                lat = bestLocation.latitude
                lng = bestLocation.longitude
                isGpsReal = true
            } else {
                // Default to popular Chennai commuter hub (Neelankarai / ECR coastal area as requested in brief)
                lat = 12.9495
                lng = 80.2583
                isGpsReal = bestLocation != null
            }

            // Find closest station
            val (nearestStationId, distKm) = findNearestChennaiHub(lat, lng)
            val station = ChennaiTransitRepository.getStationById(nearestStationId)
                ?: ChennaiTransitRepository.STATIONS.first { it.id == "ecr_neelankarai" }

            // Try reverse geocoding for locality name
            var localityName = station.nameEn
            var localityNameTa = station.nameTa

            try {
                val geocoder = Geocoder(context, Locale.ENGLISH)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val subLoc = addr.subLocality ?: addr.locality ?: addr.featureName
                    if (!subLoc.isNullOrBlank()) {
                        localityName = subLoc
                    }
                }
            } catch (e: Throwable) {
                Log.d(TAG, "Geocoder lookup: ${e.message}")
            }

            val boardingPoint = NearestTransitPoint(
                type = station.supportedModes.firstOrNull() ?: TransitMode.BUS,
                nameEn = "${station.nameEn} Boarding Stop",
                nameTa = "${station.nameTa} ஏறும் நிறுத்தம்",
                distanceEstimate = "${(distKm * 1000).toInt().coerceAtLeast(120)} meters (${((distKm * 1000) / 75).toInt().coerceAtLeast(2)} min walk)",
                walkMinutes = ((distKm * 1000) / 75).toInt().coerceAtLeast(2),
                detailsEn = "Direct boarding stop near ${station.locality}. Level footpath and pedestrian access.",
                detailsTa = "${station.locality} அருகில் உள்ள நேரடி பேருந்து நிறுத்தம். தரைத்தள அணுகல் வசதி.",
                keyRoutes = when (station.id) {
                    "ecr_neelankarai", "ecr_kottivakkam", "ecr_palavakkam" -> listOf("19B", "29C", "102", "588")
                    "triplicane" -> listOf("19B", "21G", "11G", "MRTS Suburban")
                    "guindy" -> listOf("Blue Line Metro", "Beach-Tambaram EMU", "18A", "M1")
                    "central" -> listOf("Blue & Green Line Metro", "Suburban EMU", "17D", "11H")
                    else -> listOf("MTC City Bus", "Direct Transit")
                },
                hasElevator = station.hasElevator,
                hasRamp = station.hasRamp,
                isFreeBusEligible = true,
                matchedStationId = station.id
            )

            UserLocationResult(
                localityName = localityName,
                localityNameTa = localityNameTa,
                latitude = lat,
                longitude = lng,
                nearestStation = station,
                nearestBoardingPoint = boardingPoint,
                isGpsAccurate = isGpsReal
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving user location", e)
            null
        }
    }

    private fun isWithinTamilNadu(lat: Double, lng: Double): Boolean {
        return lat in 12.0..14.0 && lng in 79.5..80.5
    }

    private fun findNearestChennaiHub(lat: Double, lng: Double): Pair<String, Double> {
        var minDistance = Double.MAX_VALUE
        var bestId = "ecr_neelankarai"

        for ((id, hubLat, hubLng) in CHENNAI_HUBS) {
            val dist = calculateHaversineDistance(lat, lng, hubLat, hubLng)
            if (dist < minDistance) {
                minDistance = dist
                bestId = id
            }
        }
        return Pair(bestId, minDistance)
    }

    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
