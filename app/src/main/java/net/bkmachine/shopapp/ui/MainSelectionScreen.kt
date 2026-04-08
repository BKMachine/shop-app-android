package net.bkmachine.shopapp.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.bkmachine.shopapp.AppMode
import net.bkmachine.shopapp.MainViewModel
import net.bkmachine.shopapp.ImageUploaderActivity
import net.bkmachine.shopapp.ui.theme.ImageBluePrimary
import net.bkmachine.shopapp.ui.theme.PartGreenPrimary
import net.bkmachine.shopapp.ui.theme.ToolPurplePrimary

@Composable
fun MainSelectionScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Content Area
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Select Application",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )

            AppOptionRow(
                title = "Tool Stock",
                icon = Icons.Default.Build,
                color = ToolPurplePrimary,
                onClick = { viewModel.selectAppMode(AppMode.TOOLS, context) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AppOptionRow(
                title = "Part Stock",
                icon = Icons.Default.Category,
                color = PartGreenPrimary,
                onClick = { viewModel.selectAppMode(AppMode.PARTS, context) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AppOptionRow(
                title = "Image Uploader",
                icon = Icons.Default.CameraAlt,
                color = ImageBluePrimary,
                onClick = { 
                    val intent = Intent(context, ImageUploaderActivity::class.java)
                    context.startActivity(intent)
                }
            )
        }

        // Footer Area
        viewModel.confirmedDisplayName?.let { name ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Device: $name",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun AppOptionRow(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp) // Made slightly smaller to fit better without scrolling
            .clickable { onClick() },
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, color)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp) // Scaled icon with button
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = title,
                fontSize = 18.sp, // Slightly smaller text
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
