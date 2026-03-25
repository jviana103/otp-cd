package pt.isel.datascan.domain

import java.util.Date

data class TripData (
    val transportType: String = "Unknown",
    val startDate: Date
) {
    fun toMap() : Map<String, Any> = mapOf(
        "transportType" to transportType,
        "startDate" to startDate
    )
}