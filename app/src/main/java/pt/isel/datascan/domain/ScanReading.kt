package pt.isel.datascan.domain

import com.google.firebase.firestore.GeoPoint

data class ScanReading(
    val timestamp: Long = System.currentTimeMillis(),
    val bluetoothCount: Int = 0,
    val wifiCount: Int = 0,
    val signalIntensitiesBT: List<Int> = emptyList(),
    val signalIntensitiesWF: List<Int> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val latency: Long = 0,
    val subjectiveRating: Int = 0
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "timestamp" to timestamp,
        "bluetoothCount" to bluetoothCount,
        "wifiCount" to wifiCount,
        "signalIntensitiesBT" to signalIntensitiesBT,
        "location" to GeoPoint(latitude ?: 0.0, longitude ?: 0.0),
        "latency" to latency,
        "subjectiveRating" to subjectiveRating
    )
}