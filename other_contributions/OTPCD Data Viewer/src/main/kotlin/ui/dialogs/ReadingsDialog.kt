package pt.isel.viewer.ui.dialogs

import pt.isel.viewer.repository.TripRepository
import java.awt.*
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.*
import kotlin.concurrent.thread

class ReadingsDialog(
    parent: JFrame,
    private val repository: TripRepository,
    private val collectionName: String,
    private val tripId: String
) : JDialog(parent, "Leituras da viagem: $tripId", true) {

    private val txtArea = JTextArea("A carregar as leituras...\n")

    init {
        setupUI()
        loadData()
    }

    private fun setupUI() {
        setSize(700, 550)
        setLocationRelativeTo(parent)

        txtArea.apply {
            isEditable = false
            font = Font("Monospaced", Font.PLAIN, 13)
            margin = Insets(15, 15, 15, 15)
            background = Color(245, 245, 245)
        }

        add(JScrollPane(txtArea))
    }

    private fun loadData() {
        thread {
            try {
                val readings = repository.fetchReadings(collectionName, tripId)
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                val sb = java.lang.StringBuilder()

                SwingUtilities.invokeLater {
                    if (readings.isEmpty()) {
                        sb.append("Nenhuma leitura encontrada para esta viagem.")
                    } else {
                        sb.append("Total: ${readings.size}\n\n")
                        readings.forEachIndexed { index, doc ->
                            val dateString = if (doc.timestamp > 0) sdf.format(Date(doc.timestamp)) else "N/D"

                            sb.append(" Leitura Nº #${index + 1} | ID: ${doc.id}\n")
                            sb.append(" Timestamp: $dateString (${doc.timestamp})\n")
                            sb.append(" Avaliação subjetiva: ${if (doc.subjectiveRating >= 0) doc.subjectiveRating else "N/D"}\n")

                            val locString = if (doc.location != null) "Lat ${doc.location.latitude}, Lng ${doc.location.longitude}" else "N/D"
                            sb.append(" Localização: $locString\n\n")

                            sb.append(" [ Wi-Fi & Bluetooth ]\n")
                            sb.append("   Qtd. BT: ${doc.bluetoothCount}\n")
                            sb.append("   Top 5 sinais BT: ${doc.signalIntensitiesBT}\n")
                            sb.append("   Qtd. Wi-Fi: ${doc.wifiCount}\n")
                            sb.append("   Top 5 sinais Wi-Fi: ${doc.signalIntensitiesWF}\n\n")

                            sb.append(" [ Desempenho de rede ]\n")
                            sb.append("   Latência média: ${doc.latencyAvg} ms\n")
                            sb.append("   Desvio padrão da latência: ${doc.latencyStdDev} ms\n")
                            sb.append("   Perda de pacotes: ${doc.packetLoss} %\n\n")

                            sb.append(" [ Sinais de rede móvel ]\n")
                            sb.append("   RSRP: ${doc.rsrp ?: "N/D"}\n")
                            sb.append("   RSSNR: ${doc.rssnr ?: "N/D"}\n")
                            sb.append("   RSRQ: ${doc.rsrq ?: "N/D"}\n\n")
                        }
                    }
                    txtArea.text = sb.toString()
                    txtArea.caretPosition = 0
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    txtArea.text = "Erro ao carregar leituras: ${e.message}"
                }
            }
        }
    }
}