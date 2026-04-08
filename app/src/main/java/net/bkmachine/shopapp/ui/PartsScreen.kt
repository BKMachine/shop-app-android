package net.bkmachine.shopapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import net.bkmachine.shopapp.MainViewModel
import net.bkmachine.shopapp.data.remote.dto.PartResponse

@Composable
fun PartsScreen(viewModel: MainViewModel) {
    val part = viewModel.lastPart
    val isUpdating = viewModel.isUpdating
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (part == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isUpdating) {
                    Text(
                        text = "SCAN DETECTED",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Fetching Part Details...", style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(
                        text = "Ready to Scan",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Scan a part barcode to begin", color = Color.Gray)
                }
            }
        } else {
            PartDetailView(part, onUpdateStock = { amount ->
                viewModel.updatePartStock(amount.toString())
            }, viewModel = viewModel)
        }
    }
}

@Composable
fun PartDetailView(part: PartResponse, onUpdateStock: (Int) -> Unit, viewModel: MainViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var adjustmentAmount by remember { mutableStateOf("") }
    var isAbsoluteMode by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val imageUrl = viewModel.getImageUrl(part.img)
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Part Image",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 4.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 4.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No Image", color = Color.DarkGray)
            }
        }

        Text(
            text = part.part,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = part.description,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(label = "Current Stock", value = part.stock.toString(), isEmphasized = true)
                Spacer(modifier = Modifier.height(8.dp).fillMaxWidth().background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)).height(1.dp))
                DetailRow(label = "Location", value = "${part.location ?: "Unknown"} / ${part.position ?: "-"}")
                DetailRow(label = "Customer", value = part.customer?.name ?: "N/A")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text(
                "Change Stock",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = { viewModel.resetToDefault() }) {
            Text("Clear Scan", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = { 
                showDialog = false
                adjustmentAmount = ""
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .imePadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 40.dp)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Stock Adjustment", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Adjust", fontSize = 14.sp)
                            Switch(
                                checked = isAbsoluteMode,
                                onCheckedChange = { isAbsoluteMode = it },
                                modifier = Modifier.padding(horizontal = 8.dp).scale(0.8f)
                            )
                            Text("Set Total", fontSize = 14.sp)
                        }

                        val input = adjustmentAmount.toIntOrNull() ?: 0
                        val derivedStock = if (isAbsoluteMode) input else part.stock + input
                        
                        OutlinedTextField(
                            value = adjustmentAmount,
                            onValueChange = { adjustmentAmount = it },
                            label = { Text(if (isAbsoluteMode) "New Total" else "Adjustment (+/-)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = if (!isAbsoluteMode) {
                                {
                                    TextButton(
                                        onClick = {
                                            adjustmentAmount = if (adjustmentAmount.startsWith("-")) {
                                                adjustmentAmount.substring(1)
                                            } else {
                                                "-$adjustmentAmount"
                                            }
                                        },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Text("+/-", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            } else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            singleLine = true
                        )

                        Text(
                            text = "Resulting Stock: $derivedStock",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (derivedStock < 0) Color.Red else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { 
                                    showDialog = false 
                                    adjustmentAmount = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    val inputVal = adjustmentAmount.toIntOrNull() ?: 0
                                    val finalAmount = if (isAbsoluteMode) {
                                        inputVal - part.stock
                                    } else {
                                        inputVal
                                    }
                                    
                                    if (part.stock + finalAmount >= 0) {
                                        onUpdateStock(finalAmount)
                                        showDialog = false
                                        adjustmentAmount = ""
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = derivedStock >= 0
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isEmphasized: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            fontWeight = if (isEmphasized) FontWeight.Bold else FontWeight.Medium,
            fontSize = if (isEmphasized) 24.sp else if (label == "Location") 14.sp else 16.sp,
            color = if (isEmphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        )
    }
}
