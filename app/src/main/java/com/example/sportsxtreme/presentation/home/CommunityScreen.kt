package com.example.sportsxtreme.presentation.home

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import android.widget.Toast
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsxtreme.R
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.example.sportsxtreme.data.remote.cloudinary.CloudinaryTournamentImageUploader
import kotlinx.coroutines.launch
import java.util.UUID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp

// Color Palette
private val GreenAccent = Color(0xFFC1FF00)
private val CyanAccent = Color(0xFF00D2FF)
private val ScreenBg = Color(0xFF010509)
private val MutedText = Color(0xFF9AA7A3)
private val StrokeBorder = Color(0x334E5B64)

private data class CommunityCategory(
    val title: String,
    val subtitle: String,
    @get:DrawableRes val icon: Int,
    val glow: Color
)

private data class Opportunity(
    val name: String,
    val role: String,
    val description: String,
    val meta: String,
    val price: String?,
    val action: String,
    val age: String,
    val accent: Color,
    val location: String? = null,
    val league: String? = null,
    val bubble: String? = null,
    val profilePhotoUrl: String? = null
)

private val categories = listOf(
    CommunityCategory("Scorers", "OFFICIAL", R.drawable.scorerss, GreenAccent),
    CommunityCategory("Umpires", "COURT ROLE", R.drawable.umpires, Color(0xFF3AD7FF)),
    CommunityCategory("Voices", "LIVE", R.drawable.commentatorss, Color(0xFFFF8EAE)),
    CommunityCategory("Streams", "BROADCASTERS", R.drawable.stremers, GreenAccent),
    CommunityCategory("Organisers", "EVENTS", R.drawable.organiserss, Color(0xFF4DE9FF)),
    CommunityCategory("Academies", "TRAINING", R.drawable.academy, Color(0xFFFFA3B7)),
    CommunityCategory("Grounds", "VENUES", R.drawable.ground, GreenAccent)
)

