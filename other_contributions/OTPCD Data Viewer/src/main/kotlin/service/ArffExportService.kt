package pt.isel.viewer.service

import pt.isel.viewer.repository.TripRepository
import java.io.File

class ArffExportService(private val repository: TripRepository) {

    fun exportToArff(collectionName: String, file: File) {
        val allTrips = repository.fetchTrips(collectionName)
        val validTrips = allTrips.filter { it.isValid }

        val sb = StringBuilder()

        sb.append("@RELATION subjective_occupancy_prediction\n\n")
        sb.append("@ATTRIBUTE userId STRING\n")
        sb.append("@ATTRIBUTE transportType {METRO,TRAIN}\n")
        sb.append("@ATTRIBUTE timestamp NUMERIC\n")
        sb.append("@ATTRIBUTE latitude NUMERIC\n")
        sb.append("@ATTRIBUTE longitude NUMERIC\n")
        sb.append("@ATTRIBUTE bluetoothCount NUMERIC\n")
        sb.append("@ATTRIBUTE bt_signal_1 NUMERIC\n")
        sb.append("@ATTRIBUTE bt_signal_2 NUMERIC\n")
        sb.append("@ATTRIBUTE bt_signal_3 NUMERIC\n")
        sb.append("@ATTRIBUTE bt_signal_4 NUMERIC\n")
        sb.append("@ATTRIBUTE bt_signal_5 NUMERIC\n")
        sb.append("@ATTRIBUTE wifiCount NUMERIC\n")
        sb.append("@ATTRIBUTE wf_signal_1 NUMERIC\n")
        sb.append("@ATTRIBUTE wf_signal_2 NUMERIC\n")
        sb.append("@ATTRIBUTE wf_signal_3 NUMERIC\n")
        sb.append("@ATTRIBUTE wf_signal_4 NUMERIC\n")
        sb.append("@ATTRIBUTE wf_signal_5 NUMERIC\n")
        sb.append("@ATTRIBUTE latencyAvg NUMERIC\n")
        sb.append("@ATTRIBUTE latencyStdDev NUMERIC\n")
        sb.append("@ATTRIBUTE packetLoss NUMERIC\n")
        sb.append("@ATTRIBUTE rsrp NUMERIC\n")
        sb.append("@ATTRIBUTE rssnr NUMERIC\n")
        sb.append("@ATTRIBUTE rsrq NUMERIC\n")
        sb.append("@ATTRIBUTE subjectiveRating {1,2,3,4,5}\n\n")
        sb.append("@DATA\n")

        for (trip in validTrips) {
            val readings = repository.fetchReadings(collectionName, trip.id)

            for (r in readings) {
                if (r.subjectiveRating !in 1..5) continue

                val transport = if (trip.transportType.uppercase() == "METRO") "METRO" else "TRAIN"
                val lat = r.location?.latitude?.toString() ?: "?"
                val lon = r.location?.longitude?.toString() ?: "?"
                val rsrp = r.rsrp?.toString() ?: "?"
                val rssnr = r.rssnr?.toString() ?: "?"
                val rsrq = r.rsrq?.toString() ?: "?"

                val bt = (r.signalIntensitiesBT as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
                val bt1 = bt.getOrNull(0)?.toString() ?: "?"
                val bt2 = bt.getOrNull(1)?.toString() ?: "?"
                val bt3 = bt.getOrNull(2)?.toString() ?: "?"
                val bt4 = bt.getOrNull(3)?.toString() ?: "?"
                val bt5 = bt.getOrNull(4)?.toString() ?: "?"

                val wf = (r.signalIntensitiesWF as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()
                val wf1 = wf.getOrNull(0)?.toString() ?: "?"
                val wf2 = wf.getOrNull(1)?.toString() ?: "?"
                val wf3 = wf.getOrNull(2)?.toString() ?: "?"
                val wf4 = wf.getOrNull(3)?.toString() ?: "?"
                val wf5 = wf.getOrNull(4)?.toString() ?: "?"

                val userIdSafe = "\"${r.userId}\""

                sb.append("${userIdSafe},")
                sb.append("${transport},")
                sb.append("${r.timestamp},")
                sb.append("${lat},")
                sb.append("${lon},")
                sb.append("${r.bluetoothCount},")
                sb.append("${bt1},${bt2},${bt3},${bt4},${bt5},")
                sb.append("${r.wifiCount},")
                sb.append("${wf1},${wf2},${wf3},${wf4},${wf5},")
                sb.append("${r.latencyAvg},")
                sb.append("${r.latencyStdDev},")
                sb.append("${r.packetLoss},")
                sb.append("${rsrp},")
                sb.append("${rssnr},")
                sb.append("${rsrq},")
                sb.append("${r.subjectiveRating}\n")
            }
        }

        file.writeText(sb.toString())
    }
}