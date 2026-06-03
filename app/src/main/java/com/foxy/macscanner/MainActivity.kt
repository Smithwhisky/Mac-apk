package com.foxy.macscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.foxy.macscanner.data.remote.NetworkClient
import com.foxy.macscanner.data.remote.StalkerApiService
import com.foxy.macscanner.data.repository.ScannerRepository
import com.foxy.macscanner.ui.screen.MainScreen
import com.foxy.macscanner.ui.viewmodel.ScannerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. إنشاء عميل الشبكة المخصص الذي يتخطى الـ SSL ويحاكي بايثون
        val unsafeHttpClient = NetworkClient.createUnsafeClient()
        val apiService = StalkerApiService(unsafeHttpClient)
        
        // 2. تمرير الخدمة إلى المستودع (Repository) ليكون العقل المدبر للعمليات
        val repository = ScannerRepository(apiService)
        
        // 3. إنشاء الـ ViewModel المسؤول عن إدارة الـ 15 بوت (Coroutines) والـ State
        val viewModel = ScannerViewModel(repository)

        setContent {
            MacScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 4. تشغيل الواجهة الرسومية وتمرير الـ ViewModel لها
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
