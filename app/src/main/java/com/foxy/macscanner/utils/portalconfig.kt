package com.foxy.macscanner.utils

object PortalConfig {

    // قائمة المسارات الأكثر شيوعاً لبوابات Stalker Middleware
    val payloads = listOf(
        "/portal.php",
        "/server/load.php",
        "/stalker_portal/server/load.php",
        "/c/portal.php",
        "/c/server/load.php",
        "/ministra/portal.php",
        "/magic/portal.php",
        "/client/index.php"
    )

    // الـ User-Agents المستعملة لمحاكاة أجهزة الـ MAG Boxes ومختلف البيئات وتجنب الحظر
    val userAgents = listOf(
        "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 (KHTML, like Gecko) MAG200 stalker/",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Linux; Android 11; MAG520) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/83.0.4103.106",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Safari/605.1.15",
        "Dalvik/2.1.0 (Linux; U; Android 10; TV-Box Build/QQ3A.200805.001)"
    )
}
