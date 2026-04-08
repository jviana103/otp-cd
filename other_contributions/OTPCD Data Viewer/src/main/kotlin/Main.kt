package pt.isel.viewer

import pt.isel.viewer.config.FirebaseConfig
import pt.isel.viewer.repository.TripRepository
import pt.isel.viewer.ui.MainWindow
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

fun main() {
    try {
        val db = FirebaseConfig.initialize()
        val repository = TripRepository(db)

        SwingUtilities.invokeLater {
            MainWindow(repository).isVisible = true
        }
    } catch (e: Exception) {
        JOptionPane.showMessageDialog(null, "Erro na inicialização: ${e.message}")
    }
}