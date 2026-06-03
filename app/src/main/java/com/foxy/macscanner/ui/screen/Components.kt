package com.foxy.macscanner.ui.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foxy.macscanner.data.model.MacHit

@Composable
fun HitCardItem(hit: MacHit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11141A)),
        shape = RoundedCornerShape(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFF00FF66).copy(alpha = 0.3f), RoundedCornerShape(2.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "MAC: ${hit.macAddress}",
                color = Color(0xFF00FF66),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(text = "EXPIRY : ${hit.expiryDate.uppercase()}", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            Text(text = "MAX_CONN: ${hit.maxConnections}", color = Color.LightGray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            
            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 6.dp),  thickness = 0.5.dp)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "CH: ${hit.liveCount}", color = Color(0xFF81D4FA), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text(text = "VOD: ${hit.vodCount}", color = Color(0xFFFFCC80), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text(text = "SERIES: ${hit.seriesCount}", color = Color(0xFFA5D6A7), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
    }
}
