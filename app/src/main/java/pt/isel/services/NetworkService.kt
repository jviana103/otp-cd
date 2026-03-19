package pt.isel.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.pow
import kotlin.math.sqrt

class NetworkService {

    data class NetworkMetrics(
        val latencyStdDev: Double,
        val packetLoss: Double
    )


    suspend fun measureNetworkMetrics(host: String = "8.8.8.8", count: Int = 5): NetworkMetrics = withContext(Dispatchers.IO) {
        val latencies = mutableListOf<Double>()
        var packetsReceived = 0

        try {
            val process = Runtime.getRuntime().exec("/system/bin/ping -c $count -W 2 $host")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                Log.e("PingTest", "Ping result: $line")
                if (line?.contains("time=") == true) {
                    val timePart = line.timeRemovalFromPingString()
                    timePart?.toDoubleOrNull()?.let {
                        latencies.add(it)
                        packetsReceived++
                    }
                }
            }
            process.waitFor()
        } catch (e: Exception) {
            Log.e("NetworkService", "Error performing ping: ${e.message}")
        }

        val packetLoss = if (count > 0) (count - packetsReceived).toDouble() / count else 0.0
        val stdDev = if (latencies.size >= 2) calculateStandardDeviation(latencies) else 0.0

        NetworkMetrics(
            latencyStdDev = stdDev,
            packetLoss = packetLoss
        )
    }

    private fun String?.timeRemovalFromPingString(): String? = this?.substringAfter("time=")?.substringBefore(" ms")

    private fun calculateStandardDeviation(values: List<Double>): Double {
        val mean = values.average()
        val sumOfSquares = values.sumOf { (it - mean).pow(2) }
        return sqrt(sumOfSquares / values.size)
    }
}
