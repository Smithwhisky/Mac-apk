package com.foxy.macscanner.utils

import kotlin.random.Random

object MacGenerator {

    // النطاقات الشهيرة الخاصة بأجهزة الـ IPTV الاسترجاعية (تسمى OUI Prefixes)
    private val macPrefixes = listOf(
        "00:1A:79", // النطاق الأكثر شهرة عالمياً لبوابات Stalker
        "00:1A:78",
        "00:1A:7A",
        "88:A3:33",
        "00:02:02"
    )

    /**
     * توليد عنوان MAC عشوائي كامل يبدأ بأحد النطاقات المعتمدة
     */
    fun generateRandomMac(): String {
        // اختيار بادئة عشوائية من القائمة
        val prefix = macPrefixes.random()
        
        // توليد الـ 3 أجزاء المتبقية (كل جزء يتكون من رقمين سداسي عشر من 00 إلى FF)
        val part4 = String.format("%02X", Random.nextInt(0, 256))
        val part5 = String.format("%02X", Random.nextInt(0, 256))
        val part6 = String.format("%02X", Random.nextInt(0, 256))
        
        return "$prefix:$part4:$part5:$part6"
    }

    /**
     * توليد قائمة من عناوين الـ MAC العشوائية بناءً على عدد البوتات المطلوب فحصها
     */
    fun generateMacList(count: Int): List<String> {
        val list = mutableListOf<String>()
        repeat(count) {
            list.add(generateRandomMac())
        }
        return list
    }
}
