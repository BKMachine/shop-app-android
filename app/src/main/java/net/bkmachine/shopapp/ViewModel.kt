package net.bkmachine.shopapp

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
}
