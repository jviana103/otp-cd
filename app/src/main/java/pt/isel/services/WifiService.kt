package pt.isel.services

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.registerReceiver

class WifiService (private val context: Context){

    private lateinit var wifiManager: WifiManager
    private lateinit var txtLog: TextView
    private lateinit var btnScan: Button


    private fun initiateWifiScan() {
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
                context.unregisterReceiver(this)
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(wifiScanReceiver, intentFilter)

        val success = wifiManager.startScan()
        if (!success) {
            scanFailure()
        } else {
            txtLog.text = "A solicitar novos dados de Wi-Fi...\n"
        }
    }

    private fun scanSuccess() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            val results = wifiManager.scanResults
            TODO("SE DER CERTO, O Q FAZER COM O RESULTS")

        } else {
            TODO("SE N DER CERTO")
        }
    }

    private fun scanFailure() {
        Log.e("error","Erro: Permissão de Localização negada")
    }

}