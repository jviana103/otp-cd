package pt.isel.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.CellInfo
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellSignalStrengthNr
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

class CellularService(private val context: Context) {

    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    data class CellularMetrics(
        val rsrp: Int? = null,
        val rssnr: Int? = null,
        val rsrq: Int? = null,
        val cqi: Int? = null
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
        } ?: return CellularMetrics()

        val registeredCell = allCellInfo.firstOrNull { it.isRegistered } ?: return CellularMetrics()

        return when (registeredCell) {
            is CellInfoLte -> {
                val signal = registeredCell.cellSignalStrength
                CellularMetrics(
                    rsrp = signal.rsrp.takeIf { it != CellInfo.UNAVAILABLE },
                    rssnr = signal.rssnr.takeIf { it != CellInfo.UNAVAILABLE },
                    rsrq = signal.rsrq.takeIf { it != CellInfo.UNAVAILABLE },
                    cqi = signal.cqi.takeIf { it != CellInfo.UNAVAILABLE }
                )
            }
            is CellInfoNr -> {
                val signal = registeredCell.cellSignalStrength as CellSignalStrengthNr
                val cqiList = signal.csiCqiReport
                val widebandCqi = cqiList.firstOrNull()?.takeIf { it != CellInfo.UNAVAILABLE }
                CellularMetrics(
                    rsrp = signal.ssRsrp.takeIf { it != CellInfo.UNAVAILABLE },
                    rssnr = signal.ssSinr.takeIf { it != CellInfo.UNAVAILABLE },
                    rsrq = signal.ssRsrq.takeIf { it != CellInfo.UNAVAILABLE },
                    cqi = widebandCqi
                )
            }
            else -> CellularMetrics()
        }
    }
}