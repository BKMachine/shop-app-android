package net.bkmachine.shopapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BarcodeScannerReceiver : BroadcastReceiver() {
    val QR_ACTION: String = "scan.rcv.message"
    private val QR_EXTRA: String = "barcodeData"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("DEBUG", "SCAN EVENT")
        if (intent?.action == QR_ACTION) {
            if (intent.hasExtra(QR_EXTRA)) {
                val code = intent.getStringExtra(QR_EXTRA)
                Log.d("SCAN_CODE", code.toString())
            }
        }
    }
}