private val filters = listOf("All", "Looking For", "Available")

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CommunityScreen(
    onMenuClick: () -> Unit = {}
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var showCreateOptions by remember { mutableStateOf(false) }
    var showProfileForm by remember { mutableStateOf(false) }
    val createOptionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val profileFormSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val communityProfiles = remember { mutableStateListOf<Opportunity>() }
    val currentUser = FirebaseAuth.getInstance().currentUser
    var accountPhotoUrl by remember(currentUser?.uid) { mutableStateOf(currentUser?.photoUrl?.toString()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { userDocument ->
                accountPhotoUrl = userDocument.getString("profilePhotoUrl")
                    ?.takeIf { it.isNotBlank() }
                    ?: currentUser.photoUrl?.toString()
            }
    }

    DisposableEffect(Unit) {
        val registration: ListenerRegistration = FirebaseFirestore.getInstance()
            .collection("communityProfiles")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    communityProfiles.clear()
                    communityProfiles.addAll(snapshot.documents.mapNotNull { document ->
                        val data = document.data ?: return@mapNotNull null
                        val createdAt = data["createdAt"] as? Timestamp
                        Opportunity(
                            name = data["name"] as? String ?: "Community member",
                            role = (data["category"] as? String ?: "PLAYER").uppercase(),
                            description = listOfNotNull(
                                (data["organization"] as? String)?.takeIf { it.isNotBlank() },
                                (data["experience"] as? String)?.takeIf { it.isNotBlank() }?.let { "$it experience" }
                            ).joinToString(" · "),
                            meta = "Match",
                            price = (data["chargePerMatch"] as? String)?.takeIf { it.isNotBlank() }?.let { "₹$it" },
                            action = "Connect",
                            age = createdAt?.let { timeAgo(it.toDate().time) } ?: "Just now",
                            accent = GreenAccent,
                            location = (data["location"] as? String)?.takeIf { it.isNotBlank() },
                            league = listOfNotNull(
                                (data["groundType"] as? String)?.let { "Ground: $it" },
                                (data["ballType"] as? String)?.let { "Ball: $it" }
                            ).joinToString(" · ").ifBlank { null },
                            profilePhotoUrl = (data["profilePhotoUrl"] as? String)?.takeIf { it.isNotBlank() }
                        )
                    })
                }
            }
        onDispose { registration.remove() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .drawBehind {
                drawCircle(
                    color = Color(0x29223416),
                    radius = size.width * 0.75f,
                    center = Offset(size.width * 0.95f, size.height * 0.25f)
                )
                drawCircle(
                    color = Color(0x1400D2FF),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.05f, size.height * 0.65f)
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterRow(
                        selectedIndex = selectedFilterIndex,
                        onSelectFilter = { selectedFilterIndex = it }
                    )
                }
                item {
                    Text(
                        text = "New Opportunities",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                if (communityProfiles.isEmpty()) {
                    item {
                        Text(
                            text = "No community posts yet. Be the first to add your profile.",
                            color = Color(0xFF9AA7A3),
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        )
                    }
                }
                items(communityProfiles) { opportunity ->
                    OpportunityCard(opportunity = opportunity)
                }
                item { Spacer(modifier = Modifier.height(34.dp)) }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 92.dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(GreenAccent)
                .clickable { showCreateOptions = true },
            contentAlignment = Alignment.Center
        ) {
            Text("+", color = Color(0xFF172000), fontSize = 34.sp, fontWeight = FontWeight.Medium)
        }

        if (showCreateOptions) {
            ModalBottomSheet(
                onDismissRequest = { showCreateOptions = false },
                sheetState = createOptionsSheetState,
                containerColor = Color(0xFF101A24),
                contentColor = Color.White,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 6.dp)
                            .width(38.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE7F0F8))
                    )
                }
            ) {
                CreateCommunityOptionsSheet(
                    onLookingFor = { showCreateOptions = false },
                    onAddProfile = {
                        showCreateOptions = false
                        showProfileForm = true
                    },
                    onCancel = { showCreateOptions = false }
                )
            }
        }

        if (showProfileForm) {
            ModalBottomSheet(
                onDismissRequest = { showProfileForm = false },
                sheetState = profileFormSheetState,
                containerColor = Color(0xFF101A24),
                contentColor = Color.White,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 6.dp)
                            .width(38.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE7F0F8))
                    )
                }
            ) {
                AddSelfProfileForm(
                    initialPhotoUrl = accountPhotoUrl,
                    onBack = {
                        showProfileForm = false
                        showCreateOptions = true
                    },
                    onPost = { name, category, location, organization, experience, groundType, ballType, chargePerMatch, whatsappNumber, whatsappConsent, photoUri ->
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user == null) {
                            Toast.makeText(context, "Sign in to post a Community profile.", Toast.LENGTH_LONG).show()
                        } else {
                            scope.launch {
                                val photoUrl = if (photoUri != null) {
                                    CloudinaryTournamentImageUploader(context.applicationContext)
                                        .upload(photoUri, "community-${UUID.randomUUID()}", "logo")
                                        .getOrElse { error ->
                                            Toast.makeText(context, "Could not upload profile photo: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                            return@launch
                                        }
                                } else accountPhotoUrl.orEmpty()
                                val profile = hashMapOf(
                                    "ownerId" to user.uid,
                                    "name" to name,
                                    "category" to category,
                                    "location" to location,
                                    "organization" to organization,
                                    "experience" to experience,
                                    "groundType" to groundType,
                                    "ballType" to ballType,
                                    "chargePerMatch" to chargePerMatch,
                                    "profilePhotoUrl" to photoUrl,
                                    "whatsappConsent" to whatsappConsent,
                                    "createdAt" to FieldValue.serverTimestamp()
                                )
                                val db = FirebaseFirestore.getInstance()
                                val profileRef = db.collection("communityProfiles").document()
                                val batch = db.batch()
                                batch.set(profileRef, profile)
                                batch.set(
                                    profileRef.collection("private").document("contact"),
                                    mapOf("ownerId" to user.uid, "whatsappNumber" to whatsappNumber)
                                )
                                batch.commit()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Your profile is now on Community.", Toast.LENGTH_SHORT).show()
                                        showProfileForm = false
                                    }
                                    .addOnFailureListener { error ->
                                        Toast.makeText(context, "Could not post profile: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                    }
                )
            }
        }
    }
}

private fun timeAgo(createdAtMillis: Long): String {
    val elapsedMinutes = ((System.currentTimeMillis() - createdAtMillis).coerceAtLeast(0L) / 60_000L)
    return when {
        elapsedMinutes < 1 -> "Just now"
        elapsedMinutes < 60 -> "${elapsedMinutes}m ago"
        elapsedMinutes < 1_440 -> "${elapsedMinutes / 60}h ago"
        else -> "${elapsedMinutes / 1_440}d ago"
    }
}

@Composable
private fun AddSelfProfileForm(
    initialPhotoUrl: String?,
    onBack: () -> Unit,
    onPost: (String, String, String, String, String, String, String, String, String, Boolean, Uri?) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var groundType by remember { mutableStateOf("") }
    var ballType by remember { mutableStateOf("") }
    var chargePerMatch by remember { mutableStateOf("") }
    var whatsappNumber by remember { mutableStateOf("") }
    var whatsappConsent by remember { mutableStateOf(false) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedPhotoUri = uri
    }
    var openMenu by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf("") }
    val categories = listOf("Batter", "Bowler", "AllRounder", "Wicket keeper", "Umpire")
    val groundTypes = listOf("Turf", "Matting", "Concrete", "Grass", "Indoor")
    val ballTypes = listOf("Leather", "Tennis", "Tape Ball", "Other")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = Color.White, fontSize = 34.sp, modifier = Modifier.clickable(onClick = onBack).padding(end = 14.dp))
            Column {
                Text("Add Self Profile", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("Create your profile as player, umpire, organizer, etc.", color = Color(0xFFA8B9C8), fontSize = 11.sp)
            }
        }

        ProfilePhotoPlaceholder(
            photo = selectedPhotoUri ?: initialPhotoUrl,
            onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        )
        ProfileInput("Full Name", "Enter your full name", fullName, { fullName = it }, required = true, icon = Icons.Filled.Person)
        ProfileDropdown("Select Category", "Select category", category, categories, { category = it }, { openMenu = it }, openMenu, required = true)
        ProfileInput("Location", "Enter location (e.g., Kalinga Stadium, Bhubaneswar)", location, { location = it }, icon = Icons.Filled.LocationOn)
        ProfileInput("Organization / Team", "Enter team or organization name", organization, { organization = it }, iconRes = R.drawable.groups)
        ProfileInput("Experience", "e.g., 2+ years", experience, { experience = it }, optional = true, iconRes = R.drawable.trophy1)
        ProfileDropdown("Ground Type", "Select ground type", groundType, groundTypes, { groundType = it }, { openMenu = it }, openMenu, required = true, icon = Icons.Filled.LocationOn)
        ProfileDropdown("Ball Type", "Select ball type", ballType, ballTypes, { ballType = it }, { openMenu = it }, openMenu, required = true, iconRes = R.drawable.tennisball)
        ProfileInput("Your Charge Per Match", "Enter your charge per match", chargePerMatch, { chargePerMatch = it }, keyboardType = KeyboardType.Number, iconRes = R.drawable.rupee)
        ProfileInput("WhatsApp Number", "Enter your WhatsApp number", whatsappNumber, { whatsappNumber = it }, required = true, keyboardType = KeyboardType.Phone, prefix = "+91", iconRes = R.drawable.whatsappicon)
        Row(
            modifier = Modifier.fillMaxWidth().clickable { whatsappConsent = !whatsappConsent },
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = whatsappConsent,
                onCheckedChange = { whatsappConsent = it },
                colors = androidx.compose.material3.CheckboxDefaults.colors(
                    checkedColor = GreenAccent,
                    uncheckedColor = Color(0xFFA8B9C8),
                    checkmarkColor = Color(0xFF111800)
                )
            )
            Text(
                "I consent to sharing my WhatsApp number with users who wish to contact me regarding this listing. My number will not be displayed publicly, but tapping the WhatsApp button will allow users to contact me directly via WhatsApp. For more, check Privacy Policy.",
                color = Color(0xFFB6C4CF),
                fontSize = 10.sp,
                lineHeight = 14.sp,
                modifier = Modifier.weight(1f).clickable { whatsappConsent = !whatsappConsent }.padding(top = 12.dp)
            )
        }

        if (validationMessage.isNotBlank()) {
            Text(validationMessage, color = Color(0xFFFF7586), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 4.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(46.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(GreenAccent)
                .clickable {
                    validationMessage = when {
                        fullName.isBlank() -> "Enter your full name."
                        category.isBlank() -> "Select a category."
                        groundType.isBlank() -> "Select a ground type."
                        ballType.isBlank() -> "Select a ball type."
                        whatsappNumber.filter(Char::isDigit).length !in 10..12 -> "Enter a valid WhatsApp number."
                        !whatsappConsent -> "Please consent to WhatsApp contact before posting."
                        else -> ""
                    }
                    if (validationMessage.isBlank()) onPost(fullName.trim(), category, location.trim(), organization.trim(), experience.trim(), groundType, ballType, chargePerMatch.filter(Char::isDigit), whatsappNumber.trim(), whatsappConsent, selectedPhotoUri)
                },
            contentAlignment = Alignment.Center
        ) {
            Text("Post", color = Color(0xFF111800), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ProfilePhotoPlaceholder(photo: Any?, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(108.dp).clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF101E2B)).border(1.dp, Color(0x55445A6B), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(76.dp).clip(CircleShape).background(Color(0xFF192838)).border(1.5.dp, Color(0xFFC1FF00), CircleShape), contentAlignment = Alignment.Center) {
            if (photo != null) {
                AsyncImage(
                    model = photo,
                    contentDescription = "Community profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Icon(Icons.Filled.Person, contentDescription = "Profile photo", tint = Color(0xFFB7C6D3), modifier = Modifier.size(34.dp))
            }
            Box(Modifier.align(Alignment.BottomEnd).size(25.dp).clip(CircleShape).background(GreenAccent).border(2.dp, Color(0xFF0D1822), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Change profile photo", tint = Color(0xFF111800), modifier = Modifier.size(14.dp))
            }
        }
        Column(Modifier.padding(start = 14.dp)) {
            Text("Add Profile Photo", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("JPG, PNG (Max 5MB)", color = Color(0xFFA8B9C8), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun ProfileInput(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    required: Boolean = false,
    optional: Boolean = false,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF0E1A26))
            .border(1.dp, Color(0x55445A6B), RoundedCornerShape(14.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(25.dp))
        } else if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color(0xFFE4EDF4), modifier = Modifier.size(24.dp))
        } else {
            Spacer(Modifier.width(30.dp))
        }
        Column(Modifier.weight(1f)) {
            ProfileFieldLabel(label, required, optional)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = Color(0xFF8295A5), fontSize = 12.sp, maxLines = 1) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                leadingIcon = if (prefix != null) ({ Text(prefix, color = Color.White, fontSize = 12.sp) }) else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF314656), unfocusedBorderColor = Color(0xFF263947),
                    cursorColor = GreenAccent
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
            )
        }
    }
}

