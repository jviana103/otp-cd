package pt.isel.datascan.domain

data class ScanReading(
    val timestamp: Long = System.currentTimeMillis(),
    val bluetoothCount: Int = 0,
    val wifiCount: Int = 0,
    val signalIntensities: List<Int> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val latency: Long = 0,
    val subjectiveRating: Int = 0
)