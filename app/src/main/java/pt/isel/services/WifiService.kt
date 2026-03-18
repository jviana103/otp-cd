package pt.isel.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WifiService(private val context: Context) {
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _wifiCount = MutableStateFlow(0)
    val wifiCount: StateFlow<Int> = _wifiCount.asStateFlow()

    private var lastValidWifiCount = 0

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                Log.d("WifiService", "Scan Complete")
                updateResults()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateResults() {
        val results = wifiManager.scanResults
        if (results.isEmpty()) return

        val uniqueCount = results.map { it.BSSID }.distinct().size

        if (uniqueCount > 0) {
            lastValidWifiCount = uniqueCount
            _wifiCount.value = uniqueCount
        }

        Log.d("WifiService", "Scan Complete: Found $uniqueCount unique APs")
    }

    fun startScan() {
        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)

        val success = wifiManager.startScan()
        Log.d("WifiService", "Scan Started: $success")
        if (!success) {
            updateResults()
        }
    }

    fun stopScan() {
        context.unregisterReceiver(wifiScanReceiver)
        _wifiCount.value = 0
    }

    fun clearScan() {
        _wifiCount.value = 0
    }

    fun requestNewScan() {
        wifiManager.startScan()
    }
}