@Composable
private fun ProfileDropdown(
    label: String,
    placeholder: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    onExpand: (String) -> Unit,
    openMenu: String,
    required: Boolean,
    icon: ImageVector? = null,
    iconRes: Int? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF0E1A26))
            .border(1.dp, Color(0x55445A6B), RoundedCornerShape(14.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            iconRes != null -> Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(25.dp))
            icon != null -> Icon(icon, contentDescription = null, tint = Color(0xFFE4EDF4), modifier = Modifier.size(24.dp))
            else -> Spacer(Modifier.width(30.dp))
        }
        Column(Modifier.weight(1f)) {
            ProfileFieldLabel(label, required, false)
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFC1FF00), RoundedCornerShape(8.dp)).clickable { onExpand(label) }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(value.ifBlank { placeholder }, color = if (value.isBlank()) Color(0xFF9DAFBD) else Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("⌄", color = Color.White, fontSize = 18.sp)
                }
                DropdownMenu(expanded = openMenu == label, onDismissRequest = { onExpand("") }) {
                    options.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); onExpand("") })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileFieldLabel(label: String, required: Boolean, optional: Boolean) {
    Row(modifier = Modifier.padding(bottom = 4.dp)) {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        if (optional) Text(" (Optional)", color = Color(0xFFA8B9C8), fontSize = 11.sp)
        if (required) Text(" *", color = Color(0xFFFF5468), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CreateCommunityOptionsSheet(
    onLookingFor: () -> Unit,
    onAddProfile: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = "What would you like to do?",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 3.dp)
        )
        CreateCommunityOption(
            icon = "⌕",
            title = "Looking for something?",
            description = "Post an opportunity for match, team, players, ground, and more.",
            iconColor = Color(0xFFC1FF00),
            onClick = onLookingFor
        )
        CreateCommunityOption(
            icon = "●",
            title = "Add Self Profile",
            description = "Create your profile as player, umpire, organizer, etc.",
            iconColor = Color(0xFFC05CFF),
            onClick = onAddProfile
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1B2834))
                .border(1.dp, Color(0x334E5B64), RoundedCornerShape(10.dp))
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center
        ) {
            Text("Cancel", color = Color(0xFFE6EDF4), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CreateCommunityOption(
    icon: String,
    title: String,
    description: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFF111D28))
            .border(1.dp, Color(0x334E5B64), RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, color = Color(0xFF101820), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(description, color = Color(0xFFA8B9C8), fontSize = 11.sp, lineHeight = 15.sp)
        }
        Text("›", color = Color(0xFFA8B9C8), fontSize = 25.sp)
    }
}

