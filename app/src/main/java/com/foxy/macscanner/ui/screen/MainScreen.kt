package com.foxy.macscanner.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foxy.macscanner.ui.viewmodel.ScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ScannerViewModel) {
    // جلب الألوان المركزية من ثيم النظام الافتراضي للتطبيق
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.error

    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // العنوان العلوي للتطبيق بطابع احترافي
        Text(
            text = "FoxyMacScan Pro Android",
            color = primaryColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // لوحة التحكم والمدخلات (رابط السيرفر والعداد)
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // حقل إدخال الرابط الأساسي للسيرفر المفحوص
                OutlinedTextField(
                    value = viewModel.serverUrl.value,
                    onValueChange = { viewModel.serverUrl.value = it },
                    label = { Text("رابط سيرفر الاستضافة (Portal URL)", color = Color.Gray) },
                    enabled = !viewModel.isScanning.value,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // حقل إدخال عدد الماكات العشوائية المستهدف فحصها
                OutlinedTextField(
                    value = viewModel.botCountInput.value,
                    onValueChange = { viewModel.botCountInput.value = it },
                    label = { Text("عدد الحسابات المطلوب توليدها وفحصها", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !viewModel.isScanning.value,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // زر التحكم الذكي (تشغيل 15 بوت متوازي أو إيقاف العملية)
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (!viewModel.isScanning.value) {
                        Button(
                            onClick = { viewModel.startScanningProcess() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("بدء الفحص المتوازي (15 Bots)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.stopScanning() },
                            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إيقاف الفحص يدوياً", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // مؤشر تقدم الفحص المباشر (Progress Indicator)
        Text(
            text = viewModel.currentProgressMessage.value,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        if (viewModel.isScanning.value) {
            LinearProgressIndicator(
                color = primaryColor,
                trackColor = Color.DarkGray,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // شريط التبويبات العلوي للنتائج والسجلات (Tabs)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = cardColor,
            contentColor = primaryColor
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("الشغالة الحالية (${viewModel.activeHits.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("سجل الفحص والـ Logs", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // نافذة العرض الديناميكية للمحتوى بناءً على التبويب النشط
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f)
        ) {
            if (selectedTab == 0) {
                // تبويب عرض بطاقات الحسابات الناجحة بالتفصيل
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.activeHits) { hit ->
                        // استدعاء المكون المستقل المعزول برمجياً لتجنب تكرار الأكواد
                        HitCardItem(hit = hit)
                    }
                }
            } else {
                // تبويب السجلات والتقارير الحية الناتجة من السيرفر وعمليات الشبكة
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.scanLogs) { log ->
                        Text(
                            text = log,
                            color = if (log.contains("🔥") || log.contains("🟢")) primaryColor else Color.LightGray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
