package pt.isel.viewer.ui

import pt.isel.viewer.repository.TripRepository
import pt.isel.viewer.service.ArffExportService
import pt.isel.viewer.ui.components.TripCard
import pt.isel.viewer.ui.dialogs.ReadingsDialog
import java.awt.*
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder
import kotlin.concurrent.thread

class MainWindow(private val repository: TripRepository) : JFrame("Visualizador de Viagens OTP-CD") {

    private val exportService = ArffExportService(repository)

    private val comboCollections = JComboBox(arrayOf("viagens", "viagens_teste"))
    private val btnSearch = JButton("Buscar viagens")
    private val btnExportWeka = JButton("Exportar dados para .arff")
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
        setSize(900, 600)
        layout = BorderLayout()

        val topPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            border = EmptyBorder(10, 10, 10, 10)
            add(JLabel("Coleção:"))
            add(comboCollections)
            add(btnSearch)

            add(Box.createHorizontalStrut(20))
            btnExportWeka.background = Color(220, 240, 220)
            add(btnExportWeka)
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

        btnExportWeka.addActionListener { handleWekaExport() }
    }

    private fun handleWekaExport() {
        val collectionName = comboCollections.selectedItem.toString()

        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Guardar ficheiro ARFF"
        fileChooser.selectedFile = File("dataset_otpcd_${collectionName}.arff")

        val userSelection = fileChooser.showSaveDialog(this)

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            val fileToSave = fileChooser.selectedFile

            btnExportWeka.isEnabled = false
            btnExportWeka.text = "A Exportar..."

            thread {
                try {
                    exportService.exportToArff(collectionName, fileToSave)

                    SwingUtilities.invokeLater {
                        JOptionPane.showMessageDialog(
                            this@MainWindow,
                            "Exportação concluída com sucesso para:\n${fileToSave.absolutePath}",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                        btnExportWeka.isEnabled = true
                        btnExportWeka.text = "Exportar dados para ficheiro .arff"
                    }
                } catch (e: Exception) {
                    SwingUtilities.invokeLater {
                        JOptionPane.showMessageDialog(
                            this@MainWindow,
                            "Erro ao exportar dados: ${e.message}",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                        )
                        btnExportWeka.isEnabled = true
                        btnExportWeka.text = "Exportar dados para ficheiro .arff"
                    }
                }
            }
        }
    }

    private fun loadTrips() {
        val collectionName = comboCollections.selectedItem.toString()
        btnSearch.isEnabled = false
        btnExportWeka.isEnabled = false

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
                    btnExportWeka.isEnabled = true
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    listPanel.removeAll()
                    listPanel.add(JLabel(" Erro ao buscar dados: ${e.message}"))
                    refreshListPanel()
                    btnSearch.isEnabled = true
                    btnExportWeka.isEnabled = true
                }
            }
        }
    }

    private fun refreshListPanel() {
        listPanel.revalidate()
        listPanel.repaint()
    }
}