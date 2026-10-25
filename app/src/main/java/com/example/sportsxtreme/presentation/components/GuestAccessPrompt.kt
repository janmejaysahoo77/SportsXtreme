package com.example.sportsxtreme.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsxtreme.R

@Composable
fun GuestAccessCard(
    title: String,
    detail: String,
    onSignup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .background(Color(0xFF101A24), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x334DE9FF), RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painterResource(R.drawable.whiteuser), "Guest account", Modifier.size(72.dp))
        Spacer(Modifier.height(10.dp))
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(detail, color = Color(0xFFADB9C0), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 5.dp))
        Button(
            onClick = onSignup,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8FF00), contentColor = Color(0xFF172000)),
            modifier = Modifier.padding(top = 13.dp)
        ) { Text("Login / Sign up", fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun GuestAuthDialog(
    detail: String,
    onDismiss: () -> Unit,
    onSignup: () -> Unit,
    onLogin: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(1.dp, Color(0xFF26394B), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        title = null,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(Color(0x30C1FF00), Color.Transparent),
                            center = Offset(size.width / 2f, 0f),
                            radius = size.height * 1.35f
                        )
                    )
                }
            ) {
                Text("Account required", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Image(painterResource(R.drawable.whiteuser), "Guest account", Modifier.padding(top = 8.dp).size(68.dp))
                Text(detail, color = Color(0xFFD8E1EB), fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp))
            }
        },
        confirmButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onLogin != null) TextButton(onClick = onLogin) { Text("Login", color = Color(0xFFC1FF00)) }
                TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFFC1FF00)) }
                TextButton(onClick = onSignup) { Text("Sign up", color = Color(0xFFC1FF00)) }
            }
        },
        containerColor = Color(0xFF0B121E),
        textContentColor = Color.White
    )
}
