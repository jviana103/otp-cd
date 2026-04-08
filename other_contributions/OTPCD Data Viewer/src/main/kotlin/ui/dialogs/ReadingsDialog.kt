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
) : JDialog(parent, "Leituras da Viagem: $tripId", true) {

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
                        sb.append("TOTAL DE LEITURAS REGISTADAS: ${readings.size}\n\n")
                        readings.forEachIndexed { index, doc ->
                            val dateString = if (doc.timestamp > 0) sdf.format(Date(doc.timestamp)) else "N/D"

                            sb.append("─────────────────────────────────────────────────────────────────\n")
                            sb.append(" LEITURA #${index + 1} | ID: ${doc.id}\n")
                            sb.append("─────────────────────────────────────────────────────────────────\n")
                            sb.append(" Data/Hora (Timestamp)  : $dateString (${doc.timestamp})\n")
                            sb.append(" Avaliação Subjetiva    : ${if (doc.subjectiveRating >= 0) doc.subjectiveRating else "N/D"}\n")

                            val locString = if (doc.location != null) "Lat ${doc.location.latitude}, Lng ${doc.location.longitude}" else "N/D"
                            sb.append(" Localização (GPS)      : $locString\n\n")

                            sb.append(" [ Wi-Fi & Bluetooth ]\n")
                            sb.append("   Qtd. Dispositivos BT : ${doc.bluetoothCount}\n")
                            sb.append("   Sinais BT (Top 5)    : ${doc.signalIntensitiesBT}\n")
                            sb.append("   Qtd. Redes Wi-Fi     : ${doc.wifiCount}\n")
                            sb.append("   Sinais Wi-Fi (Top 5) : ${doc.signalIntensitiesWF}\n\n")

                            sb.append(" [ Desempenho de Rede ]\n")
                            sb.append("   Latência Média       : ${doc.latencyAvg} ms\n")
                            sb.append("   Desvio Padrão Lat.   : ${doc.latencyStdDev} ms\n")
                            sb.append("   Perda de Pacotes     : ${doc.packetLoss} %\n\n")

                            sb.append(" [ Rede Móvel (Celular) ]\n")
                            sb.append("   RSRP (Potência)      : ${doc.rsrp ?: "N/D"}\n")
                            sb.append("   RSSNR (Qualidade)    : ${doc.rssnr ?: "N/D"}\n")
                            sb.append("   RSRQ (Interferência) : ${doc.rsrq ?: "N/D"}\n\n")
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