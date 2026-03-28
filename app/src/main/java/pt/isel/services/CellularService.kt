package pt.isel.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.CellInfo
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthNr
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

class CellularService(private val context: Context) {

    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    data class CellularMetrics(
        val rsrp: Int? = null,
        val rssnr: Int? = null,
        val rsrq: Int? = null
    )

    @SuppressLint("MissingPermission")
    fun getCurrentMetrics(): CellularMetrics {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return CellularMetrics()
        }

        val allCellInfo = try {
            telephonyManager.allCellInfo
        } catch (e: Exception) {
            null
        } ?: emptyList()

        val registeredCell = allCellInfo.firstOrNull { it.isRegistered } ?: return CellularMetrics()

        val currentSignalStrength = telephonyManager.signalStrength
        val signalStrengths = currentSignalStrength?.cellSignalStrengths ?: emptyList()

        return when (registeredCell) {
            is CellInfoLte -> {
                val lteSignal = signalStrengths.filterIsInstance<CellSignalStrengthLte>().firstOrNull() 
                    ?: registeredCell.cellSignalStrength

                CellularMetrics(
                    rsrp = lteSignal.rsrp.takeIf { it != CellInfo.UNAVAILABLE },
                    rssnr = lteSignal.rssnr.takeIf { it != CellInfo.UNAVAILABLE },
                    rsrq = lteSignal.rsrq.takeIf { it != CellInfo.UNAVAILABLE }
                )
            }
            is CellInfoNr -> {
                val nrSignal = signalStrengths.filterIsInstance<CellSignalStrengthNr>().firstOrNull()
                    ?: (registeredCell.cellSignalStrength as CellSignalStrengthNr)

                CellularMetrics(
                    rsrp = nrSignal.ssRsrp.takeIf { it != CellInfo.UNAVAILABLE } 
                        ?: nrSignal.csiRsrp.takeIf { it != CellInfo.UNAVAILABLE },
                    rssnr = nrSignal.ssSinr.takeIf { it != CellInfo.UNAVAILABLE } 
                        ?: nrSignal.csiSinr.takeIf { it != CellInfo.UNAVAILABLE },
                    rsrq = nrSignal.ssRsrq.takeIf { it != CellInfo.UNAVAILABLE } 
                        ?: nrSignal.csiRsrq.takeIf { it != CellInfo.UNAVAILABLE }
                )
            }
            else -> CellularMetrics()
        }
    }
}
