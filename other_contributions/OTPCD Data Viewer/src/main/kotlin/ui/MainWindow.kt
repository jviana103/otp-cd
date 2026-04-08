package pt.isel.viewer.ui

import pt.isel.viewer.repository.TripRepository
import pt.isel.viewer.ui.components.TripCard
import pt.isel.viewer.ui.dialogs.ReadingsDialog
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import kotlin.concurrent.thread

class MainWindow(private val repository: TripRepository) : JFrame("Visualizador de Viagens OTP-CD") {

    private val comboCollections = JComboBox(arrayOf("viagens", "viagens_teste"))
    private val btnSearch = JButton("Buscar Viagens")
    private val listPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    init {
        setupUI()
        bindEvents()
        loadTrips()
    }

    private fun setupUI() {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(850, 600)
        layout = BorderLayout()

        val topPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            border = EmptyBorder(10, 10, 10, 10)
            add(JLabel("Coleção:"))
            add(comboCollections)
            add(btnSearch)
        }

        val scrollPane = JScrollPane(listPanel).apply {
            verticalScrollBar.unitIncrement = 16
            border = EmptyBorder(10, 10, 10, 10)
        }

        add(topPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
        setLocationRelativeTo(null)
    }

    private fun bindEvents() {
        btnSearch.addActionListener { loadTrips() }
        comboCollections.addActionListener { loadTrips() }
    }

    private fun loadTrips() {
        val collectionName = comboCollections.selectedItem.toString()
        btnSearch.isEnabled = false

        listPanel.removeAll()
        listPanel.add(JLabel(" A carregar dados e identificar Utilizadores... por favor aguarde."))
        refreshListPanel()

        thread {
            try {
                val trips = repository.fetchTrips(collectionName)

                SwingUtilities.invokeLater {
                    listPanel.removeAll()

                    if (trips.isEmpty()) {
                        val emptyLabel = JLabel(" Nenhuma viagem encontrada na coleção '$collectionName'.")
                        emptyLabel.font = Font("SansSerif", Font.ITALIC, 14)
                        listPanel.add(emptyLabel)
                    } else {
                        trips.forEach { trip ->
                            val card = TripCard(trip) { tripId ->
                                val dialog = ReadingsDialog(this@MainWindow, repository, collectionName, tripId)
                                dialog.isVisible = true
                            }
                            listPanel.add(card)
                        }
                    }
                    refreshListPanel()
                    btnSearch.isEnabled = true
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    listPanel.removeAll()
                    listPanel.add(JLabel(" Erro ao buscar dados: ${e.message}"))
                    refreshListPanel()
                    btnSearch.isEnabled = true
                }
            }
        }
    }

    private fun refreshListPanel() {
        listPanel.revalidate()
        listPanel.repaint()
    }
}