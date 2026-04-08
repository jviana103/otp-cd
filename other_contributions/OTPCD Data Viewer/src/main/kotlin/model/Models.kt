package pt.isel.viewer.model

import com.google.cloud.firestore.GeoPoint
import java.util.Date

data class Trip(
    val id: String,
    val startDate: Date?,
    val transportType: String,
    val isValid: Boolean,
    val userId: String
)

data class Reading(
    val id: String,
    val timestamp: Long,
    val userId: String,
    val subjectiveRating: Long,
    val location: GeoPoint?,
    val bluetoothCount: Long,
    val wifiCount: Long,
    val signalIntensitiesBT: List<*>,
    val signalIntensitiesWF: List<*>,
    val latencyAvg: Double,
    val latencyStdDev: Double,
    val packetLoss: Double,
    val rsrp: Long?,
    val rssnr: Long?,
    val rsrq: Long?
)