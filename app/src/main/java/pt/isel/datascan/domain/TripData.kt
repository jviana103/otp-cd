package pt.isel.datascan.domain

data class TripData (
    val transportType: String = "Unknown",
    val startDate : String
) {
    fun toMap() : Map<String, String> = mapOf(
        "transportType" to transportType,
        "startDate" to startDate
    )
}