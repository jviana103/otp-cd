package pt.isel.viewer.ui.components

import pt.isel.viewer.model.Trip
import java.awt.*
import java.text.SimpleDateFormat
import javax.swing.*
import javax.swing.border.EmptyBorder

class TripCard(
    private val trip: Trip,
    private val onViewReadingsClicked: (String) -> Unit
) : JPanel(BorderLayout()) {

    init {
        setupUI()
    }

    private fun setupUI() {
        border = BorderFactory.createCompoundBorder(
            EmptyBorder(5, 5, 10, 5),
            BorderFactory.createTitledBorder("ID da viagem: ${trip.id}")
        )
        maximumSize = Dimension(Int.MAX_VALUE, 120)

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val dateString = if (trip.startDate != null) sdf.format(trip.startDate) else "Desconhecida"
        val isValidStr = if (trip.isValid) "Sim" else "Não"

        val infoText = """
            <html>
                <table cellpadding='4' style='font-family: SansSerif; font-size: 11px;'>
                    <tr>
                        <td><b>Data de início:</b></td><td width='150'>$dateString</td>
                        <td><b>Transporte:</b></td><td>${trip.transportType}</td>
                    </tr>
                    <tr>
                        <td><b>Viagem válida:</b></td><td>$isValidStr</td>
                        <td><b>ID de utilizador:</b></td><td>${trip.userId}</td>
                    </tr>
                </table>
            </html>
        """.trimIndent()

        val lblInfo = JLabel(infoText).apply { border = EmptyBorder(5, 10, 5, 10) }

        val btnLeituras = JButton("Ver leituras").apply {
            font = Font("SansSerif", Font.BOLD, 12)
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            addActionListener { onViewReadingsClicked(trip.id) }
        }

        val btnPanel = JPanel(GridBagLayout()).apply {
            border = EmptyBorder(0, 0, 0, 10)
            add(btnLeituras)
        }

        add(lblInfo, BorderLayout.CENTER)
        add(btnPanel, BorderLayout.EAST)
    }
}