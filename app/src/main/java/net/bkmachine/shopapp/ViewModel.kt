package net.bkmachine.shopapp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AppViewModel : ViewModel() {
    var headerText by mutableStateOf("Pick Tool")
        private set

    fun setHeader(text: String) {
        headerText = text;
    }

    fun pickTool(scanCode: String) {
        Log.d("DEBUG", scanCode)
    }
}
