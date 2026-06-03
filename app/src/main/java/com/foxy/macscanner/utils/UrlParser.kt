package com.foxy.macscanner.utils

import java.net.URL

object UrlParser {

    /**
     * تنظيف الرابط واستخراج النطاق والمنفذ بشكل آمن (مثل دالة clear_domain في بايثون)
     * لتفادي تجمد التطبيق أو الـ Crash في حال إدخال رابط مشوه.
     */
    fun clearDomain(rawUrl: String?): String {
        if (rawUrl.isNullOrBlank()) return ""
        
        // إزالة المسافات والرموز الزائدة في البداية والنهاية
        var cleanUrl = rawUrl.trim().replace(" ", "")

        // التأكد من وجود بروتوكول للـ URL لتتمكن مكتبة جافا من قراءته
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "http://$cleanUrl"
        }

        return try {
            val parsedUrl = URL(cleanUrl)
            val host = parsedUrl.host
            
            // إذا لم يحدد المستخدم منفذ (Port)، نستخدم 80 كوضع افتراضي
            val port = if (parsedUrl.port == -1) 80 else parsedUrl.port
            
            "$host:$port"
        } catch (e: Exception) {
            // تفاصيل صغيرة: بدلاً من انهيار التطبيق عند إدخال رابط خاطئ، نرجع نصاً فارغاً لمعالجته
            ""
        }
    }

    /**
     * بناء الرابط الكامل لطلب الـ API بناءً على النطاق النظيف والمسار المحدد
     */
    fun buildFullUrl(domain: String, path: String): String {
        val base = if (domain.startsWith("http://") || domain.startsWith("https://")) domain else "http://$domain"
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$base$cleanPath"
    }
}