@Composable
private fun HeaderBar(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hamburger Menu
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onMenuClick),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(20.dp)) {
                val stroke = 2.dp.toPx()
                val color = Color.White
                drawLine(color, Offset(0f, size.height * 0.2f), Offset(size.width, size.height * 0.2f), stroke, StrokeCap.Round)
                drawLine(color, Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), stroke, StrokeCap.Round)
                drawLine(color, Offset(0f, size.height * 0.8f), Offset(size.width, size.height * 0.8f), stroke, StrokeCap.Round)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // SportsXtreme Logo with Brand Drawable
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.splash_brand_tight_logo),
                contentDescription = "Brand Logo",
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Sports",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Xtreme",
                color = CyanAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Search Icon
        Canvas(
            modifier = Modifier
                .size(20.dp)
                .clickable { }
        ) {
            val stroke = 2.dp.toPx()
            drawCircle(Color.White, radius = size.width * 0.35f, center = Offset(size.width * 0.4f, size.height * 0.4f), style = Stroke(stroke))
            drawLine(Color.White, Offset(size.width * 0.65f, size.height * 0.65f), Offset(size.width * 0.95f, size.height * 0.95f), stroke, StrokeCap.Round)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Notification Icon (@drawable/ghanti)
        Image(
            painter = painterResource(id = R.drawable.ghanti),
            contentDescription = "Notifications",
            modifier = Modifier
                .size(22.dp)
                .clickable { }
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Message Icon
        Canvas(
            modifier = Modifier
                .size(20.dp)
                .clickable { }
        ) {
            val stroke = 1.8.dp.toPx()
            val rectPath = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.2f)
                lineTo(size.width * 0.9f, size.height * 0.2f)
                lineTo(size.width * 0.9f, size.height * 0.7f)
                lineTo(size.width * 0.4f, size.height * 0.7f)
                lineTo(size.width * 0.2f, size.height * 0.9f)
                lineTo(size.width * 0.2f, size.height * 0.7f)
                lineTo(size.width * 0.1f, size.height * 0.7f)
                close()
            }
            drawPath(rectPath, Color.White, style = Stroke(stroke, cap = StrokeCap.Round))
            drawLine(Color.White, Offset(size.width * 0.3f, size.height * 0.4f), Offset(size.width * 0.7f, size.height * 0.4f), stroke, StrokeCap.Round)
            drawLine(Color.White, Offset(size.width * 0.3f, size.height * 0.55f), Offset(size.width * 0.55f, size.height * 0.55f), stroke, StrokeCap.Round)
        }
    }
}

