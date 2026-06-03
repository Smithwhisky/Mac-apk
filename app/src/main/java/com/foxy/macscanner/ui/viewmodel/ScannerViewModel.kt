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
                // 1. Discover valid portals endpoints very fast
                scanProgress("Checking active portal endpoints...")
                val checkedUrl = serverUrl.value.trim()
                val validPortals = mutableListOf<String>()
                
                // Scan paths in parallel
                val pathJobs = PortalConfig.payloads.map { path ->
                    async {
                        val fullPath = if (checkedUrl.endsWith("/")) "$checkedUrl${path.removePrefix("/")}" else "$checkedUrl$path"
                        if (repository.checkPortalEndpoint(fullPath)) {
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

                // 2. Multi-threaded MAC scanning loop
                val counter = AtomicInteger(0)
                
                // Creating a pool of 15 concurrent workers
                val workersCount = 15
                val channel = MacGenerator.generateRandomMacs(totalToScan) // Assume helper yields iterable or we loop

                val jobs = List(workersCount) {
                    launch {
                        while (isActive && counter.get() < totalToScan) {
                            val currentProgress = counter.incrementAndGet()
                            if (currentProgress > totalToScan) break

                            val targetMac = MacGenerator.generateSingleMac()
                            val randomUserAgent = PortalConfig.userAgents.random()
                            
                            // Log every 20 attempts to avoid UI lag
                            if (currentProgress % 20 == 0 || currentProgress == 1) {
                                withContext(Dispatchers.Main) {
                                    scanProgress("Scanned $currentProgress / $totalToScan MACs")
                                }
                            }

                            // Try hitting all discovered valid paths
                            for (portal in validPortals) {
                                val result = repository.scanMacAddress(portal, targetMac, randomUserAgent)
                                if (result != null && result.isValid) {
                                    withContext(Dispatchers.Main) {
                                        val newHit = MacHit(
                                            macAddress = targetMac,
                                            expiryDate = result.expiryDate ?: "Unlimited",
                                            maxConnections = result.maxConnections ?: "1",
                                            liveCount = result.liveCount ?: 0,
                                            vodCount = result.vodCount ?: 0,
                                            seriesCount = result.seriesCount ?: 0
                                        )
                                        activeHits.add(newHit)
                                        scanLogs.add(0, "🔥 [HIT] $targetMac | Exp: ${newHit.expiryDate}")
                                    }
                                    break // Stop checking other paths if one works
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
