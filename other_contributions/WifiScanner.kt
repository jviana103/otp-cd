private fun initiateWifiScan() {
    val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
            unregisterReceiver(this)
        }
    }

    val intentFilter = IntentFilter()
    intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
    registerReceiver(wifiScanReceiver, intentFilter)

    val success = wifiManager.startScan()
    if (!success) {
        scanFailure()
    } else {
        txtLog.text = "A solicitar novos dados de Wi-Fi...\n"
    }
}

private fun scanSuccess() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {

        val results = wifiManager.scanResults
        updateUI("Novo Scan Sucesso", results)

    } else {
        updateUI("Erro: Permissão de Localização negada", emptyList())
    }
}

private fun scanFailure() {
    updateUI("Erro: Permissão de Localização negada", emptyList())
}

private fun updateUI(status: String, results: List<ScanResult>) {
    runOnUiThread {
        txtLog.text = "Status: $status\n"
        txtLog.append("Dispositivos Wi-Fi: ${results.size}\n\n")

        for (res in results) {
            val ssid = if (res.SSID.isNullOrEmpty()) "Rede Oculta" else res.SSID
            txtLog.append("SSID: $ssid | BSSID: ${res.BSSID} | Sinal: ${res.level} dBm\n")
        }
    }
}