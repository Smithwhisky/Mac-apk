package com.foxy.macscanner.data.remote

import okhttp3.CookieJar
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object NetworkClient {

    /**
     * بناء عميل OkHttpClient مخصص يتجاهل حماية SSL لضمان عدم انهيار التطبيق
     * مع تفعيل ميزة حفظ الكوكيز لإبقاء الجلسة نشطة (Session Persistence)
     */
    fun createUnsafeClient(): OkHttpClient {
        return try {
            // تفاصيل صغيرة: إنشاء مديير ثقة (Trust Manager) يتجاهل فحص الشهادات تماماً كـ verify=False في بايثون
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            // إعداد بروتوكول الـ SSL
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true } // الموافقة على جميع أسماء النطاقات حتى لو كانت غير آمنة
                .connectTimeout(5, TimeUnit.SECONDS) // تحديد وقت محدد لمحاولة الاتصال لمنع تجمد الـ Thread
                .readTimeout(5, TimeUnit.SECONDS)
                .cookieJar(CookieJar.NO_COOKIES) // في بوابات Stalker نقوم بإرسال الكوكيز يدوياً في الهيدر أحياناً، وسنترك خيار التخصيص مرناً
                .followRedirects(false) // منع التوجيه التلقائي للحفاظ على دقة كود الحالة (401 أو 200)
                .build()
        } catch (e: Exception) {
            // في حال حدوث خطأ غير متوقع، نعود بعميل افتراضي عادي لحماية التطبيق من الـ Crash
            OkHttpClient.Builder().build()
        }
    }
}