@Composable
private fun CategoryGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        categories.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { category ->
                    CategoryTile(category = category, modifier = Modifier.weight(1f))
                }
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f).height(132.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(category: CommunityCategory, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF222830), Color(0xFF171D22), Color(0xFF11161B))
                )
            )
            .border(1.dp, Color(0x4A657079), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(category.glow.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = category.icon),
                contentDescription = category.title,
                modifier = Modifier.size(42.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = category.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = category.subtitle,
                color = category.glow,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FilterRow(
    selectedIndex: Int,
    onSelectFilter: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) GreenAccent else Color(0xFF11181E))
                    .border(1.dp, if (isSelected) Color(0xFFDFFF6B) else Color(0x332A353D), RoundedCornerShape(9.dp))
                    .clickable { onSelectFilter(index) }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (index != 0) FilterTabIcon(available = index == 2, color = if (isSelected) Color(0xFF101604) else Color(0xFFEAF0F3))
                    Text(
                        text = label,
                        color = if (isSelected) Color(0xFF101604) else Color(0xFFEAF0F3),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterTabIcon(available: Boolean, color: Color) {
    Canvas(modifier = Modifier.size(16.dp)) {
        val stroke = 1.7.dp.toPx()
        if (available) {
            drawCircle(color, radius = size.width * 0.18f, center = Offset(size.width * 0.5f, size.height * 0.28f), style = Stroke(stroke))
            drawArc(color, 180f, 180f, false, Offset(size.width * 0.17f, size.height * 0.48f), Size(size.width * 0.66f, size.height * 0.43f), style = Stroke(stroke, cap = StrokeCap.Round))
        } else {
            drawCircle(color, radius = size.width * 0.29f, center = Offset(size.width * 0.43f, size.height * 0.43f), style = Stroke(stroke))
            drawLine(color, Offset(size.width * 0.64f, size.height * 0.64f), Offset(size.width * 0.9f, size.height * 0.9f), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun OpportunityCard(opportunity: Opportunity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF10171D), Color(0xFF0B1218), Color(0xFF10171B))
                )
            )
            .border(1.dp, Color(0x332A3840), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (opportunity.profilePhotoUrl != null) {
                AsyncImage(
                    model = opportunity.profilePhotoUrl,
                    contentDescription = "${opportunity.name} profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(42.dp).clip(CircleShape)
                        .border(1.5.dp, opportunity.accent, CircleShape)
                )
            } else {
                BlankAvatar(accent = opportunity.accent)
            }

            Spacer(modifier = Modifier.width(9.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = opportunity.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = opportunity.role,
                            color = opportunity.accent,
                            fontSize = 8.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = opportunity.age,
                        color = MutedText,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(start = 5.dp, top = 1.dp)
                    )
                }

                if (opportunity.description.isNotBlank() && opportunity.price == null) {
                    Text(
                        text = opportunity.description,
                        color = Color(0xFFC2CDD2),
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(top = 6.dp, end = 4.dp)
                    )
                }

                opportunity.location?.let { loc ->
                    MetaLine(text = loc, modifier = Modifier.padding(top = 7.dp))
                }

                opportunity.league?.let { lg ->
                    MetaLine(text = lg, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (opportunity.price != null) {
                        Text(
                            text = opportunity.price,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (opportunity.meta.isNotBlank()) {
                            Text(
                                text = " /${opportunity.meta}",
                                color = MutedText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        opportunity.bubble?.let { SmallBubble(text = it) }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    ActionPill(label = opportunity.action)
                }
            }
        }
    }
}

@Composable
private fun BlankAvatar(accent: Color) {
    Canvas(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFF0E1419))
    ) {
        drawCircle(color = accent.copy(alpha = 0.18f), radius = size.minDimension * 0.47f, center = center)
        drawCircle(color = accent, radius = size.minDimension * 0.46f, center = center, style = Stroke(width = 1.5.dp.toPx()))
        drawCircle(color = Color.White.copy(alpha = 0.15f), radius = size.minDimension * 0.22f, center = Offset(size.width * 0.5f, size.height * 0.42f), style = Stroke(width = 1.2.dp.toPx()))
        drawArc(
            color = Color.White.copy(alpha = 0.15f),
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(size.width * 0.25f, size.height * 0.52f),
            size = Size(size.width * 0.5f, size.height * 0.42f),
            style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun MetaLine(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            val tint = MutedText
            val path = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.92f)
                cubicTo(size.width * 0.18f, size.height * 0.56f, size.width * 0.24f, size.height * 0.16f, size.width * 0.5f, size.height * 0.16f)
                cubicTo(size.width * 0.76f, size.height * 0.16f, size.width * 0.82f, size.height * 0.56f, size.width * 0.5f, size.height * 0.92f)
            }
            drawPath(path, tint, style = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round))
            drawCircle(tint, radius = 1.2.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.43f))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color(0xFFC8D2CE),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SmallBubble(text: String) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(0xFF11161B))
            .border(1.dp, StrokeBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActionPill(label: String) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(9.dp))
            .border(1.dp, GreenAccent, RoundedCornerShape(9.dp))
            .background(Color(0x201B2404))
            .clickable { }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = GreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PremiumSignalCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF101419), Color(0xFF111A12), Color(0xFF263815))
                )
            )
            .border(1.dp, Color(0x263A462F), RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(16) { i ->
                val x = size.width * (i / 15f)
                drawLine(
                    color = Color.White.copy(alpha = 0.02f),
                    start = Offset(x, 0f),
                    end = Offset(x + size.width * 0.18f, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}
