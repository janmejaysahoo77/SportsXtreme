package com.example.sportsxtreme.presentation.clubs

import android.content.Intent
import com.example.sportsxtreme.common.dashedBorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.ContactPage
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.rounded.Beenhere
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R

class Step_FourCreateClubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            StepFourTheme {
                CreateClubStepFourScreen { finish() }
            }
        }
    }
}

// Unified Design System Colors (matching ClubLandingActivity)
private val ColorBg = Color(5, 7, 8)
private val ColorSurface = Color.DarkGray.copy(alpha = 0.1f)
private val ColorLime = Color(190, 255, 24)
private val ColorTextPrimary = Color.White
private val ColorTextSecondary = Color(162, 169, 164)
private val ColorCardBorder = Color(45, 51, 46)

@Composable
fun StepFourTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = ColorBg,
            surface = ColorSurface,
            primary = ColorLime,
            onBackground = ColorTextPrimary,
            onSurface = ColorTextPrimary
        ),
        content = content
    )
}

@Composable
fun CreateClubStepFourScreen(onBack: () -> Unit) {
    var selectedOwnerId by remember { mutableStateOf("Aadhaar Card") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        containerColor = ColorBg,
        topBar = { StepFourTopBar(onBack) },
        bottomBar = {
            StepFourBottomBar(
                onClick = {
                    context.startActivity(Intent(context, ClubSubmissionSuccessActivity::class.java))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            StepFourIndicator()
            Spacer(modifier = Modifier.height(32.dp))

            // Hero Section
            ProofHeroSection()

            Spacer(modifier = Modifier.height(32.dp))

            // 1. Proof of Club
            ProofOfClubSection()

            Spacer(modifier = Modifier.height(32.dp))

            // 2. Owner Identity
            OwnerIdentitySection(
                selectedId = selectedOwnerId,
                onIdSelect = { selectedOwnerId = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ProofInfoCard(
                        title = stringResource(R.string.secure_private_title),
                        description = stringResource(R.string.secure_private_desc),
                        icon = Icons.Outlined.Lock,
                        iconColor = Color(0xFF3B82F6)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ProofInfoCard(
                        title = stringResource(R.string.important_note_title),
                        description = stringResource(R.string.important_note_desc),
                        icon = Icons.Outlined.Warning,
                        iconColor = Color(0xFFF59E0B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun StepFourTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = ColorTextPrimary
            )
        }
        Text(
            text = stringResource(R.string.create_club_header),
            color = ColorTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(ColorSurface)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(R.string.step_4_of_4),
                color = ColorTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StepFourIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        val steps = listOf(
            stringResource(R.string.step_basic),
            stringResource(R.string.step_contact),
            stringResource(R.string.step_location),
            stringResource(R.string.step_proof)
        )

        steps.forEachIndexed { index, label ->
            val isCurrent = index == 3
            val isCompleted = index < 3
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent || isCompleted) ColorLime else ColorCardBorder)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = ColorLime,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = label,
                        color = if (isCurrent || isCompleted) ColorLime else ColorTextSecondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ProofHeroSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(ColorLime.copy(alpha = 0.1f))
                .border(1.dp, ColorLime.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Beenhere,
                contentDescription = null,
                tint = ColorLime,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.proof_of_club_title),
            color = ColorTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.proof_of_club_subtitle),
            color = ColorTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ProofOfClubSection() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ContactPage,
                    contentDescription = null,
                    tint = ColorTextSecondary,
                    modifier = Modifier.size(25.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.section_proof_club),
                    color = ColorTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.section_proof_club_desc),
                    color = ColorTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Upload Area - Increased Height and Dashed Border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .dashedBorder(1.5.dp, ColorCardBorder, 18.dp)
                .background(ColorSurface, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.CloudUpload,
                    contentDescription = null,
                    tint = ColorLime,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.drag_drop_file),
                    color = ColorTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.or_text),
                    color = ColorTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = ColorLime),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.choose_file),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.file_format_limit),
                    color = ColorTextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.accepted_documents_label),
            color = ColorTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Documents Grid - 2 cards per row
        val docs = listOf(
            stringResource(R.string.doc_club_reg) to stringResource(R.string.tag_rec),
            stringResource(R.string.doc_trust_soc) to stringResource(R.string.tag_rec),
            stringResource(R.string.doc_pan_club) to stringResource(R.string.tag_opt),
            stringResource(R.string.doc_addr_proof) to stringResource(R.string.tag_opt),
            stringResource(R.string.doc_ground_photo) to stringResource(R.string.tag_opt),
            stringResource(R.string.doc_other) to stringResource(R.string.tag_opt)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            docs.chunked(2).forEach { rowDocs ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowDocs.forEach { (name, tag) ->
                        Box(modifier = Modifier.weight(1f)) {
                            DocumentItem(name = name, tag = tag)
                        }
                    }
                    if (rowDocs.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Review Note
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(ColorLime.copy(alpha = 0.05f))
                .border(1.dp, ColorCardBorder, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = ColorLime,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.review_note),
                    color = ColorTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun DocumentItem(name: String, tag: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(ColorSurface)
            .border(1.dp, ColorCardBorder, RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = ColorTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = tag,
                    color = if (tag == "REC.") ColorLime else ColorTextSecondary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                color = ColorTextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun OwnerIdentitySection(selectedId: String, onIdSelect: (String) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = ColorTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.section_owner_identity),
                    color = ColorTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.section_owner_identity_desc),
                    color = ColorTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ID Chips - 2 per row
        val ids = listOf(
            stringResource(R.string.id_aadhaar),
            stringResource(R.string.id_pan),
            stringResource(R.string.id_passport),
            stringResource(R.string.id_voter)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ids.chunked(2).forEach { rowIds ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowIds.forEach { id ->
                        val isSelected = id == selectedId
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) ColorLime else ColorSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) ColorLime else ColorCardBorder,
                                    CircleShape
                                )
                                .clickable { onIdSelect(id) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = id,
                                color = if (isSelected) Color.Black else ColorTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ID Upload Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(18.dp))
                .dashedBorder(1.dp, ColorCardBorder, 18.dp)
                .background(ColorSurface, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.Upload,
                    contentDescription = null,
                    tint = ColorTextSecondary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.upload_id_hint),
                    color = ColorTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ProofInfoCard(title: String, description: String, icon: ImageVector, iconColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ColorCardBorder),
        colors = CardDefaults.cardColors(containerColor = ColorSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = ColorTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = ColorTextSecondary,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun StepFourBottomBar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(
                    elevation = 6.dp, shape = RoundedCornerShape(16.dp),
                    clip = true, spotColor = ColorLime, ambientColor = ColorLime
                )
                .clip(RoundedCornerShape(16.dp))
                .background(ColorLime)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.btn_submit_verification),
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W800
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

