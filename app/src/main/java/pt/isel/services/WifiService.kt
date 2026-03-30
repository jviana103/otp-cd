package pt.isel.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class WifiService(private val context: Context) {
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _wifiCount = MutableStateFlow(0)
    val wifiCount: StateFlow<Int> = _wifiCount.asStateFlow()

    private val discoveredWifi = MutableStateFlow<Map<String, Int>>(emptyMap())

    private var isReceiverRegistered = false
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    val strongestSignals: StateFlow<List<Int>> = discoveredWifi
        .map { map ->
            map.values.sortedDescending().take(5)
        }
        .stateIn(
            scope = serviceScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

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

        val allWifiS = discoveredWifi.value.toMutableMap()

        results.forEach { scanResult ->
            val bssid = scanResult.BSSID
            val rssi = scanResult.level

            allWifiS[bssid] = rssi
        }

        discoveredWifi.value = allWifiS

        val uniqueCount = allWifiS.size
        _wifiCount.value = uniqueCount

        Log.d("WifiService", "Scan Complete: Found $uniqueCount unique APs")
    }

    fun startScan() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            context.registerReceiver(wifiScanReceiver, intentFilter)
            isReceiverRegistered = true
        }
        val success = wifiManager.startScan()
        Log.d("WifiService", "Scan Started: $success")
        if (!success) {
            updateResults()
        }
    }

    fun stopScan() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(wifiScanReceiver)
            } catch (e: IllegalArgumentException) {
                Log.e("WifiService", "Receiver wasn't registered", e)
            }
            isReceiverRegistered = false
        }
        _wifiCount.value = 0
    }

    fun clearScan() {
        _wifiCount.value = 0
    }

    fun requestNewScan() {
        wifiManager.startScan()
    }
}