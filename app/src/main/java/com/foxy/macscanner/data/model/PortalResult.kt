package com.foxy.macscanner.data.model

/**
 * يمثل نتيجة فحص مسار معين داخل السيرفر
 */
data class PortalResult(
    val baseUrl: String,       // الرابط الأساسي للسيرفر
    val path: String,          // المسار المفحوص (مثل /c/portal.php)
    val statusCode: Int,       // كود استجابة السيرفر (200, 401, 404, إلخ)
    val isWorking: Boolean     // هل المسار يعتبر بوابة صالحة للفحص (غالباً إذا كان الكود 200 أو 401)
)
