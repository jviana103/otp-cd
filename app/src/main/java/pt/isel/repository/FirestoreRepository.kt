package pt.isel.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import pt.isel.datascan.domain.ScanReading
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance("otp-cd-db")
) {

    fun createTrip(
        tripId: String,
        transportType: String = "Unknown",
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val tripData = mapOf(
            "tipo_transporte" to transportType,
            "data_hora_inicio" to SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        )

        db.collection("viagens")
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
        val readingData = mapOf(
            "timestamp_leitura" to SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date(reading.timestamp)),
            "qtd_dispositivos_bluetooth" to reading.bluetoothCount,
            "qtd_access_points" to reading.wifiCount,
            "intensidade_sinal_5_ap" to reading.signalIntensities,
            "localizacao" to GeoPoint(reading.latitude ?: 0.0, reading.longitude ?: 0.0),
            "latencia" to reading.latency,
            "perda_pacotes" to 0.0,
            "avaliacao_subjetiva" to reading.subjectiveRating
        )

        db.collection("viagens")
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
