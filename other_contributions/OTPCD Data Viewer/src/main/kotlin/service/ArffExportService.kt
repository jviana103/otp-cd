package pt.isel.viewer.service

import pt.isel.viewer.repository.TripRepository
import java.io.File

class ArffExportService(private val repository: TripRepository) {

    fun exportToArff(collectionName: String, file: File) {
        val allTrips = repository.fetchTrips(collectionName)
        val validTrips = allTrips.filter { it.isValid }

        val sb = StringBuilder()

        sb.append("""
            @RELATION subjective_occupancy_prediction

            @ATTRIBUTE userId STRING
            @ATTRIBUTE transportType {METRO,TRAIN}
            @ATTRIBUTE timestamp NUMERIC
            @ATTRIBUTE latitude NUMERIC
            @ATTRIBUTE longitude NUMERIC
            @ATTRIBUTE bluetoothCount NUMERIC
            @ATTRIBUTE bt_signal_1 NUMERIC
            @ATTRIBUTE bt_signal_2 NUMERIC
            @ATTRIBUTE bt_signal_3 NUMERIC
            @ATTRIBUTE bt_signal_4 NUMERIC
            @ATTRIBUTE bt_signal_5 NUMERIC
            @ATTRIBUTE wifiCount NUMERIC
            @ATTRIBUTE wf_signal_1 NUMERIC
            @ATTRIBUTE wf_signal_2 NUMERIC
            @ATTRIBUTE wf_signal_3 NUMERIC
            @ATTRIBUTE wf_signal_4 NUMERIC
            @ATTRIBUTE wf_signal_5 NUMERIC
            @ATTRIBUTE latencyAvg NUMERIC
            @ATTRIBUTE latencyStdDev NUMERIC
            @ATTRIBUTE packetLoss NUMERIC
            @ATTRIBUTE rsrp NUMERIC
            @ATTRIBUTE rssnr NUMERIC
            @ATTRIBUTE rsrq NUMERIC
            @ATTRIBUTE subjectiveRating {1,2,3,4,5}

            @DATA
            
        """.trimIndent())

        for (trip in validTrips) {
            val readings = repository.fetchReadings(collectionName, trip.id)

            for (r in readings) {
                if (r.subjectiveRating !in 1..5) continue

                val rowValues = listOf(
                    r.userId,
                    trip.transportType.uppercase(),
                    r.timestamp,
                    r.location?.latitude ?: "?",
                    r.location?.longitude ?: "?",
                    r.bluetoothCount
                ) + parseTop5Signals(r.signalIntensitiesBT) + listOf(
                    r.wifiCount
                ) + parseTop5Signals(r.signalIntensitiesWF) + listOf(
                    r.latencyAvg,
                    r.latencyStdDev,
                    r.packetLoss,
                    r.rsrp ?: "?",
                    r.rssnr ?: "?",
                    r.rsrq ?: "?",
                    r.subjectiveRating
                )

                sb.append(rowValues.joinToString(",")).append("\n")
            }
        }

        file.writeText(sb.toString())
    }

    private fun parseTop5Signals(signals: Any?): List<String> {
        val parsedList = (signals as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
        return List(5) { index -> parsedList.getOrNull(index)?.toString() ?: "?" }
    }
}