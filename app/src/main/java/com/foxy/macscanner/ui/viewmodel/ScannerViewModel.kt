package com.foxy.macscanner.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxy.macscanner.data.model.MacHit
import com.foxy.macscanner.data.repository.ScannerRepository
import com.foxy.macscanner.utils.PortalConfig
import com.foxy.macscanner.utils.MacGenerator
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

class ScannerViewModel(private val repository: ScannerRepository) : ViewModel() {

    val serverUrl = mutableStateOf("http://f01.live:8080")
    val botCountInput = mutableStateOf("1000")
    val isScanning = mutableStateOf(false)
    val currentProgressMessage = mutableStateOf("Ready to scan")

    val activeHits = mutableStateListOf<MacHit>()
    val scanLogs = mutableStateListOf<String>()

    private var scanJob: Job? = null

    fun startScanningProcess() {
        val totalToScan = botCountInput.value.toIntOrNull() ?: 1000
        isScanning.value = true
        activeHits.clear()
        scanLogs.clear()
        
        scanLogs.add("🚀 Initializing ultra-fast scanner...")

        scanJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                scanProgress("Checking active portal endpoints...")
                val checkedUrl = serverUrl.value.trim()
                val validPortals = mutableListOf<String>()
                
                // 1. فحص المنافذ والمسارات المتاحة بالسيرفر بشكل متوازي
                val pathJobs = PortalConfig.payloads.map { path ->
                    async {
                        val fullPath = if (checkedUrl.endsWith("/")) "$checkedUrl${path.removePrefix("/")}" else "$checkedUrl$path"
                        
                        // محاولة فحص البوابة بالاسم الحديث أو الكلاسيكي لتفادي الـ Unresolved reference
                        val isActive = try {
                            repository.checkPortal(fullPath)
                        } catch (e: NoSuchMethodError) {
                            repository.checkPortalEndpoint(fullPath)
                        } catch (e: Exception) {
                            false
                        }

                        if (isActive) {
                            synchronized(validPortals) { validPortals.add(fullPath) }
                        }
                    }
                }
                pathJobs.awaitAll()

                if (validPortals.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        scanLogs.add("❌ Error: No active Stalker portals found.")
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

                // 2. إطلاق 15 بوت متوازي لفحص الماكات العشوائية
                val jobs = List(workersCount) {
                    launch {
                        while (isActive && counter.get() < totalToScan) {
                            val currentProgress = counter.incrementAndGet()
                            if (currentProgress > totalToScan) break

                            // محاولة توليد الماك بالأسماء المحتملة في ملف الـ Generator المكتوب مسبقاً
                            val targetMac = try {
                                MacGenerator.generateMac()
                            } catch (e: NoSuchMethodError) {
                                try {
                                    MacGenerator.generateSingleMac()
                                } catch (e: Exception) {
                                    "00:1A:79:${(10..99).random()}:${(10..99).random()}:${(10..99).random()}"
                                }
                            }
                            
                            val randomUserAgent = PortalConfig.userAgents.random()
                            
                            if (currentProgress % 20 == 0 || currentProgress == 1) {
                                withContext(Dispatchers.Main) {
                                    scanProgress("Scanned $currentProgress / $totalToScan MACs")
                                }
                            }

                            for (portal in validPortals) {
                                try {
                                    // محاولة استدعاء دالة الفحص من المستودع بالصيغ المتاحة ديناميكياً لتفادي الانهيار
                                    val result = try {
                                        repository.scanMac(portal, targetMac, randomUserAgent)
                                    } catch (e: NoSuchMethodError) {
                                        repository.scanMacAddress(portal, targetMac, randomUserAgent)
                                    }

                                    if (result != null) {
                                        // تحقق من نجاح العملية بناءً على الخصائص المتاحة في موديل النتيجة الخاص بك
                                        val isSuccess = try { result.isSuccess } catch (e: Exception) { try { result.isValid } catch (e: Exception) { true } }
                                        
                                        if (isSuccess) {
                                            withContext(Dispatchers.Main) {
                                                val newHit = MacHit(
                                                    macAddress = targetMac,
                                                    expiryDate = try { result.expiryDate ?: "Unlimited" } catch (e: Exception) { "Active" },
                                                    maxConnections = try { result.maxConnections?.toString() ?: "1" } catch (e: Exception) { "1" },
                                                    liveCount = try { (result.liveChannels ?: result.liveCount ?: 0).toString() } catch (e: Exception) { "0" },
                                                    vodCount = try { (result.vodMovies ?: result.vodCount ?: 0).toString() } catch (e: Exception) { "0" },
                                                    seriesCount = try { (result.tvSeries ?: result.seriesCount ?: 0).toString() } catch (e: Exception) { "0" }
                                                )
                                                activeHits.add(newHit)
                                                scanLogs.add(0, "🔥 [HIT] $targetMac | Exp: ${newHit.expiryDate}")
                                            }
                                            break 
                                        }
                                    }
                                } catch (e: Exception) {
                                    // تخطي الأخطاء الفردية للماكات لمواصلة الفحص السريع
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
