package com.foxy.macscanner.data.repository

import com.foxy.macscanner.data.model.MacHit
import com.foxy.macscanner.data.model.PortalResult
import com.foxy.macscanner.data.remote.StalkerApiService
import com.foxy.macscanner.utils.PortalConfig
import com.foxy.macscanner.utils.UrlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ScannerRepository(private val apiService: StalkerApiService) {

    /**
     * فحص جميع المسارات المحتملة للبوابة على سيرفر معين (تحاكي searchpanel في بايثون)
     */
    suspend fun checkServerPortals(rawUrl: String): List<PortalResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<PortalResult>()
        val cleanDomain = UrlParser.clearDomain(rawUrl)
        
        if (cleanDomain.isBlank()) return@withContext results

        // المرور على قائمة المسارات المخزنة في الـ Config
        for (path in PortalConfig.payloads) {
            val fullUrl = UrlParser.buildFullUrl(cleanDomain, path)
            val code = apiService.checkPortalPath(fullUrl)
            
            // البوابة تعتبر صالحة ومفتوحة للفحص إذا أعادت كود 200 أو 401 (طلب مصادقة)
            val isWorking = (code == 200 || code == 401 || code == 512)
            
            results.add(
                PortalResult(
                    baseUrl = "http://$cleanDomain",
                    path = path,
                    statusCode = code,
                    isWorking = isWorking
                )
            )
        }
        return@withContext results
    }

    /**
     * فحص عنوان MAC معين على بوابة محددة واستخراج كامل بيانات الحساب (تاريخ الانتهاء والعدادات)
     */
    suspend fun checkSingleMac(baseUrl: String, path: String, mac: String): MacHit? = withContext(Dispatchers.IO) {
        try {
            // 1. خطوة الـ Handshake لجلب الجلسة والتوكن (تحاكي جلب token في بايثون)
            val handshakeResponse = apiService.executeHandshake(baseUrl, path, mac) ?: return@withContext null
            if (handshakeResponse.code != 200) return@withContext null
            
            val handshakeBody = handshakeResponse.body?.string() ?: return@withContext null
            val handshakeJson = JSONObject(handshakeBody)
            
            // تفاصيل صغيرة: التحقق الآمن من وجود الـ token داخل الـ JSON لمنع الكراش
            val jsData = handshakeJson.optJSONObject("js") ?: return@withContext null
            val token = jsData.optString("token", "")
            if (token.isBlank()) return@withContext null

            // 2. خطوة جلب الملف الشخصي (get_profile) لاستخراج تاريخ الانتهاء (trh) والروابط
            val profileResponse = apiService.getProfile(baseUrl, path, mac, token) ?: return@withContext null
            val profileBody = profileResponse.body?.string() ?: return@withContext null
            val profileJson = JSONObject(profileBody)
            val profileData = profileJson.optJSONObject("js")

            var expiryDate = "Unlimited / No Expiry"
            var maxConnections = "1"

            if (profileData != null) {
                // استخراج تاريخ انتهاء الصلاحية
                if (profileData.has("keep_alive_js") && !profileData.isNull("keep_alive_js")) {
                    expiryDate = profileData.optString("keep_alive_js", "N/A")
                }
                // استخراج الحد الأقصى للاتصالات المتزامنة
                maxConnections = profileData.optString("max_connections", "1")
            }

            // 3. جلب العدادات (المباشر، الأفلام، المسلسلات) عبر استجواب الـ ordered_list آمن برمجياً
            val liveCount = fetchCountForType(baseUrl, path, mac, token, "itv")
            val vodCount = fetchCountForType(baseUrl, path, mac, token, "vod")
            val seriesCount = fetchCountForType(baseUrl, path, mac, token, "series")

            // بناء رابط الـ M3U الافتراضي للحساب الشغال كما كان يفعل السكربت
            val m3uLink = "$baseUrl$path?type=stb&action=get_ordered_list&api_auth=Bearer $token"

            // العودة بكائن النتيجة المكتمل
            return@withContext MacHit(
                macAddress = mac,
                portalUrl = "$baseUrl$path",
                expiryDate = expiryDate,
                maxConnections = maxConnections,
                m3uLink = m3uLink,
                liveCount = liveCount,
                vodCount = vodCount,
                seriesCount = seriesCount
            )

        } catch (e: Exception) {
            // تفاصيل صغيرة: أي خطأ في التحليل الداخلي للـ JSON لا يعطل الفحص، بل يتجاوزه بأمان
            return@withContext null
        }
    }

    /**
     * دالة مساعدة داخلية لطلب العدادات وتفكيك حقل الـ total_items من الـ JSON آلياً
     */
    private fun fetchCountForType(baseUrl: String, path: String, mac: String, token: String, type: String): String {
        return try {
            val response = apiService.getContentCount(baseUrl, path, mac, token, type)
            val body = response?.body?.string() ?: return "0"
            val json = JSONObject(body)
            val jsObj = json.optJSONObject("js")
            
            // في بوابات Stalker، الحقل المسؤول عن العدد الإجمالي هو total_items
            if (jsObj != null && jsObj.has("total_items")) {
                jsObj.optInt("total_items", 0).toString()
            } else {
                "0"
            }
        } catch (e: Exception) {
            "0"
        }
    }
}
