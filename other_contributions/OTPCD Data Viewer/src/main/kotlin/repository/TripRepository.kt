package pt.isel.viewer.repository

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import pt.isel.viewer.model.Reading
import pt.isel.viewer.model.Trip

class TripRepository(private val db: Firestore) {

    fun fetchTrips(collectionName: String): List<Trip> {
        val snapshot = db.collection(collectionName).get().get()

        return snapshot.documents.map { doc ->
            val id = doc.id
            val startDate = doc.getDate("startDate")
            val transportType = doc.getString("transportType") ?: "N/D"
            val isValid = doc.getBoolean("isTripValid") ?: false

            val readingSnapshot = db.collection(collectionName)
                .document(id)
                .collection("leituras")
                .limit(1)
                .get()
                .get()

            val userId = if (!readingSnapshot.isEmpty) {
                readingSnapshot.documents[0].getString("userId") ?: "N/D"
            } else {
                "Sem leituras"
            }

            Trip(id, startDate, transportType, isValid, userId)
        }.sortedByDescending { it.startDate?.time ?: 0L }
    }

    fun fetchReadings(collectionName: String, tripId: String): List<Reading> {
        val snapshot = db.collection(collectionName)
            .document(tripId)
            .collection("leituras")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .get()

        return snapshot.documents.map { doc ->
            Reading(
                id = doc.id,
                timestamp = doc.getLong("timestamp") ?: 0L,
                userId = doc.getString("userId") ?: "N/D",
                subjectiveRating = doc.getLong("subjectiveRating") ?: -1L,
                location = doc.getGeoPoint("location"),
                bluetoothCount = doc.getLong("bluetoothCount") ?: 0L,
                wifiCount = doc.getLong("wifiCount") ?: 0L,
                signalIntensitiesBT = doc.get("signalIntensitiesBT") as? List<*> ?: emptyList<Any>(),
                signalIntensitiesWF = doc.get("signalIntensitiesWF") as? List<*> ?: emptyList<Any>(),
                latencyAvg = doc.getDouble("latencyAvg") ?: 0.0,
                latencyStdDev = doc.getDouble("latencyStdDev") ?: 0.0,
                packetLoss = doc.getDouble("packetLoss") ?: 0.0,
                rsrp = doc.getLong("rsrp"),
                rssnr = doc.getLong("rssnr"),
                rsrq = doc.getLong("rsrq")
            )
        }
    }
}