package net.bkmachine.shopapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BarcodeScannerReceiver : BroadcastReceiver() {
    val QR_ACTION: String = "scan.rcv.message"
    private val QR_EXTRA: String = "barcodeData"

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            Log.d("BarcodeScannerReceiver", "Received intent: ${intent?.action}")
            if (intent?.action == QR_ACTION) {
                val code = intent.getStringExtra(QR_EXTRA)
                if (code != null) {
                    MyViewModel.handleScan(code)
                }
            }
        } catch (e: Exception) {
            Log.e("BarcodeScannerReceiver", "Error receiving scan", e)
        }
    }
}
