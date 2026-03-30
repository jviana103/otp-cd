package pt.isel.datascan.domain

import java.util.Date

data class TripData(
    val transportType: String,
    val startDate: Date,
    val isTripValid: Boolean = true
) {
    fun toMap(): Map<String, Any> = mapOf(
        "transportType" to transportType,
        "startDate" to startDate,
        "isTripValid" to isTripValid
    )
}