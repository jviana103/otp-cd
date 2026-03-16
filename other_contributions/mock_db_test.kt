import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.google.cloud.firestore.GeoPoint
import java.io.FileInputStream

fun sendMockData(db: Firestore) {
    val dadosViagem = mapOf(
        "id_voluntario" to "hash_anónimo_do_utilizador",
        "tipo_transporte" to "Metro",
        "data_hora_inicio" to "11-03-2026 08:30:00"
    )

    val dadosLeitura = mapOf(
        "timestamp_leitura" to "11-03-2026 08:30:30",
        "qtd_dispositivos_bluetooth" to 14,
        "qtd_access_points" to 6,
        "intensidade_sinal_5_ap" to listOf(-45, -55, -60, -71, -80),
        "localizacao" to GeoPoint(38.755, -9.116),
        "latencia" to 42,
        "perda_pacotes" to 0.001,
        "avaliacao_subjetiva" to 3
    )

    try {
        db.collection("viagens")
            .document("viagem_abc123")
            .set(dadosViagem)
            .get()

        val future = db.collection("viagens")
            .document("viagem_abc123")
            .collection("leituras")
            .document("leitura_001")
            .set(dadosLeitura)

        val resultado = future.get()
        println("Dados guardados com sucesso no timestamp ${resultado.updateTime}")

    } catch (e: Exception) {
        println("Erro em guardar: ${e.message}")
    }
}

fun getAndPrintMockData(db: Firestore) {
    try {
        val viagemSnapshot = db.collection("viagens").document("viagem_abc123").get().get()

        if (viagemSnapshot.exists()) {
            println("Viagens mock:")
            println(viagemSnapshot.data)

            val leiturasSnapshot = db.collection("viagens")
                .document("viagem_abc123")
                .collection("leituras")
                .get()
                .get()

            println("Leituras mock:")
            for (document in leiturasSnapshot.documents) {
                println("ID: ${document.id} -> ${document.data}")
            }
        } else {
            println("Nenhum dado encontrado")
        }
    } catch (e: Exception) {
        println("Erro ao obter os dados ${e.message}")
    }
}

fun main() {
    val keyPath = System.getenv("FIREBASE_KEY_PATH")

    val serviceAccount = FileInputStream(keyPath)
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    FirebaseApp.initializeApp(options)

    val firestoreOptions = FirestoreOptions.newBuilder()
        .setCredentials(GoogleCredentials.fromStream(FileInputStream(keyPath)))
        .setDatabaseId("otp-cd-db")
        .build()

    val db: Firestore = firestoreOptions.service

    println("Testando leitura na base de dados firestore otp-cd-db")
    getAndPrintMockData(db)
}