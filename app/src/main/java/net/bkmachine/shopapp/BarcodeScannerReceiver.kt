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
            val action = intent?.action
            Log.d("BarcodeScannerReceiver", "Received intent: $action")
            
            if (action == QR_ACTION) {
                // Support both String and CharSequence extras as seen in PartInventory
                val code = intent.getStringExtra(QR_EXTRA) ?: intent.getCharSequenceExtra(QR_EXTRA)?.toString()
                
                if (!code.isNullOrEmpty()) {
                    Log.d("BarcodeScannerReceiver", "Scanned code: $code")
                    MyViewModel.handleScan(code)
                } else {
                    Log.w("BarcodeScannerReceiver", "Scanned code was null or empty")
                }
            }
        } catch (e: Exception) {
            Log.e("BarcodeScannerReceiver", "Error receiving scan", e)
        }
    }
}
