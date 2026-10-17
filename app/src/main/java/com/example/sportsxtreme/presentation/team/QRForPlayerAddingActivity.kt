package com.example.sportsxtreme.presentation.team

import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.tournament.*
import com.example.sportsxtreme.presentation.components.*
import com.example.sportsxtreme.presentation.auth.*
import com.example.sportsxtreme.presentation.scoring.*
import com.example.sportsxtreme.presentation.match.*
import com.example.sportsxtreme.presentation.media.*
import com.example.sportsxtreme.presentation.home.*
import com.example.sportsxtreme.presentation.team.*
import com.example.sportsxtreme.presentation.profile.*
import com.example.sportsxtreme.presentation.store.*
import android.os.Bundle
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.presentation.team.ManagePlayersInsideTeamActivity
import com.google.firebase.functions.FirebaseFunctions
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QRForPlayerAddingActivity : ComponentActivity() {
    @Inject lateinit var functions: FirebaseFunctions
    private var invitationUrl by mutableStateOf<String?>(null)
    private var loadingInvite by mutableStateOf(true)
    private var inviteError by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val teamId = intent.getStringExtra(ManagePlayersInsideTeamActivity.EXTRA_TEAM_ID).orEmpty()
        val teamName = intent.getStringExtra(ManagePlayersInsideTeamActivity.EXTRA_TEAM_NAME).orEmpty().ifBlank { "Your team" }
        setContent {
            QRForPlayerAddingScreen(
                teamName = teamName,
                invitationUrl = invitationUrl,
                loading = loadingInvite,
                error = inviteError,
                onClose = { finish() },
                onShare = ::shareInvite
            )
        }
        if (teamId.isBlank()) {
            loadingInvite = false
            inviteError = "Team information is missing. Reopen this page and try again."
        } else {
            functions.getHttpsCallable("createTeamInvite")
                .call(mapOf("teamId" to teamId))
                .addOnSuccessListener { result ->
                    invitationUrl = (result.data as? Map<*, *>)?.get("invitationUrl") as? String
                    if (invitationUrl.isNullOrBlank()) {
                        inviteError = "Could not create a team invitation. Please try again."
                    }
                    loadingInvite = false
                }
                .addOnFailureListener { error ->
                    inviteError = error.message ?: "Unable to create a team invitation."
                    loadingInvite = false
                }
        }
    }

    private fun shareInvite() {
        val url = invitationUrl ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Join $url")
        }
        startActivity(Intent.createChooser(shareIntent, "Invite players to your team"))
    }
}

private val QrAddBg = Color(0xFF020A15)
private val QrAddAccent = Color(0xFFC1FF00)
private val QrAddTextMuted = Color(0xFFAAAAC6)
private val QrAddPanel = Color(0xFF0A1322)

@Composable
private fun QRForPlayerAddingScreen(
    teamName: String,
    invitationUrl: String?,
    loading: Boolean,
    error: String?,
    onClose: () -> Unit,
    onShare: () -> Unit
) {
    val qrBitmap = remember(invitationUrl) { invitationUrl?.let(::createInviteQrBitmap) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QrAddBg)
            .drawBehind {
                drawCircle(Color(0x162F7E00), radius = size.width * 0.72f, center = Offset(size.width * 0.5f, size.height * 0.72f))
                drawCircle(Color(0x0D2C90FF), radius = size.width * 0.42f, center = Offset(size.width * 0.52f, size.height * 0.43f))
            }
            .padding(horizontal = 13.dp)
    ) {
        QRTopActions(onClose, onShare)
        TeamIdentity(teamName, Modifier.padding(top = 22.dp))
        QRCodeFrame(teamName, Modifier.padding(top = 32.dp), qrBitmap, loading, error)
        InstructionText(teamName, Modifier.padding(top = 25.dp))
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .height(54.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (invitationUrl == null) QrAddPanel else QrAddAccent)
                .clickable(enabled = invitationUrl != null, onClick = onShare),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (loading) "Preparing invite QR…" else if (invitationUrl != null) "Share this code" else "Invite unavailable",
                color = if (invitationUrl != null) Color(0xFF111604) else QrAddTextMuted,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun QRTopActions(onClose: () -> Unit, onShare: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(top = 27.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CloseIcon(Modifier.size(35.dp).clickable(onClick = onClose).padding(8.dp), Color.White)
        Spacer(Modifier.weight(1f))
        DownloadIcon(Modifier.size(35.dp).padding(8.dp), Color.White)
        ShareNetworkIcon(Modifier.padding(start = 13.dp).size(35.dp).clickable(onClick = onShare).padding(8.dp), Color.White)
    }
}

@Composable
private fun TeamIdentity(teamName: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(99.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .border(4.dp, Color(0xFF202839), CircleShape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.finalicon),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
        Text(teamName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 21.dp), maxLines = 1)
        Text("TEAM INVITATION", color = Color(0xFF7F789B), fontSize = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.5.sp, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun QRCodeFrame(teamName: String, modifier: Modifier = Modifier, qrBitmap: androidx.compose.ui.graphics.ImageBitmap?, loading: Boolean, error: String?) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(272.dp)
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(QrAddPanel)
            .border(2.dp, Color(0xFF222A40), RoundedCornerShape(22.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> CircularProgressIndicator(color = QrAddAccent)
                error != null -> Text(error, color = Color(0xFF27313A), fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(18.dp))
                qrBitmap != null -> Image(bitmap = qrBitmap, contentDescription = "Scan to join $teamName", modifier = Modifier.fillMaxSize().padding(10.dp), contentScale = ContentScale.Fit)
                else -> Text("QR code unavailable", color = Color(0xFF27313A), fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun InstructionText(teamName: String, modifier: Modifier = Modifier) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = QrAddTextMuted)) {
                append("Ask players to scan this code to join\n")
                append("the team ")
            }
            withStyle(SpanStyle(color = QrAddAccent, fontWeight = FontWeight.Black)) {
                append("$teamName.")
            }
        },
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier.fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

private fun createInviteQrBitmap(value: String): androidx.compose.ui.graphics.ImageBitmap? = runCatching {
    val matrix = QRCodeWriter().encode(
        value,
        BarcodeFormat.QR_CODE,
        512,
        512,
        mapOf(EncodeHintType.MARGIN to 2)
    )
    Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888).apply {
        for (y in 0 until 512) {
            for (x in 0 until 512) {
                setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
    }.asImageBitmap()
}.getOrNull()

@Composable
private fun CloseIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.2f, size.height * 0.2f), Offset(size.width * 0.8f, size.height * 0.8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.8f, size.height * 0.2f), Offset(size.width * 0.2f, size.height * 0.8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun DownloadIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.15f), Offset(size.width * 0.5f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.32f, size.height * 0.44f)
            lineTo(size.width * 0.5f, size.height * 0.64f)
            lineTo(size.width * 0.68f, size.height * 0.44f)
        }
        drawPath(path, tint, style = stroke)
        drawArc(tint, 20f, 140f, false, topLeft = Offset(size.width * 0.18f, size.height * 0.48f), size = Size(size.width * 0.64f, size.height * 0.36f), style = stroke)
    }
}

@Composable
private fun ShareNetworkIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val left = Offset(size.width * 0.28f, size.height * 0.53f)
        val top = Offset(size.width * 0.72f, size.height * 0.29f)
        val bottom = Offset(size.width * 0.72f, size.height * 0.74f)
        drawLine(tint, left, top, strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, left, bottom, strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.11f, center = left, style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.11f, center = top, style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.11f, center = bottom, style = stroke)
    }
}
