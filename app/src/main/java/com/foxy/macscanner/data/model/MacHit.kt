package com.foxy.macscanner.data.model

/**
 * يمثل كائن الحساب الشغال (Hit) مع كامل تفاصيله المستخرجة من السيرفر
 */
data class MacHit(
    val macAddress: String,                // عنوان الـ MAC الذي تم اختباره بنجاح
    val portalUrl: String,                 // رابط البوابة الشغالة
    val expiryDate: String = "N/A",        // تاريخ انتهاء الاشتراك (trh في السكربت الأصلي)
    val maxConnections: String = "N/A",    // أقصى عدد اتصالات مسموحة (real)
    val m3uLink: String = "",              // رابط الـ M3U الخاص بالماك إذا تم توليده
    val statusMessage: String = "Active",   // رسالة الحالة (مثل حظر vpn أو منتهي)
    
    // تفاصيل العدادات (التفاصيل الصغيرة: نضع قيم افتراضية "0" لتجنب الكراش إذا لم يرسلها السيرفر)
    val liveCount: String = "0",           // عدد قنوات البث المباشر
    val vodCount: String = "0",            // عدد الأفلام
    val seriesCount: String = "0"          // عدد المسلسلات
)
