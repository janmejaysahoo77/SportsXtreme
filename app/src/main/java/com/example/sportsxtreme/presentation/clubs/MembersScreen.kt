package com.example.sportsxtreme.presentation.clubs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*

@Composable
fun MembersScreen(
    onBack: () -> Unit,
    onAddMember: () -> Unit = {}
) {
    val admins = listOf(
        MemberData("01", "Suresh Nayak", "suresh.nayak@victorycc.com", "+91 9876543210", "Admin"),
        MemberData("02", "Vikas Sharma", "vikas.sharma@victorycc.com", "+91 9123456780", "Admin")
    )
    
    val captains = listOf(
        MemberData("01", "Rahul Kumar", "rahul.kumar@victorycc.com", "+91 9876543211", "Captain"),
        MemberData("02", "Amit Sharma", "amit.sharma@victorycc.com", "+91 9123456781", "Captain")
    )
    
    val players = listOf(
        MemberData("01", "Vivek Sharma", "vivek.sharma@victorycc.com", "+91 9876543212", "Player"),
        MemberData("02", "Nilesh Rao", "nilesh.rao@victorycc.com", "+91 9876543213", "Player"),
        MemberData("03", "Arjun Patil", "arjun.patil@victorycc.com", "+91 9876543214", "Player")
    )

    Scaffold(
        containerColor = BlueBackground,
        topBar = {
            MembersTopBar(onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            HeaderSection(onAddMember)

            Spacer(modifier = Modifier.height(16.dp))

            // Sections
            MemberSection("Admins", 2, admins)
            MemberSection("Captains", 2, captains)
            MemberSection("Players", 12, players)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MembersTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { /* Handle Notifications */ }) {
            Image(
                painter = painterResource(id = R.drawable.image__38_),
                contentDescription = "Notifications",
                modifier = Modifier.size(28.dp),
            )
        }
        
        IconButton(
            onClick = { /* Handle More */ },
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HeaderSection(onAddMember: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Shield Logo
        Image(
            painter = painterResource(id = R.drawable.victory),
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Members",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Victory Cricket Club",
                    color = XtremeMuted,
                    fontSize = 7.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(id = R.drawable.verified),
                    contentDescription = "Verified",
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        // Add Member Button
        Surface(
            onClick = onAddMember,
            color = XtremeLime,
            shape = RoundedCornerShape(7.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAddAlt,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Member",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MemberSection(
    title: String,
    count: Int,
    members: List<MemberData>
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(XtremeLime)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "($count)",
                color = XtremeMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.clickable { /* Handle View All */ },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View All",
                    color = XtremeLime,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = XtremeLime,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            members.forEach { member ->
                MemberCard(member)
            }
        }
    }
}

@Composable
private fun MemberCard(member: MemberData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlueCardBackGround),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = member.id,
                color = XtremeLime,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp)
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E2A3A),
                                Color(0xFF0F1722)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = member.profileRes),
                    contentDescription = member.name,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(XtremeMuted)
                )
                // Online status dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(XtremeLime)
                        .border(1.5.dp, BlueCardBackGround, CircleShape)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-1).dp, y = (-1).dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    RoleTag(member.role)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = member.email,
                    color = XtremeMuted,
                    fontSize = 8.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.whitecall),
                        contentDescription = null,
                        tint = XtremeLime,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = member.phone,
                        color = XtremeMuted,
                        fontSize = 9.sp
                    )
                }
            }

            IconButton(
                onClick = { /* Handle member menu */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun RoleTag(role: String) {
    Surface(
        color = Color.Transparent,
        border = BorderStroke(1.dp, XtremeLime.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(5.dp),
    ) {
        Box(
            modifier = Modifier
                .background(XtremeLime.copy(alpha = 0.08f))
                .padding(horizontal = 5.dp, vertical = 1.5.dp)
        ) {
            Text(
                text = role,
                color = XtremeLime,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class MemberData(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val profileRes: Int = R.drawable.whiteuser
)
