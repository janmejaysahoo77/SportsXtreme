package com.example.sportsxtreme.presentation.team

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddPlayerActivity : ComponentActivity() {
    @Inject lateinit var functions: FirebaseFunctions
    private var isCreatingInvite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val teamName = intent.getStringExtra(ManagePlayersInsideTeamActivity.EXTRA_TEAM_NAME).orEmpty().ifBlank { "Balia Gang" }
        val teamId = intent.getStringExtra(ManagePlayersInsideTeamActivity.EXTRA_TEAM_ID).orEmpty()
        setContent {
            AddPlayerScreen(
                teamName = teamName,
                onBack = ::finish,
                onInvite = { createAndShareInvite(teamId, teamName) },
                onAddByMobileNumber = { startActivity(Intent(this, AddPlayerByMobileNumberActivity::class.java)) },
                onScanQrCode = { startActivity(Intent(this, QRForPlayerAddingActivity::class.java)) }
            )
        }
    }

    private fun createAndShareInvite(teamId: String, teamName: String) {
        if (isCreatingInvite) return
        if (teamId.isBlank()) {
            Toast.makeText(this, "This team is missing its ID. Please reopen it and try again.", Toast.LENGTH_SHORT).show()
            return
        }
        isCreatingInvite = true
        functions.getHttpsCallable(CREATE_TEAM_INVITE_FUNCTION)
            .call(mapOf("teamId" to teamId))
            .addOnSuccessListener { result ->
                val invitationUrl = (result.data as? Map<*, *>)?.get("invitationUrl") as? String
                if (invitationUrl.isNullOrBlank()) {
                    Toast.makeText(this, "Could not create an invitation.", Toast.LENGTH_SHORT).show()
                } else {
                    shareInvite(teamName, invitationUrl)
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, error.message ?: "Unable to create an invitation.", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener { isCreatingInvite = false }
    }

    private fun shareInvite(teamName: String, invitationUrl: String) {
        val shareText = "Join $teamName on SportsXtreme:\n$invitationUrl"
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                },
                "Invite members to $teamName"
            )
        )
    }

    private companion object {
        const val CREATE_TEAM_INVITE_FUNCTION = "createTeamInvite"
    }
}

private val Accent = Color(0xFFC7F36A)
private val ScreenBg = Color(0xFF050B14)
private val Surface = Color(0xFF0B1420)
private val SurfaceRaised = Color(0xFF101C2A)
private val StrokeColor = Color(0xFF223144)
private val Muted = Color(0xFF9BAABD)
private val Primary = Color(0xFFF4F7FA)

@Composable
private fun AddPlayerScreen(teamName: String, onBack: () -> Unit, onInvite: () -> Unit, onAddByMobileNumber: () -> Unit, onScanQrCode: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(ScreenBg).drawBehind {
            drawCircle(Accent.copy(alpha = .045f), size.width * .72f, Offset(size.width * .78f, size.height * .04f))
            drawCircle(Color(0xFF245383).copy(alpha = .08f), size.width * .90f, Offset(size.width * -.14f, size.height * .62f))
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            TopBar(teamName, onBack)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                Text("ADD TO YOUR SQUAD", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, modifier = Modifier.padding(top = 26.dp))
                Text("Choose an invite method", color = Primary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 7.dp))
                Text("Build your team in the way that works best for you.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
                InviteCard(Modifier.padding(top = 25.dp), onInvite)
                MethodCard(Modifier.padding(top = 12.dp), "Add by Mobile Number", "Quickly add a player using their phone number.", { PhoneIcon(Modifier.size(23.dp), Accent) }, onAddByMobileNumber)
                MethodCard(Modifier.padding(top = 12.dp), "Scan QR Code", "Scan a player's QR code to add them instantly.", { QrIcon(Modifier.size(22.dp), Accent) }, onScanQrCode)
                InfoCard(Modifier.padding(top = 22.dp))
                Spacer(Modifier.height(28.dp))
            }
            ContinueButton()
        }
    }
}

