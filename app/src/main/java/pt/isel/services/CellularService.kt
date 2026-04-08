package pt.isel.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.CellInfo
import android.telephony.CellInfoLte
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

        val signalStrengths = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            telephonyManager.signalStrength?.cellSignalStrengths ?: emptyList()
        } else {
            emptyList()
        }

        return when (registeredCell) {
            is CellInfoLte -> {
                val lteSignal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    signalStrengths.filterIsInstance<android.telephony.CellSignalStrengthLte>().firstOrNull()
                        ?: registeredCell.cellSignalStrength
                } else {
                    registeredCell.cellSignalStrength
                }

                CellularMetrics(
                    rsrp = lteSignal.rsrp.takeIf { it != CellInfo.UNAVAILABLE },
                    rssnr = lteSignal.rssnr.takeIf { it != CellInfo.UNAVAILABLE },
                    rsrq = lteSignal.rsrq.takeIf { it != CellInfo.UNAVAILABLE }
                )
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    getNrMetricsSafe(registeredCell, signalStrengths)
                } else {
                    CellularMetrics()
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private fun getNrMetricsSafe(registeredCell: CellInfo, signalStrengths: List<android.telephony.CellSignalStrength>): CellularMetrics {
        if (registeredCell is android.telephony.CellInfoNr) {
            val nrSignal = signalStrengths.filterIsInstance<android.telephony.CellSignalStrengthNr>().firstOrNull()
                ?: (registeredCell.cellSignalStrength as android.telephony.CellSignalStrengthNr)

            return CellularMetrics(
                rsrp = nrSignal.ssRsrp.takeIf { it != CellInfo.UNAVAILABLE }
                    ?: nrSignal.csiRsrp.takeIf { it != CellInfo.UNAVAILABLE },
                rssnr = nrSignal.ssSinr.takeIf { it != CellInfo.UNAVAILABLE }
                    ?: nrSignal.csiSinr.takeIf { it != CellInfo.UNAVAILABLE },
                rsrq = nrSignal.ssRsrq.takeIf { it != CellInfo.UNAVAILABLE }
                    ?: nrSignal.csiRsrq.takeIf { it != CellInfo.UNAVAILABLE }
            )
        }
        return CellularMetrics()
    }
}