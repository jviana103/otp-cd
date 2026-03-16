package com.example.testapp
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var scanning = false
    private lateinit var btnScan: Button
    private lateinit var txtLog: TextView
    private lateinit var txtCounter: TextView
    val map = mutableMapOf<String, Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtCounter = findViewById(R.id.txtCounter)
        btnScan = findViewById(R.id.btnScan)
        txtLog = findViewById(R.id.txtLog)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        btnScan.setOnClickListener {
            checkPermissionsAndStart()
        }
    }

    private fun checkPermissionsAndStart() {
        val permissionsNeeded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missingPermissions = permissionsNeeded.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
        } else {
            startBleScan()
        }
    }

    private fun startBleScan() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            txtLog.text = "Erro: Bluetooth desligado ou não suportado."
            return
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (!scanning) {
            scanning = true
            btnScan.text = "Parar Scan"
            txtLog.text = "A procurar dispositivos...\n"
            runOnUiThread {
                txtCounter.text = "Dispositivos: 0"
            }
            scanner.startScan(leScanCallback)
        }

        else {
            scanning = false
            btnScan.text = "Iniciar Scan"
            map.clear()
            scanner.stopScan(leScanCallback)
        }
    }

    private val leScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceName = result.device.name ?: "Desconhecido"
            val deviceAddress = result.device.address
            val rssi = result.rssi

            if (!map.keys.contains(deviceAddress)) {
                Log.d("BLE_DATA", "Encontrado: $deviceAddress | $rssi dBm")
                map[deviceAddress] = rssi

                val info = "Nome: $deviceName\nMAC: $deviceAddress\nSinal: $rssi dBm\n\n"
                runOnUiThread {
                    txtCounter.text = "Dispositivos: ${map.size}"
                    txtLog.append(info)
                }
            }
        }
    }
}