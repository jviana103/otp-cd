package pt.isel.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import pt.isel.datascan.domain.ScanReading
import pt.isel.datascan.domain.TripData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance("otp-cd-db")
) {

    fun createTrip(
        tripId: String,
        trip : TripData,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val tripData = trip.toMap()

        db.collection("viagens_teste")
            .document(tripId)
            .set(tripData)
            .addOnSuccessListener {
                Log.d("FirestoreRepository", "Trip $tripId initialized successfully")
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

        db.collection("viagens_teste")
            .document(tripId)
            .collection("leituras")
            .add(readingData)
            .addOnSuccessListener { ref ->
                Log.d("FirestoreRepository", "Reading stored with ID: ${ref.id} for trip $tripId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreRepository", "Error storing reading: ${e.message}")
                onFailure(e)
            }
    }
}


