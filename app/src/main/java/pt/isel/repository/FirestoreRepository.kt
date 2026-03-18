package pt.isel.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

import pt.isel.datascan.domain.ScanReading
import pt.isel.datascan.domain.TripData
import pt.isel.datascan.viewmodel.state.IS_TEST_TRIP


class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance("otp-cd-db")
) {

    private val collectionName: String
        get() = if (IS_TEST_TRIP) "viagens_teste" else "viagens"

    fun createTrip(
        tripId: String,
        trip : TripData,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val tripData = trip.toMap()

        db.collection(collectionName)
            .document(tripId)
            .set(tripData)
            .addOnSuccessListener {
                Log.d("FirestoreRepository", "Trip $tripId initialized successfully in $collectionName")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreRepository", "Error initializing trip: ${e.message}")
                onFailure(e)
            }
    }

    fun addReading(
        tripId: String,
        reading: ScanReading,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val readingData = reading.toMap()

        db.collection(collectionName)
            .document(tripId)
            .collection("leituras")
            .add(readingData)
            .addOnSuccessListener { ref ->
                Log.d("FirestoreRepository", "Reading stored with ID: ${ref.id} for trip $tripId in $collectionName")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreRepository", "Error storing reading: ${e.message}")
                onFailure(e)
            }
    }
}
