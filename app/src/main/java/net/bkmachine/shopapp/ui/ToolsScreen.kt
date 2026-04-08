package net.bkmachine.shopapp.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.bkmachine.shopapp.MainViewModel
import net.bkmachine.shopapp.NavigationTabs
import net.bkmachine.shopapp.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ToolsScreen(viewModel: MainViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = viewModel.toolHeaderText,
            textAlign = TextAlign.Center,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )
        if (!viewModel.showToolStockTextField || viewModel.toolHeaderText != "Re-Stock") {
            Text(
                text = viewModel.userMessage,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            val focusRequester = remember { FocusRequester() }
            if (viewModel.showToolStockTextField && viewModel.toolHeaderText == "Re-Stock") {
                LaunchedEffect(viewModel.showToolStockTextField) {
                    if (viewModel.showToolStockTextField) {
                        try {
                            focusRequester.requestFocus()
                        } catch (e: Exception) {
                            Log.e("ToolsScreen", "Focus request failed", e)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(0.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CompositionLocalProvider(
                        LocalTextInputService provides null
                    ) {
                        Text(
                            text = viewModel.userMessage,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                        TextField(
                            value = viewModel.toolTextField,
                            label = {
                                Text(
                                    text = "Stock Adjustment Amount",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .focusRequester(focusRequester)
                                .onKeyEvent {
                                    if (it.nativeKeyEvent.keyCode == 66) {
                                        viewModel.updateToolStock(viewModel.toolTextField.text)
                                    }
                                    false
                                },
                            onValueChange = {
                                viewModel.updateToolTextField(it)
                            })
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Button(
                            onClick = {
                                if (viewModel.toolTextField.text.isBlank()) return@Button
                                val num = viewModel.toolTextField.text.toIntOrNull()
                                if (num == null) {
                                    // Handle non-number if needed, though MainViewModel might handle it
                                    return@Button
                                }
                                if (num == 0) return@Button
                                val text = (-num).toString()
                                viewModel.updateToolTextField(androidx.compose.ui.text.input.TextFieldValue(text))
                                focusRequester.requestFocus()
                            },
                            modifier = Modifier
                        ) {
                            Text(text = "+/-", modifier = Modifier)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { viewModel.updateToolStock(viewModel.toolTextField.text) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Enter")
                        }
                    }
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.bk_logo),
                    contentDescription = "BK Machine Logo",
                    modifier = Modifier.fillMaxWidth(0.6f)
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text(
                    text = viewModel.resultMessage,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(y = (-8).dp)
                )
            }
            NavigationTabs()
        }
    }
}
