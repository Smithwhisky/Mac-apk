package com.foxy.macscanner.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foxy.macscanner.ui.viewmodel.ScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ScannerViewModel) {
    val backgroundColor = Color(0xFF0D0E11) 
    val cardColor = Color(0xFF161920)       
    val primaryColor = Color(0xFF00FF66)    
    val secondaryColor = Color(0xFFFF3366)  

    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FOXY_MAC_SCANNER",
                    color = primaryColor,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "v3.9 // Multi-Threading Core",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (viewModel.isScanning.value) {
                CircularProgressIndicator(
                    color = primaryColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(4.dp), 
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                
                OutlinedTextField(
                    value = viewModel.serverUrl.value,
                    onValueChange = { viewModel.serverUrl.value = it },
                    label = { Text("TARGET PORTAL URL", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    enabled = !viewModel.isScanning.value,
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = primaryColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = viewModel.botCountInput.value,
                    onValueChange = { viewModel.botCountInput.value = it },
                    label = { Text("TOTAL GENERATIONS", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !viewModel.isScanning.value,
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = primaryColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (!viewModel.isScanning.value) {
                    Button(
                        onClick = { viewModel.startScanningProcess() },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "LAUNCH DISPATCHER (15 BOTS)", 
                            color = Color.Black, 
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.stopScanning() },
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ABORT SCAN PARALLEL", 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "STATUS // ${viewModel.currentProgressMessage.value.uppercase()}",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        if (viewModel.isScanning.value) {
            LinearProgressIndicator(
                color = primaryColor,
                trackColor = Color(0xFF161920), // تم إصلاح الخطأ المطبعي هنا بنجاح
                modifier = Modifier.fillMaxWidth().height(2.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = cardColor,
            contentColor = primaryColor,
            modifier = Modifier.border(1.dp, Color.DarkGray)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { 
                    Text(
                        text = "LIVE HITS (${viewModel.activeHits.size})", 
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ) 
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { 
                    Text(
                        text = "CONSOLE LOGS", 
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ) 
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF050608))
                .border(1.dp, Color(0xFF1F232D))
                .padding(8.dp)
        ) {
            if (selectedTab == 0) {
                if (viewModel.activeHits.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("NO VALID CODES FOUND YET", color = Color.DarkGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.activeHits) { hit ->
                        HitCardItem(hit = hit)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.scanLogs) { log ->
                        Text(
                            text = log,
                            color = if (log.contains("🔥") || log.contains("🟢")) primaryColor else Color.Gray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
