package pt.isel.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import pt.isel.datascan.domain.ScanReading
import pt.isel.datascan.domain.TripData
import pt.isel.datascan.viewmodel.state.IS_TEST_TRIP

class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance("otp-cd-db"),
    private val authRepository: AuthRepository = AuthRepository()
) {
    var isTest: Boolean = IS_TEST_TRIP

    private val collectionName: String
        get() = if (isTest) "viagens_teste" else "viagens"

    fun createTrip(
        tripId: String,
        trip : TripData,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        authRepository.ensureAuth(
            onSuccess = {
                db.collection(collectionName)
                    .document(tripId)
                    .set(trip.toMap())
                    .addOnSuccessListener {
                        Log.d("FirestoreRepository", "Trip $tripId created successfully")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreRepository", "Error creating trip $tripId", e)
                        onFailure(e)
                    }
            },
            onFailure = onFailure
        )
    }

    fun addReading(
        tripId: String,
        reading: ScanReading,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        authRepository.ensureAuth(
            onSuccess = {
                db.collection(collectionName)
                    .document(tripId)
                    .collection("leituras")
                    .add(reading.toMap())
                    .addOnSuccessListener { ref ->
                        Log.d("FirestoreRepository", "Reading added with ID: ${ref.id}")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreRepository", "Error adding reading to trip $tripId", e)
                        onFailure(e)
                    }
            },
            onFailure = onFailure
        )
    }
}
