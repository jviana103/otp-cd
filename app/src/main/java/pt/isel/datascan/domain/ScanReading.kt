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
    val latencyStdDev: Double = 0.0,
    val packetLoss: Double = 0.0,
    val subjectiveRating: Int = 0,
    val rsrp: Int? = null,
    val rssnr: Int? = null,
    val rsrq: Int? = null,
    val cqi: Int? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "timestamp" to timestamp,
        "bluetoothCount" to bluetoothCount,
        "wifiCount" to wifiCount,
        "signalIntensitiesBT" to signalIntensitiesBT,
        "signalIntensitiesWF" to signalIntensitiesWF,
        "location" to GeoPoint(latitude ?: 0.0, longitude ?: 0.0),
        "latencyStdDev" to latencyStdDev,
        "packetLoss" to packetLoss,
        "subjectiveRating" to subjectiveRating,
        "rsrp" to rsrp,
        "rssnr" to rssnr,
        "rsrq" to rsrq,
        "cqi" to cqi
    )
}