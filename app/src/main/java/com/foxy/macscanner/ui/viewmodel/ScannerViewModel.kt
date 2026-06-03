package com.foxy.macscanner.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foxy.macscanner.data.model.MacHit
import com.foxy.macscanner.data.model.PortalResult
import com.foxy.macscanner.data.repository.ScannerRepository
import com.foxy.macscanner.utils.MacGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ScannerViewModel(private val repository: ScannerRepository) : ViewModel() {

    // متغيرات الحالة لمراقبة مدخلات المستخدم وعملية الفحص في الواجهة (UI States)
    var serverUrl = mutableStateOf("http://f01.live:8080")
    var botCountInput = mutableStateOf("100") // عدد الماكات المطلوب توليدها وفحصها
    var isScanning = mutableStateOf(false)
    var currentProgressMessage = mutableStateOf("جاهز لبدء الفحص")

    // قوائم مراقبة النتائج (Thread-Safe بمساعدة Mutex عند تحديثها من خيوط مختلفة)
    val foundPortals = mutableStateListOf<PortalResult>()
    val activeHits = mutableStateListOf<MacHit>()
    val scanLogs = mutableStateListOf<String>()

    // أداة القفل المتبادل (Mutex) لتجنب خطأ ConcurrentModificationException الشهير في أندرويد
    private val mutex = Mutex()

    /**
     * الدالة الرئيسية لبدء العملية الكاملة (فحص البوابات أولاً ثم فحص الـ MACs)
     */
    fun startScanningProcess() {
        // تفاصيل صغيرة: منع المستخدم من إطلاق فحصين في نفس الوقت
        if (isScanning.value) return

        viewModelScope.launch(Dispatchers.Main) {
            isScanning.value = true
            foundPortals.clear()
            activeHits.clear()
            scanLogs.clear()
            
            val totalBots = botCountInput.value.toIntOrNull() ?: 50
            scanLogs.add("⏳ جاري فحص مسارات البوابات المحتملة للسيرفر...")

            // 1. فحص البوابات المتاحة على السيرفر
            val portals = repository.checkServerPortals(serverUrl.value)
            foundPortals.addAll(portals)

            val activePortals = portals.filter { it.isWorking }
            
            if (activePortals.isEmpty()) {
                scanLogs.add("❌ لم يتم العثور على بوابات Stalker نشطة على هذا السيرفر.")
                isScanning.value = false
                return@launch
            }

            scanLogs.add("🟢 تم العثور على (${activePortals.size}) بوابة مفتوحة! بدء توليد وفحص الـ MACs...")
            
            // 2. توليد قائمة الـ MAC Addresses العشوائية بناءً على طلب المستخدم
            val macList = MacGenerator.generateMacList(totalBots)
            
            // 3. إطلاق الـ Bots (التوازي المتعدد بحد أقصى 15 بوت متزامن كما في بايثون)
            // سنقوم بتقسيم الماكات إلى مجموعات (Chunks) بحجم 15 لتشغيل 15 Coroutine معاً
            val chunks = macList.chunked(15)
            
            var checkedCount = 0

            for (chunk in chunks) {
                if (!isScanning.value) break // إمكانية إيقاف الفحص يدوياً

                // تشغيل الـ 15 بوت بالتوازي عبر async
                val deferredJobs = chunk.map { mac ->
                    async(Dispatchers.IO) {
                        // فحص الماك الحالي على أول بوابة نشطة تم العثور عليها
                        val targetPortal = activePortals.first()
                        val hitResult = repository.checkSingleMac(targetPortal.baseUrl, targetPortal.path, mac)
                        
                        // تحديث الواجهة بشكل آمن باستخدام الـ Mutex
                        mutex.withLock {
                            checkedCount++
                            currentProgressMessage.value = "تم فحص $checkedCount من أصل $totalBots"
                            
                            if (hitResult != null) {
                                activeHits.add(hitResult)
                                scanLogs.add("🔥 HIT عثر على ماك شغال: ${hitResult.macAddress}")
                            }
                        }
                    }
                }
                // الانتظار حتى تنتهي المجموعة الحالية (15 بوت) قبل الانتقال للمجموعة التالية
                deferredJobs.awaitAll()
            }

            scanLogs.add("✅ اكتملت عملية الفحص بالكامل.")
            isScanning.value = false
        }
    }

    /**
     * إمكانية إيقاف الفحص يدوياً من قبل المستخدم
     */
    fun stopScanning() {
        isScanning.value = false
        currentProgressMessage.value = "تم إيقاف الفحص يدوياً"
        scanLogs.add("🛑 تم إيقاف عملية الفحص بطلب من المستخدم.")
    }
}
