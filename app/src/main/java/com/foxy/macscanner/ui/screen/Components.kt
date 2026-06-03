package com.foxy.macscanner.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foxy.macscanner.data.model.MacHit

/**
 * عنصر (Component) مستقل لعرض تفاصيل الـ MAC الشغال بشكل منظم
 */
@Composable
fun HitCardItem(hit: MacHit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // عرض الماك المكتشف باللون الأخضر المميز
            Text(
                text = "MAC: ${hit.macAddress}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(text = "تاريخ الانتهاء: ${hit.expiryDate}", color = Color.White, fontSize = 14.sp)
            Text(text = "الاتصالات المسموحة: ${hit.maxConnections}", color = Color.LightGray, fontSize = 13.sp)
            
            // الحل هنا: استبدال HorizontalDivider بـ Divider المتوافق 100% مع إصدار المكتبة الحالية
            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
            
            // سطر العدادات الخاصة بمحتوى السيرفر المستخرج بأمان
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "📺 قنوات: ${hit.liveCount}", color = Color(0xFF81D4FA), fontSize = 12.sp)
                Text(text = "🎬 أفلام: ${hit.vodCount}", color = Color(0xFFFFCC80), fontSize = 12.sp)
                Text(text = "🍿 مسلسلات: ${hit.seriesCount}", color = Color(0xFFA5D6A7), fontSize = 12.sp)
            }
        }
    }
}
