package com.foxy.macscanner.data.remote

import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class StalkerApiService(private val client: OkHttpClient) {

    // قائمة الـ User-Agents العشوائية لمحاكاة الأجهزة المختلفة وتجنب الحظر من جدران الحماية
    private val userAgents = listOf(
        "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stalker/",
        "Mozilla/5.0 (Link; Linux x86_64; U; Baby) AppleWebKit/537.36 (KHTML, like Gecko) Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    )

    /**
     * توليد الهيدرز اللازمة لكل طلب بناءً على الماك والتوكن كما في دالة hea2 في بايثون
     */
    private fun createHeaders(mac: String, token: String? = null): Headers {
        val builder = Headers.Builder()
            .add("User-Agent", userAgents.random())
            .add("Accept", "application/json")
            .add("X-User-Timezone", "Europe/Berlin")
            
        // إضافة الماك في الكوكيز وهو أمر إلزامي لبوابات Stalker
        builder.add("Cookie", "mac=${mac.replace(":", "%3A")};")
        
        // إذا توفر التوكن بعد الـ Handshake نقوم بإضافته في الـ Authorization Header
        if (!token.isNullOrBlank()) {
            builder.add("Authorization", "Bearer $token")
        }
        
        return builder.build()
    }

    /**
     * محاكاة لعملية الفحص الأولي للمسار (تحديد كود الحالة 200 أو 401)
     */
    fun checkPortalPath(fullUrl: String): Int {
        val request = Request.Builder()
            .url(fullUrl)
            .headers(createHeaders(""))
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response -> response.code }
        } catch (e: IOException) {
            -1 // تفاصيل صغيرة: نرجع -1 بدلاً من إلقاء خطأ يتسبب في إغلاق التطبيق
        }
    }

    /**
     * تنفيذ طلب الـ Handshake لجلب الـ Token (تحاكي url5 في بايثون)
     */
    fun executeHandshake(baseUrl: String, path: String, mac: String): Response? {
        val fullUrl = "$baseUrl$path?type=stb&action=handshake"
        val request = Request.Builder()
            .url(fullUrl)
            .headers(createHeaders(mac))
            .get()
            .build()

        return try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            null
        }
    }

    /**
     * جلب بيانات الملف الشخصي والصلاحية (تحاكي url6 في بايثون لجلب تاريخ الانتهاء)
     */
    fun getProfile(baseUrl: String, path: String, mac: String, token: String): Response? {
        val fullUrl = "$baseUrl$path?type=stb&action=get_profile"
        val request = Request.Builder()
            .url(fullUrl)
            .headers(createHeaders(mac, token))
            .get()
            .build()

        return try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            null
        }
    }

    /**
     * جلب عدادات المحتوى (المباشر، الأفلام، المسلسلات) تحاكي الطلبات المتتالية في بايثون
     */
    fun getContentCount(baseUrl: String, path: String, mac: String, token: String, contentType: String): Response? {
        // contentType قد تكون 'itv' للقنوات، 'vod' للأفلام، أو 'series' للمسلسلات
        val fullUrl = "$baseUrl$path?type=stb&action=get_ordered_list&row_start=0&count=1&itv_id=0&video_id=0&p=0&sortby=name&hd=0&fav=0&ch_link=0&cmd=&movie_id=0&season_id=0&episode_id=0"
        val request = Request.Builder()
            .url(fullUrl)
            .headers(createHeaders(mac, token))
            .get()
            .build()

        return try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            null
        }
    }
}
