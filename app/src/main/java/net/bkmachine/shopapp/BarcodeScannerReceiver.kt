package net.bkmachine.shopapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BarcodeScannerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("DEBUG", intent?.action.toString())
    }
}

/*
* const val QR_ACTION: String = "scan.rcv.message"
const val QR_EXTRA: String = "barcodeData"

private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {


        *//*try {
            // Timber.d("Get intent ${intent.action}")
            if (QR_ACTION == intent.action) {
                if (intent.hasExtra(QR_EXTRA)) {
                    val code = intent.getStringExtra(QR_EXTRA)
                    // Timber.d("New QR code $code")
                    pickTool(code.toString())
                }
            }
        } catch (t: Throwable) {
            // handle errors
        }*//*
    }
}
*/