package pt.isel.services

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
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

class BluetoothService(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val _deviceCount = MutableStateFlow(0)
    val deviceCount: StateFlow<Int> = _deviceCount.asStateFlow()

    private val discoveredDevices = MutableStateFlow<Map<String, Int>>(emptyMap())

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val strongestSignals: StateFlow<List<Int>> = discoveredDevices
        .map { devices ->
            devices.values
                .sortedDescending()
                .take(5)
        }
        .stateIn(
            scope = serviceScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceAddress = result.device.address
            val rssi = result.rssi

            if (!discoveredDevices.value.containsKey(deviceAddress)) {
                val allDevices = discoveredDevices.value.toMutableMap()
                allDevices[deviceAddress] = rssi
                _deviceCount.value = allDevices.size
                discoveredDevices.value = allDevices
                Log.d("BluetoothService", "New device: $deviceAddress | Count: ${deviceCount.value}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (bluetoothAdapter?.isEnabled != true || scanner == null) {
            Log.e("BLE_SCAN", "Bluetooth is OFF or Scanner is null")
            return
        }

        scanner.startScan(scanCallback)
    }

    fun clearScan() {
        discoveredDevices.value = emptyMap()
        _deviceCount.value = 0
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        discoveredDevices.value = emptyMap()
        _deviceCount.value = 0
    }
}