package com.example.sportsxtreme.presentation.clubs

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*

class AddMemberInClubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { 
            AddMemberScreen(onBack = { finish() })
        }
    }
}

@Composable
fun AddMemberScreen(onBack: () -> Unit) {
    var selectedRole by remember { mutableStateOf("Captain") } // Default to Captain for this task
    var searchQuery by remember { mutableStateOf("") }
    
    // Form State
    var selectedTeam by remember { mutableStateOf("") }
    var jerseyNumber by remember { mutableStateOf("") }
    var playingRole by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = BlueBackground,
        topBar = {
            AddMemberTopBar(onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // 1. Select Role
            SectionTitle(number = "1", title = "Select Role")
            RoleSelectionSection(
                selectedRole = selectedRole,
                onRoleSelected = { selectedRole = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Search Existing User
            SectionTitle(number = "2", title = "Search Existing User")
            SearchSection(
                searchQuery = searchQuery,
                onSearchChanged = { searchQuery = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Select User
            SectionTitle(number = "3", title = "Select User")
            SelectUserSection(
                name = if (selectedRole == "Captain") "Rahul Kumar" else "Suresh Nayak",
                email = if (selectedRole == "Captain") "rahul.kumar@sportsxtreme.com" else "suresh.nayak@sportsxtreme.com",
                phone = if (selectedRole == "Captain") "+91 9876543211" else "+91 9876543210"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Details Section
            CaptainDetailsSection(
                role = selectedRole,
                selectedTeam = selectedTeam,
                onTeamChange = { selectedTeam = it },
                jerseyNumber = jerseyNumber,
                onJerseyChange = { jerseyNumber = it },
                playingRole = playingRole,
                onPlayingRoleChange = { playingRole = it },
                experience = experience,
                onExperienceChange = { experience = it },
                notes = notes,
                onNotesChange = { notes = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                Button(
                    onClick = { 
                        Toast.makeText(context, "Adding $selectedRole...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = XtremeLime),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add $selectedRole",
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(number: String, title: String) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$number. ",
            color = XtremeLime,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AddMemberTopBar(onBack: () -> Unit) {
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

        Spacer(modifier = Modifier.width(12.dp))

        Image(
            painter = painterResource(id = R.drawable.victory),
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Add Member",
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
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        IconButton(onClick = { /* Notifications */ }) {

                Image(
                    painter = painterResource(id = R.drawable.image__38_),
                    contentDescription = "Notifications",
                    modifier = Modifier.size(28.dp)
                )


        }

        IconButton(
            onClick = { /* Menu */ },
            modifier = Modifier.size(38.dp)
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
private fun RoleSelectionSection(selectedRole: String, onRoleSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RoleCard(
            title = "Admin",
            desc = "Full access to manage the club",
            iconRes = R.drawable.whitevictory,
            isSelected = selectedRole == "Admin",
            modifier = Modifier.weight(1f),
            onClick = { onRoleSelected("Admin") }
        )
        RoleCard(
            title = "Captain",
            desc = "Manage team, players and matches",
            iconRes = R.drawable.whitevitorystar,
            isSelected = selectedRole == "Captain",
            modifier = Modifier.weight(1f),
            onClick = { onRoleSelected("Captain") }
        )
        RoleCard(
            title = "Player",
            desc = "Add as a player in a team",
            iconRes = R.drawable.whiteuser,
            isSelected = selectedRole == "Player",
            modifier = Modifier.weight(1f),
            onClick = { onRoleSelected("Player") }
        )
    }
}

@Composable
private fun RoleCard(
    title: String,
    desc: String,
    iconRes: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(145.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(BlueCardBackGround)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) XtremeLime else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                colorFilter = ColorFilter.tint(if (isSelected) XtremeLime else Color.White.copy(alpha = 0.7f))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = if (isSelected) XtremeLime else Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                color = XtremeMuted,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Icon(
                imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) XtremeLime else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SearchSection(searchQuery: String, onSearchChanged: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.weight(1f),
                color = BlueCardBackGround,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = XtremeMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChanged,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text("Search by name, email or phone...", color = XtremeMuted, fontSize = 12.sp)
                            }
                            inner()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                modifier = Modifier.size(44.dp),
                color = BlueCardBackGround,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                IconButton(onClick = { /* Filter */ }) {
                    Image(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = XtremeMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Only existing users of ",
                color = XtremeMuted,
                fontSize = 10.sp
            )
            Text(
                text = "SportsXtreme ",
                color = XtremeLime,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "can be added.",
                color = XtremeMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun SelectUserSection(name: String, email: String, phone: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = BlueCardBackGround),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(56.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.whiteuser),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(1.dp, XtremeCardBorder, CircleShape),
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.tint(XtremeMuted)
                        )
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(XtremeLime)
                                .border(2.dp, BlueCardBackGround, CircleShape)
                                .align(Alignment.BottomEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(10.dp).align(Alignment.Center)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = email,
                            color = XtremeMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = phone,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF1E3A1A),
                                shape = RoundedCornerShape(3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = XtremeLime,
                                        modifier = Modifier.size(8.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Verified",
                                        color = XtremeLime,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.TopEnd).size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CaptainDetailsSection(
    role: String,
    selectedTeam: String, onTeamChange: (String) -> Unit,
    jerseyNumber: String, onJerseyChange: (String) -> Unit,
    playingRole: String, onPlayingRoleChange: (String) -> Unit,
    experience: String, onExperienceChange: (String) -> Unit,
    notes: String, onNotesChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle(number = "4", title = "$role Details")
        Text(
            text = "Add ${role.lowercase()} specific information.",
            color = XtremeMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 0.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        FormField(
            label = "Select Team",
            value = selectedTeam,
            onValueChange = onTeamChange,
            hint = "Select the team to assign this ${role.lowercase()}",
            iconVector = Icons.Default.KeyboardArrowDown
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            FormField(
                label = "Jersey Number (Optional)",
                value = jerseyNumber,
                onValueChange = onJerseyChange,
                hint = "Enter jersey number",
                iconRes = R.drawable.rcb, // Placeholder for jersey icon if available, or just generic
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            FormField(
                label = "Preferred Playing Role (Optional)",
                value = playingRole,
                onValueChange = onPlayingRoleChange,
                hint = "e.g., Batsman, Bowler...",
                iconVector = Icons.Default.KeyboardArrowDown,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        FormField(
            label = "Experience (Optional)",
            value = experience,
            onValueChange = onExperienceChange,
            hint = "e.g., 3 years of playing experience",
            iconVector = Icons.Outlined.EmojiEvents
        )

        Spacer(modifier = Modifier.height(12.dp))
        FormField(
            label = "Notes (Optional)",
            value = notes,
            onValueChange = onNotesChange,
            hint = "Add any additional notes about the ${role.lowercase()}",
            modifier = Modifier.fillMaxWidth(),
            isTextArea = true
        )
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    iconVector: ImageVector? = null,
    isTextArea: Boolean = false
) {
    Surface(
        modifier = modifier.then(if (isTextArea) Modifier.height(110.dp) else Modifier.height(64.dp)),
        color = BlueCardBackGround,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text(text = hint, color = XtremeMuted, fontSize = 11.sp)
                        }
                        inner()
                    }
                )
            }
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(16.dp)
                )
            } else if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(18.dp)
                )
            }
            if (isTextArea) {
                Text(
                    text = "${value.length}/250",
                    color = XtremeMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}
