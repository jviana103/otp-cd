package pt.isel.services

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothService(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val _deviceCount = MutableStateFlow(0)
    val deviceCount: StateFlow<Int> = _deviceCount.asStateFlow()

    private val discoveredDevices = mutableMapOf<String, Int>()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceAddress = result.device.address
            val rssi = result.rssi

            if (!discoveredDevices.containsKey(deviceAddress)) {
                discoveredDevices[deviceAddress] = rssi
                _deviceCount.value = discoveredDevices.size
                Log.d("BluetoothService", "New device: $deviceAddress | Count: ${discoveredDevices.size}")
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
        discoveredDevices.clear()
        _deviceCount.value = 0
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        discoveredDevices.clear()
        _deviceCount.value = 0
    }
}