@Composable
private fun TopBar(teamName: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(72.dp).background(Color(0xCC08111C)).border(.5.dp, Color(0xFF17263A)).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceRaised).border(1.dp, StrokeColor, RoundedCornerShape(12.dp)).clickable(onClick = onBack), contentAlignment = Alignment.Center) { BackIcon(Modifier.size(20.dp), Primary) }
        Column(Modifier.padding(start = 13.dp).weight(1f)) {
            Text("ADD PLAYERS", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
            Text(buildAnnotatedString { append("TO "); withStyle(SpanStyle(color = Accent, fontWeight = FontWeight.Bold)) { append(teamName) } }, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Surface), contentAlignment = Alignment.Center) { HelpIcon(Modifier.size(19.dp), Muted) }
    }
}

@Composable
private fun InviteCard(modifier: Modifier, onInvite: () -> Unit) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brush.verticalGradient(listOf(Color(0xFF15231C), SurfaceRaised))).border(1.dp, Accent.copy(alpha = .48f), RoundedCornerShape(18.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MethodIcon(true) { ShareIcon(Modifier.size(23.dp), Color(0xFF13200A)) }
            Column(Modifier.padding(start = 13.dp).weight(1f)) {
                Text("Invite Player", color = Primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Send an invite link to join SportsXtreme.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
            }
            Text("Recommended", color = Accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShareButton("Share", Modifier.weight(1f), false, onInvite)
            ShareButton("WhatsApp", Modifier.weight(1f), true, onInvite)
        }
    }
}

@Composable
private fun MethodCard(modifier: Modifier, title: String, subtitle: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Row(modifier.fillMaxWidth().height(86.dp).clip(RoundedCornerShape(16.dp)).background(Surface).border(1.dp, StrokeColor, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        MethodIcon(false, icon)
        Column(Modifier.padding(start = 13.dp).weight(1f)) {
            Text(title, color = Primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 4.dp))
        }
        ChevronIcon(Modifier.size(18.dp), Muted)
    }
}

@Composable
private fun MethodIcon(selected: Boolean, content: @Composable () -> Unit) {
    Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(if (selected) Accent else Accent.copy(alpha = .10f)).border(1.dp, if (selected) Accent else Accent.copy(alpha = .20f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun ShareButton(text: String, modifier: Modifier, isWhatsApp: Boolean, onClick: () -> Unit) {
    Row(modifier.height(42.dp).clip(RoundedCornerShape(11.dp)).background(if (isWhatsApp) Accent else Color.Transparent).border(1.dp, if (isWhatsApp) Accent else StrokeColor, RoundedCornerShape(11.dp)).clickable(onClick = onClick), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        if (isWhatsApp) Image(painterResource(R.drawable.whatsappicon), "WhatsApp", Modifier.size(16.dp)) else SmallShareIcon(Modifier.size(16.dp), Accent)
        Text(text, color = if (isWhatsApp) Color(0xFF10170B) else Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun InfoCard(modifier: Modifier) {
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF0A1725)).border(1.dp, Color(0xFF1E3B58), RoundedCornerShape(14.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).background(Color(0xFF173553), CircleShape), contentAlignment = Alignment.Center) { InfoIcon(Modifier.size(17.dp), Color(0xFF9CCEFF)) }
        Column(Modifier.padding(start = 11.dp)) {
            Text("Invite your squad", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("Players can be added via invite link, mobile number, or QR code.", color = Muted, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun ContinueButton() {
    Box(Modifier.fillMaxWidth().background(Color(0xF2070E18)).border(.5.dp, Color(0xFF17263A)).padding(horizontal = 20.dp, vertical = 14.dp)) {
        Box(Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(15.dp)).background(Accent).clickable { }, contentAlignment = Alignment.Center) { Text("CONTINUE", color = Color(0xFF10170B), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp) }
    }
}

@Composable
private fun BackIcon(modifier: Modifier, tint: Color) = Canvas(modifier) {
    val stroke = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
    drawPath(Path().apply { moveTo(size.width * .62f, size.height * .23f); lineTo(size.width * .35f, size.height * .5f); lineTo(size.width * .62f, size.height * .77f) }, tint, style = stroke)
}

@Composable
private fun HelpIcon(modifier: Modifier, tint: Color) = Canvas(modifier) {
    val stroke = Stroke(1.6.dp.toPx(), cap = StrokeCap.Round)
    drawCircle(tint, size.minDimension * .38f, style = stroke)
    drawArc(tint, 210f, 220f, false, style = stroke)
    drawCircle(tint, size.minDimension * .04f, Offset(size.width * .5f, size.height * .68f))
}

@Composable
private fun ShareIcon(modifier: Modifier, tint: Color) = Canvas(modifier) {
    val stroke = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
    val from = Offset(size.width * .3f, size.height * .52f)
    val upper = Offset(size.width * .68f, size.height * .3f)
    val lower = Offset(size.width * .68f, size.height * .7f)
    drawLine(tint, from, upper, stroke.width, cap = StrokeCap.Round)
    drawLine(tint, from, lower, stroke.width, cap = StrokeCap.Round)
    drawCircle(tint, size.minDimension * .11f, from, style = stroke)
    drawCircle(tint, size.minDimension * .11f, upper, style = stroke)
    drawCircle(tint, size.minDimension * .11f, lower, style = stroke)
}

@Composable
private fun PhoneIcon(modifier: Modifier, tint: Color) = Canvas(modifier) {
    val stroke = Stroke(2.1.dp.toPx(), cap = StrokeCap.Round)
    drawPath(Path().apply {
        moveTo(size.width * .3f, size.height * .18f)
        cubicTo(size.width * .16f, size.height * .24f, size.width * .2f, size.height * .62f, size.width * .48f, size.height * .78f)
        cubicTo(size.width * .62f, size.height * .86f, size.width * .78f, size.height * .78f, size.width * .82f, size.height * .66f)
    }, tint, style = stroke)
    drawCircle(tint, size.minDimension * .07f, Offset(size.width * .32f, size.height * .22f), style = stroke)
    drawCircle(tint, size.minDimension * .07f, Offset(size.width * .74f, size.height * .67f), style = stroke)
}

@Composable
private fun QrIcon(modifier: Modifier, tint: Color) = Canvas(modifier) {
    val stroke = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round)
    fun square(x: Float, y: Float) {
        drawRect(tint, Offset(size.width * x, size.height * y), Size(size.width * .18f, size.height * .18f), style = stroke)
    }
    square(.16f, .16f)
    square(.62f, .16f)
    square(.16f, .62f)
    drawLine(tint, Offset(size.width * .5f, size.height * .52f), Offset(size.width * .78f, size.height * .52f), stroke.width, cap = StrokeCap.Round)
    drawLine(tint, Offset(size.width * .52f, size.height * .62f), Offset(size.width * .52f, size.height * .82f), stroke.width, cap = StrokeCap.Round)
    drawCircle(tint, size.minDimension * .04f, Offset(size.width * .72f, size.height * .72f))
}

@Composable
private fun ChevronIcon(modifier: Modifier, tint: Color) = Canvas(modifier) {
    val stroke = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
    drawPath(Path().apply { moveTo(size.width * .38f, size.height * .28f); lineTo(size.width * .62f, size.height * .5f); lineTo(size.width * .38f, size.height * .72f) }, tint, style = stroke)
}

@Composable
private fun SmallShareIcon(modifier: Modifier, tint: Color) = Canvas(modifier) {
    val stroke = Stroke(1.7.dp.toPx(), cap = StrokeCap.Round)
    drawLine(tint, Offset(size.width * .5f, size.height * .2f), Offset(size.width * .5f, size.height * .72f), stroke.width, cap = StrokeCap.Round)
    drawLine(tint, Offset(size.width * .32f, size.height * .38f), Offset(size.width * .5f, size.height * .2f), stroke.width, cap = StrokeCap.Round)
    drawLine(tint, Offset(size.width * .68f, size.height * .38f), Offset(size.width * .5f, size.height * .2f), stroke.width, cap = StrokeCap.Round)
    drawRoundRect(tint, Offset(size.width * .24f, size.height * .54f), Size(size.width * .52f, size.height * .3f), style = stroke)
}

@Composable
private fun InfoIcon(modifier: Modifier, tint: Color) = Canvas(modifier) {
    val stroke = Stroke(1.6.dp.toPx(), cap = StrokeCap.Round)
    drawCircle(tint, size.minDimension * .42f, style = stroke)
    drawLine(tint, Offset(size.width * .5f, size.height * .45f), Offset(size.width * .5f, size.height * .72f), stroke.width, cap = StrokeCap.Round)
    drawCircle(tint, size.minDimension * .035f, Offset(size.width * .5f, size.height * .29f))
}
