package net.bkmachine.shopapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import net.bkmachine.shopapp.ui.ImageUploaderScreen
import net.bkmachine.shopapp.ui.theme.ShopAppTheme
import net.bkmachine.shopapp.ui.theme.ImageBluePrimary

class ImageUploaderActivity : ComponentActivity() {
    private val viewModel = MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize viewmodel if needed (it's a singleton in this project)
        viewModel.init(this)

        setContent {
            ShopAppTheme(darkTheme = viewModel.isDarkTheme, primaryOverride = ImageBluePrimary) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = viewModel.backgroundColor
                ) {
                    ImageUploaderScreen(viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // The Activity destruction is the "hard reset" for the camera hardware
    }
}
