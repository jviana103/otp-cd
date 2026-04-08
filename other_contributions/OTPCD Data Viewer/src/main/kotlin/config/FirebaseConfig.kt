package pt.isel.viewer.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import java.io.FileInputStream

object FirebaseConfig {
    fun initialize(): Firestore {
        val keyPath = System.getenv("FIREBASE_KEY_PATH")
            ?: throw IllegalArgumentException("Variável de ambiente FIREBASE_KEY_PATH não definida.")

        val options = FirestoreOptions.newBuilder()
            .setCredentials(GoogleCredentials.fromStream(FileInputStream(keyPath)))
            .setDatabaseId("otp-cd-db")
            .build()

        return options.service
    }
}