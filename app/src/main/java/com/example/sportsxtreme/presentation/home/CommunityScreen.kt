package com.example.sportsxtreme.presentation.home

import android.net.Uri
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.annotation.DrawableRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import android.widget.Toast
import android.content.Intent
import org.json.JSONObject
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.components.SportsXtremeAppBar
import com.example.sportsxtreme.presentation.components.GuestAuthDialog
import com.example.sportsxtreme.presentation.auth.MainActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
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
private val ScreenBg = Color(0xFF0A101C)
private val MutedText = Color(0xFF9AA7A3)
private val StrokeBorder = Color(0x334E5B64)

private const val COMMUNITY_DRAFTS_PREFS = "community_form_drafts"
private const val LOOKING_FOR_DRAFT = "looking_for"
private const val SELF_PROFILE_DRAFT = "self_profile"

private fun readCommunityDraft(context: android.content.Context, key: String): JSONObject = runCatching {
    JSONObject(context.getSharedPreferences(COMMUNITY_DRAFTS_PREFS, android.content.Context.MODE_PRIVATE).getString(key, "{}"))
}.getOrDefault(JSONObject())

private fun saveCommunityDraft(context: android.content.Context, key: String, draft: JSONObject) {
    context.getSharedPreferences(COMMUNITY_DRAFTS_PREFS, android.content.Context.MODE_PRIVATE)
        .edit().putString(key, draft.toString()).apply()
}

private fun clearCommunityDraft(context: android.content.Context, key: String) {
    context.getSharedPreferences(COMMUNITY_DRAFTS_PREFS, android.content.Context.MODE_PRIVATE)
        .edit().remove(key).apply()
}

private data class CommunityCategory(
    val title: String,
    val subtitle: String,
    @get:DrawableRes val icon: Int,
    val glow: Color
)

private data class Opportunity(
    val id: String = "",
    val ownerId: String? = null,
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
    val profilePhotoUrl: String? = null,
    val isRequest: Boolean = false,
    val organization: String? = null,
    val experience: String? = null,
    val groundType: String? = null,
    val ballType: String? = null,
    val chargePerMatch: String? = null,
    val requestTitle: String? = null,
    val requestDate: String? = null,
    val requestTime: String? = null,
    val requestFormat: String? = null
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

private val filters = listOf("All", "Looking for", "Available")

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CommunityScreen(
    onMenuClick: () -> Unit = {}
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateOptions by remember { mutableStateOf(false) }
    var showProfileForm by remember { mutableStateOf(false) }
    var showLookingForForm by remember { mutableStateOf(false) }
    var guestPromptDetail by remember { mutableStateOf<String?>(null) }
    val createOptionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val profileFormSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lookingForSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val communityProfiles = remember { mutableStateListOf<Opportunity>() }
    val ownerPhotoUrls = remember { mutableStateMapOf<String, String>() }
    val currentUser = FirebaseAuth.getInstance().currentUser
    var accountPhotoUrl by remember(currentUser?.uid) { mutableStateOf(currentUser?.photoUrl?.toString()) }
    var isPostingRequest by remember { mutableStateOf(false) }
    var isPostingProfile by remember { mutableStateOf(false) }
    var selectedContactProfile by remember { mutableStateOf<Opportunity?>(null) }
    var contactWhatsappNumber by remember { mutableStateOf<String?>(null) }
    var isLoadingContact by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { userDocument ->
                val photo = userDocument.getString("profilePhotoUrl")?.takeIf { it.isNotBlank() }
                if (photo != null) {
                    accountPhotoUrl = photo
                } else {
                    FirebaseFirestore.getInstance().collection("users")
                        .whereEqualTo("id", uid)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { querySnap ->
                            val legacyPhoto = querySnap.documents.firstOrNull()
                                ?.getString("profilePhotoUrl")?.takeIf { it.isNotBlank() }
                            accountPhotoUrl = legacyPhoto ?: currentUser.photoUrl?.toString()
                        }
                        .addOnFailureListener {
                            accountPhotoUrl = currentUser.photoUrl?.toString()
                        }
                }
            }
            .addOnFailureListener {
                accountPhotoUrl = currentUser.photoUrl?.toString()
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
                        val isRequest = data["postType"] == "request"
                        val ownerId = data["ownerId"] as? String
                        val docPhotoUrl = (data["profilePhotoUrl"] as? String)?.trim()?.takeIf { it.isNotBlank() }

                        if (docPhotoUrl != null && ownerId != null) {
                            ownerPhotoUrls[ownerId] = docPhotoUrl
                        } else if (ownerId != null && !ownerPhotoUrls.containsKey(ownerId)) {
                            if (ownerId == currentUser?.uid && !accountPhotoUrl.isNullOrBlank()) {
                                ownerPhotoUrls[ownerId] = accountPhotoUrl!!
                            } else {
                                FirebaseFirestore.getInstance().collection("users").document(ownerId).get()
                                    .addOnSuccessListener { userDoc ->
                                        val photo = userDoc.getString("profilePhotoUrl")?.trim()?.takeIf { it.isNotBlank() }
                                        if (photo != null) {
                                            ownerPhotoUrls[ownerId] = photo
                                        } else {
                                            FirebaseFirestore.getInstance().collection("users")
                                                .whereEqualTo("id", ownerId)
                                                .limit(1)
                                                .get()
                                                .addOnSuccessListener { querySnap ->
                                                    val legacyPhoto = querySnap.documents.firstOrNull()
                                                        ?.getString("profilePhotoUrl")?.trim()?.takeIf { it.isNotBlank() }
                                                    if (legacyPhoto != null) {
                                                        ownerPhotoUrls[ownerId] = legacyPhoto
                                                    }
                                                }
                                        }
                                    }
                            }
                        }

                        Opportunity(
                            id = document.id,
                            ownerId = ownerId,
                            name = if (isRequest) data["title"] as? String ?: "Community request" else data["name"] as? String ?: "Community member",
                            role = (data["category"] as? String ?: "PLAYER").uppercase(),
                            description = if (isRequest) data["description"] as? String ?: "" else listOfNotNull(
                                (data["organization"] as? String)?.takeIf { it.isNotBlank() },
                                (data["experience"] as? String)?.takeIf { it.isNotBlank() }?.let { "$it experience" }
                            ).joinToString(" · "),
                            meta = if (isRequest) listOfNotNull((data["date"] as? String)?.let { "Date: $it" }, (data["time"] as? String)?.takeIf { it.isNotBlank() }?.let { "Time: $it" }).joinToString(" · ").ifBlank { "Looking for" } else "Match",
                            price = (data["chargePerMatch"] as? String)?.takeIf { it.isNotBlank() }?.let { "₹$it" },
                            action = if (isRequest) "Respond" else "Connect",
                            age = createdAt?.let { timeAgo(it.toDate().time) } ?: "Just now",
                            accent = GreenAccent,
                            location = (data["location"] as? String)?.takeIf { it.isNotBlank() },
                            league = if (isRequest) (data["format"] as? String)?.takeIf { it.isNotBlank() } else listOfNotNull(
                                (data["groundType"] as? String)?.let { "Ground: $it" },
                                (data["ballType"] as? String)?.let { "Ball: $it" }
                            ).joinToString(" · ").ifBlank { null },
                            profilePhotoUrl = docPhotoUrl,
                            isRequest = isRequest,
                            organization = (data["organization"] as? String)?.takeIf { it.isNotBlank() },
                            experience = (data["experience"] as? String)?.takeIf { it.isNotBlank() },
                            groundType = (data["groundType"] as? String)?.takeIf { it.isNotBlank() },
                            ballType = (data["ballType"] as? String)?.takeIf { it.isNotBlank() },
                            chargePerMatch = (data["chargePerMatch"] as? String)?.takeIf { it.isNotBlank() },
                            requestTitle = if (isRequest) data["title"] as? String else null,
                            requestDate = if (isRequest) data["date"] as? String else null,
                            requestTime = if (isRequest) data["time"] as? String else null,
                            requestFormat = if (isRequest) data["format"] as? String else null
                        )
                    })
                }
            }
        onDispose { registration.remove() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A101C))
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(Color(0x26C1FF00), Color.Transparent),
                        center = Offset(size.width / 2f, -50f),
                        radius = size.height * 0.6f
                    )
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SportsXtremeAppBar(
                onMenuClick = onMenuClick,
                showSearch = true,
                onSearchClick = { isSearchExpanded = true },
                searchExpanded = isSearchExpanded,
                searchQuery = searchQuery,
                searchPlaceholder = "Players, roles, or listings",
                onSearchQueryChange = { searchQuery = it },
                onSearchClose = {
                    searchQuery = ""
                    isSearchExpanded = false
                }
            )
            Spacer(Modifier.height(10.dp))
            FilterRow(selectedIndex = selectedFilterIndex, onSelectFilter = { selectedFilterIndex = it })
            AnimatedContent(
                targetState = selectedFilterIndex,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    (slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn()) togetherWith
                        (slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut())
                },
                label = "community-tab-content"
            ) { tabIndex ->
                val displayedProfiles = remember(tabIndex, communityProfiles.toList(), searchQuery) {
                    val query = searchQuery.trim().lowercase()
                    val tabProfiles = if (query.isNotBlank()) communityProfiles.toList() else when (tabIndex) {
                        1 -> communityProfiles.filter { it.isRequest }
                        2 -> communityProfiles.filterNot { it.isRequest }
                        else -> communityProfiles.toList()
                    }
                    if (query.isBlank()) tabProfiles else tabProfiles.filter { profile ->
                        val searchable = listOf(
                            profile.name, profile.role, profile.description, profile.organization.orEmpty(),
                            profile.location.orEmpty(), profile.requestTitle.orEmpty(), profile.requestFormat.orEmpty()
                        ).joinToString(" ").lowercase()
                        val normalizedQuery = when (query) {
                            "wk", "keeper", "wicketkeeper" -> "wicket keeper"
                            "allrounder", "all-rounder" -> "all rounder"
                            else -> query
                        }
                        searchable.contains(query) || searchable.contains(normalizedQuery)
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().pointerInput(tabIndex) {
                        var horizontalDrag = 0f
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                horizontalDrag += dragAmount
                            },
                            onDragEnd = {
                                if (horizontalDrag < -72f && tabIndex < filters.lastIndex) selectedFilterIndex = tabIndex + 1
                                if (horizontalDrag > 72f && tabIndex > 0) selectedFilterIndex = tabIndex - 1
                                horizontalDrag = 0f
                            },
                            onDragCancel = { horizontalDrag = 0f }
                        )
                    },
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = filters[tabIndex],
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }
                    if (displayedProfiles.isEmpty()) {
                        item {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No community listings match this search." else when (tabIndex) {
                                    1 -> "No one is looking for an opportunity right now."
                                    2 -> "No self profiles are available yet."
                                    else -> "No community posts yet. Be the first to add your profile."
                                },
                                color = Color(0xFF9AA7A3),
                                fontSize = 13.sp,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                            )
                        }
                    }
                    items(displayedProfiles, key = { it.id }) { opportunity ->
                        OpportunityCard(
                            opportunity = opportunity,
                            ownerPhotoUrls = ownerPhotoUrls,
                            currentUserId = currentUser?.uid,
                            accountPhotoUrl = accountPhotoUrl,
                            onGuestAction = { guestPromptDetail = it },
                            onConnect = { profile ->
                                if (FirebaseAuth.getInstance().currentUser == null) {
                                    guestPromptDetail = "Login or sign up to respond to Community posts."
                                    return@OpportunityCard
                                }
                                selectedContactProfile = profile
                                contactWhatsappNumber = null
                                isLoadingContact = true
                                FirebaseFirestore.getInstance()
                                    .collection("communityProfiles").document(profile.id)
                                    .collection("private").document("contact").get()
                                    .addOnSuccessListener { doc ->
                                        contactWhatsappNumber = doc.getString("whatsappNumber")?.takeIf { it.isNotBlank() }
                                        isLoadingContact = false
                                    }
                                    .addOnFailureListener { error ->
                                        isLoadingContact = false
                                        Toast.makeText(context, "Could not load contact details: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(34.dp)) }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 92.dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(GreenAccent)
                .clickable {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        guestPromptDetail = "Login or sign up to post requirements or your profile."
                    } else showCreateOptions = true
                },
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
                    onLookingFor = {
                        showCreateOptions = false
                        showLookingForForm = true
                    },
                    onAddProfile = {
                        showCreateOptions = false
                        showProfileForm = true
                    },
                    onCancel = { showCreateOptions = false }
                )
            }
        }

        if (showLookingForForm) {
            ModalBottomSheet(
                onDismissRequest = { showLookingForForm = false },
                sheetState = lookingForSheetState,
                containerColor = Color(0xFF08131D),
                contentColor = Color.White,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp, bottom = 2.dp)
                            .width(38.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE7F0F8))
                    )
                }
            ) {
                LookingForOpportunityScreen(
                    modifier = Modifier.fillMaxSize(),
                    isPosting = isPostingRequest,
                    onBack = {
                        showLookingForForm = false
                        showCreateOptions = true
                    },
                    onPost = { category, title, description, date, time, location, format, whatsappNumber, whatsappConsent ->
                        if (isPostingRequest) return@LookingForOpportunityScreen
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user == null) {
                            Toast.makeText(context, "Sign in to post a Community request.", Toast.LENGTH_LONG).show()
                        } else {
                            isPostingRequest = true
                            val db = FirebaseFirestore.getInstance()

                            fun publishRequest(photoUrl: String) {
                                val requestRef = db.collection("communityProfiles").document()
                                val batch = db.batch()
                                batch.set(requestRef, mapOf(
                                    "postType" to "request",
                                    "ownerId" to user.uid,
                                    "name" to user.displayName.orEmpty().ifBlank { "Community member" },
                                    "title" to title,
                                    "category" to category,
                                    "description" to description,
                                    "date" to date,
                                    "time" to time,
                                    "location" to location,
                                    "format" to format,
                                    "profilePhotoUrl" to photoUrl,
                                    "whatsappConsent" to whatsappConsent,
                                    "createdAt" to FieldValue.serverTimestamp()
                                ))
                                batch.set(requestRef.collection("private").document("contact"), mapOf(
                                    "ownerId" to user.uid,
                                    "whatsappNumber" to whatsappNumber
                                ))
                                batch.commit()
                                    .addOnSuccessListener {
                                        isPostingRequest = false
                                        Toast.makeText(context, "Your request is now on Community.", Toast.LENGTH_SHORT).show()
                                        clearCommunityDraft(context, LOOKING_FOR_DRAFT)
                                        showLookingForForm = false
                                    }
                                    .addOnFailureListener { error ->
                                        isPostingRequest = false
                                        Toast.makeText(context, "Could not post request: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                            }

                            val fallbackUrl = accountPhotoUrl?.takeIf { it.isNotBlank() } ?: user.photoUrl?.toString().orEmpty()

                            db.collection("users").document(user.uid).get()
                                .addOnSuccessListener { userDocument ->
                                    val fetchedUrl = userDocument.getString("profilePhotoUrl")
                                        ?.takeIf { it.isNotBlank() }
                                        ?: fallbackUrl
                                    publishRequest(fetchedUrl)
                                }
                                .addOnFailureListener {
                                    publishRequest(fallbackUrl)
                                }
                        }
                    }
                )
            }
        }

        guestPromptDetail?.let { detail ->
            GuestAuthDialog(
                detail = detail,
                onDismiss = { guestPromptDetail = null },
                onSignup = { guestPromptDetail = null; (context as? MainActivity)?.showSignupScreen() },
                onLogin = { guestPromptDetail = null; (context as? MainActivity)?.showLoginScreen() }
            )
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
                    isPosting = isPostingProfile,
                    onBack = {
                        showProfileForm = false
                        showCreateOptions = true
                    },
                    onPost = { name, category, location, organization, experience, groundType, ballType, chargePerMatch, whatsappNumber, whatsappConsent, photoUri ->
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user == null) {
                            Toast.makeText(context, "Sign in to post a Community profile.", Toast.LENGTH_LONG).show()
                        } else if (!isPostingProfile) {
                            isPostingProfile = true
                            scope.launch {
                                val photoUrl = if (photoUri != null) {
                                    CloudinaryTournamentImageUploader(context.applicationContext)
                                        .upload(photoUri, "community-${UUID.randomUUID()}", "logo")
                                        .getOrElse { error ->
                                            isPostingProfile = false
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
                                        isPostingProfile = false
                                        Toast.makeText(context, "Your profile is now on Community.", Toast.LENGTH_SHORT).show()
                                        clearCommunityDraft(context, SELF_PROFILE_DRAFT)
                                        showProfileForm = false
                                    }
                                    .addOnFailureListener { error ->
                                        isPostingProfile = false
                                        Toast.makeText(context, "Could not post profile: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                    }
                )
            }
        }

        selectedContactProfile?.let { profile ->
            ModalBottomSheet(
                onDismissRequest = { selectedContactProfile = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color(0xFF061018),
                contentColor = Color.White,
                dragHandle = null,
            ) {
                ContactDetailsSheet(
                    profile = profile,
                    photoUrl = profile.profilePhotoUrl?.takeIf { it.isNotBlank() }
                        ?: profile.ownerId?.let { ownerPhotoUrls[it] }
                        ?: if (profile.ownerId == currentUser?.uid) accountPhotoUrl else null,
                    whatsappNumber = contactWhatsappNumber,
                    isLoading = isLoadingContact,
                    onBack = { selectedContactProfile = null }
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
private fun LookingForOpportunityScreen(
    modifier: Modifier = Modifier,
    isPosting: Boolean,
    onBack: () -> Unit,
    onPost: (String, String, String, String, String, String, String, String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val draft = remember { readCommunityDraft(context, LOOKING_FOR_DRAFT) }
    var category by remember { mutableStateOf(draft.optString("category")) }
    var title by remember { mutableStateOf(draft.optString("title")) }
    var description by remember { mutableStateOf(draft.optString("description")) }
    var date by remember { mutableStateOf(draft.optString("date")) }
    var time by remember { mutableStateOf(draft.optString("time")) }
    var location by remember { mutableStateOf(draft.optString("location")) }
    var format by remember { mutableStateOf(draft.optString("format")) }
    var whatsappNumber by remember { mutableStateOf(draft.optString("whatsappNumber")) }
    var whatsappConsent by remember { mutableStateOf(draft.optBoolean("whatsappConsent")) }
    var openMenu by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val categories = listOf("Batter", "Bowler", "All Rounder", "Wicket Keeper", "Umpire")

    LaunchedEffect(category, title, description, date, time, location, format, whatsappNumber, whatsappConsent) {
        saveCommunityDraft(context, LOOKING_FOR_DRAFT, JSONObject().apply {
            put("category", category); put("title", title); put("description", description)
            put("date", date); put("time", time); put("location", location); put("format", format)
            put("whatsappNumber", whatsappNumber); put("whatsappConsent", whatsappConsent)
        })
    }

    Box(modifier.fillMaxSize().background(Color(0xFF08131D)).navigationBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(Modifier.weight(1f).padding(start = 4.dp)) {
                    Text("Looking for Someone", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("Post an opportunity for match, team, players, ground, and more.", color = Color(0xFFA8B9C8), fontSize = 11.sp)
                }
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 5.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                OpportunityDropdownField("I am looking for", category, "Select category", categories, true, Icons.Filled.GridView, openMenu, { openMenu = it }, { category = it })
                OpportunityTextField("Title", title, { title = it }, "e.g., Need Umpire for Weekend Match", true, Icons.Filled.Business)
                OpportunityTextField("Description", description, { if (it.length <= 500) description = it }, "Provide more details (e.g., match format, date, requirements, etc.)", true, Icons.Filled.Description, minLines = 3, counter = "${description.length}/500")
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    OpportunityDateTimeField("Date", date.ifBlank { "Select date" }, true, Icons.Filled.CalendarMonth, Modifier.weight(1f)) {
                        val now = Calendar.getInstance()
                        DatePickerDialog(context, { _, year, month, day -> date = "%02d/%02d/%04d".format(day, month + 1, year) }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
                    }
                    OpportunityDateTimeField("Time", time.ifBlank { "Select time" }, false, Icons.Filled.AccessTime, Modifier.weight(1f)) {
                        val now = Calendar.getInstance()
                        TimePickerDialog(context, { _, hour, minute -> time = "%02d:%02d".format(hour, minute) }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show()
                    }
                }
                OpportunityTextField("Location", location, { location = it }, "Enter location (e.g., Kalinga Stadium, Bhubaneswar)", true, Icons.Filled.Place)
                OpportunityTextField("Match Type / Format", format, { format = it }, "e.g., T20 / League / Practice / Tournament", false, Icons.Filled.SportsEsports, optional = true)
                OpportunityTextField("WhatsApp Number", whatsappNumber, { whatsappNumber = it }, "Enter your WhatsApp number", true, icon = null, iconRes = R.drawable.whatsappicon, keyboardType = KeyboardType.Phone)
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
                    WhatsAppConsentText(
                        modifier = Modifier.weight(1f).padding(top = 12.dp),
                        onToggleConsent = { whatsappConsent = !whatsappConsent }
                    )
                }
                if (error.isNotBlank()) Text(error, color = Color(0xFFFF6878), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp))
            }
            Box(
                Modifier.fillMaxWidth().padding(start = 5.dp, end = 5.dp, top = 8.dp, bottom = 14.dp).height(46.dp)
                    .clip(RoundedCornerShape(11.dp)).background(GreenAccent.copy(alpha = if (isPosting) 0.65f else 1f)).clickable(enabled = !isPosting) {
                        error = when {
                            category.isBlank() -> "Select what you are looking for."
                            title.isBlank() -> "Enter a title."
                            description.isBlank() -> "Add a short description."
                            date.isBlank() -> "Select a date."
                            location.isBlank() -> "Enter a location."
                            whatsappNumber.filter(Char::isDigit).length !in 10..12 -> "Enter a valid WhatsApp number."
                            !whatsappConsent -> "Please consent to WhatsApp contact before posting."
                            else -> ""
                        }
                        if (error.isBlank()) onPost(category, title.trim(), description.trim(), date, time, location.trim(), format.trim(), whatsappNumber.trim(), whatsappConsent)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isPosting) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF111800), strokeWidth = 2.dp)
                        Text("Posting…", color = Color(0xFF111800), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }
                } else Text("Post", color = Color(0xFF111800), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun OpportunityDateTimeField(label: String, value: String, required: Boolean, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp)).background(Color(0xFF0D1A25)).border(1.dp, Color(0x334E5B64), RoundedCornerShape(11.dp)).clickable(onClick = onClick).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(21.dp))
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            ProfileFieldLabel(label, required, !required)
            Row(Modifier.fillMaxWidth().height(34.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF0A1620)).border(1.dp, Color(0xFF263947), RoundedCornerShape(8.dp)).padding(horizontal = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(value, color = if (value.startsWith("Select")) Color(0xFF9DAFBD) else Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Icon(icon, null, tint = Color(0xFFDCE7EF), modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun OpportunityTextField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, required: Boolean, icon: ImageVector?, minLines: Int = 1, counter: String? = null, optional: Boolean = false, iconRes: Int? = null, keyboardType: KeyboardType = KeyboardType.Text) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp)).background(Color(0xFF0D1A25)).border(1.dp, Color(0x334E5B64), RoundedCornerShape(11.dp)).padding(10.dp), verticalAlignment = Alignment.Top) {
        if (iconRes != null) Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.padding(top = 16.dp).size(21.dp))
        else if (icon != null) Icon(icon, null, tint = Color.White, modifier = Modifier.padding(top = 16.dp).size(21.dp))
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            ProfileFieldLabel(label, required, optional)
            OutlinedTextField(value = value, onValueChange = onValueChange, placeholder = { Text(placeholder, color = Color(0xFF8295A5), fontSize = 11.sp) }, singleLine = minLines == 1, minLines = minLines, keyboardOptions = KeyboardOptions(keyboardType = keyboardType), leadingIcon = if (keyboardType == KeyboardType.Phone) ({ Text("+91", color = Color.White, fontSize = 11.sp) }) else null, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF314656), unfocusedBorderColor = Color(0xFF263947), cursorColor = GreenAccent), textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp))
            if (counter != null) Text(counter, color = Color(0xFF8295A5), fontSize = 10.sp, modifier = Modifier.align(Alignment.End).padding(end = 4.dp, top = 2.dp))
        }
    }
}

@Composable
private fun OpportunityDropdownField(label: String, value: String, placeholder: String, options: List<String>, required: Boolean, icon: ImageVector, openMenu: String, onExpand: (String) -> Unit, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp)).background(Color(0xFF0D1A25)).border(1.dp, Color(0x334E5B64), RoundedCornerShape(11.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(21.dp))
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            ProfileFieldLabel(label, required, false)
            Box {
                Row(Modifier.fillMaxWidth().height(38.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, if (value.isBlank()) GreenAccent else Color(0xFF263947), RoundedCornerShape(8.dp)).clickable { onExpand(if (openMenu == label) "" else label) }.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(value.ifBlank { placeholder }, color = if (value.isBlank()) Color(0xFF9DAFBD) else Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text("⌄", color = Color.White, fontSize = 18.sp)
                }
                DropdownMenu(expanded = openMenu == label, onDismissRequest = { onExpand("") }) { options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); onExpand("") }) } }
            }
        }
    }
}

@Composable
private fun AddSelfProfileForm(
    initialPhotoUrl: String?,
    isPosting: Boolean,
    onBack: () -> Unit,
    onPost: (String, String, String, String, String, String, String, String, String, Boolean, Uri?) -> Unit
) {
    val context = LocalContext.current
    val draft = remember { readCommunityDraft(context, SELF_PROFILE_DRAFT) }
    var fullName by remember { mutableStateOf(draft.optString("fullName")) }
    var category by remember { mutableStateOf(draft.optString("category")) }
    var location by remember { mutableStateOf(draft.optString("location")) }
    var organization by remember { mutableStateOf(draft.optString("organization")) }
    var experience by remember { mutableStateOf(draft.optString("experience")) }
    var groundType by remember { mutableStateOf(draft.optString("groundType")) }
    var ballType by remember { mutableStateOf(draft.optString("ballType")) }
    var chargePerMatch by remember { mutableStateOf(draft.optString("chargePerMatch")) }
    var whatsappNumber by remember { mutableStateOf(draft.optString("whatsappNumber")) }
    var whatsappConsent by remember { mutableStateOf(draft.optBoolean("whatsappConsent")) }
    var selectedPhotoUri by remember { mutableStateOf(draft.optString("selectedPhotoUri").takeIf { it.isNotBlank() }?.let(Uri::parse)) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        selectedPhotoUri = uri
    }
    var openMenu by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf("") }
    val categories = listOf("Batter", "Bowler", "AllRounder", "Wicket keeper", "Umpire")
    val groundTypes = listOf("Turf", "Matting", "Concrete", "Grass", "Indoor")
    val ballTypes = listOf("Leather", "Tennis", "Tape Ball", "Other")

    LaunchedEffect(fullName, category, location, organization, experience, groundType, ballType, chargePerMatch, whatsappNumber, whatsappConsent, selectedPhotoUri) {
        saveCommunityDraft(context, SELF_PROFILE_DRAFT, JSONObject().apply {
            put("fullName", fullName); put("category", category); put("location", location)
            put("organization", organization); put("experience", experience); put("groundType", groundType)
            put("ballType", ballType); put("chargePerMatch", chargePerMatch); put("whatsappNumber", whatsappNumber)
            put("whatsappConsent", whatsappConsent); put("selectedPhotoUri", selectedPhotoUri?.toString().orEmpty())
        })
    }

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
            WhatsAppConsentText(
                modifier = Modifier.weight(1f).padding(top = 12.dp),
                onToggleConsent = { whatsappConsent = !whatsappConsent }
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
                .background(GreenAccent.copy(alpha = if (isPosting) 0.65f else 1f))
                .clickable(enabled = !isPosting) {
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
            if (isPosting) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF111800), strokeWidth = 2.dp)
                    Text("Posting…", color = Color(0xFF111800), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            } else Text("Post", color = Color(0xFF111800), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun WhatsAppConsentText(
    modifier: Modifier = Modifier,
    onToggleConsent: () -> Unit
) {
    val context = LocalContext.current
    val text = buildAnnotatedString {
        append("I consent to sharing my WhatsApp number with users who wish to contact me regarding this listing. My number will not be displayed publicly, but tapping the WhatsApp button will allow users to contact me directly via WhatsApp. For more, check ")
        pushStringAnnotation(tag = "privacy-policy", annotation = "open")
        withStyle(
            SpanStyle(
                color = GreenAccent,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Privacy Policy")
        }
        pop()
        append(".")
    }
    ClickableText(
        text = text,
        modifier = modifier,
        style = TextStyle(color = Color(0xFFB6C4CF), fontSize = 10.sp, lineHeight = 14.sp),
        onClick = { offset ->
            val isPolicyLink = text.getStringAnnotations("privacy-policy", offset, offset).isNotEmpty()
            if (isPolicyLink) {
                context.startActivity(WebPageActivity.intent(context, "/app-privacy-policy"))
            } else {
                onToggleConsent()
            }
        }
    )
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
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) GreenAccent else Color(0xFF11181E),
                label = "community-tab-background-$index"
            )
            val foregroundColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF101604) else Color(0xFFEAF0F3),
                label = "community-tab-foreground-$index"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(backgroundColor)
                    .border(1.dp, if (isSelected) Color(0xFFDFFF6B) else Color(0x332A353D), RoundedCornerShape(9.dp))
                    .clickable { onSelectFilter(index) }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (index != 0) FilterTabIcon(available = index == 2, color = foregroundColor)
                    Text(
                        text = label,
                        color = foregroundColor,
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
private fun OpportunityCard(
    opportunity: Opportunity,
    ownerPhotoUrls: Map<String, String>,
    currentUserId: String?,
    accountPhotoUrl: String?,
    onGuestAction: (String) -> Unit,
    onConnect: (Opportunity) -> Unit
) {
    val context = LocalContext.current
    var menuExpanded by remember(opportunity.id) { mutableStateOf(false) }
    var confirmDelete by remember(opportunity.id) { mutableStateOf(false) }
    var showReportSheet by remember(opportunity.id) { mutableStateOf(false) }
    val isOwner = currentUserId != null && opportunity.ownerId == currentUserId
    val rawPhotoUrl = opportunity.profilePhotoUrl?.takeIf { it.isNotBlank() }
        ?: opportunity.ownerId?.let { ownerPhotoUrls[it] }
        ?: if (opportunity.ownerId == currentUserId) accountPhotoUrl?.takeIf { it.isNotBlank() } else null

    val photoUrl = rawPhotoUrl?.trim()?.let { rawUrl ->
        val secureUrl = rawUrl.replaceFirst("http://", "https://", ignoreCase = true)
            .let { if (it.startsWith("//")) "https:$it" else it }
        if (secureUrl.contains("/image/upload/", ignoreCase = true) &&
            !secureUrl.contains("/image/upload/c_fill,g_auto,h_512,w_512,f_jpg,q_auto/", ignoreCase = true)
        ) secureUrl.replace("/image/upload/", "/image/upload/c_fill,g_auto,h_512,w_512,f_jpg,q_auto/", ignoreCase = true)
        else secureUrl
    }
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
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(photoUrl).build(),
                    contentDescription = "${opportunity.name} profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(42.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
                        .clip(CircleShape)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = opportunity.age, color = MutedText, fontSize = 9.sp, modifier = Modifier.padding(start = 5.dp, top = 1.dp))
                        Box {
                            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Post options", tint = Color.White, modifier = Modifier.size(19.dp))
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                if (isOwner) {
                                    DropdownMenuItem(text = { Text("Delete") }, onClick = { menuExpanded = false; confirmDelete = true })
                                }
                                DropdownMenuItem(text = { Text("Share") }, onClick = {
                                    menuExpanded = false
                                    val shareText = "${opportunity.name} · ${opportunity.role}\n${opportunity.description}\n${opportunity.location.orEmpty()}".trim()
                                    val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) }
                                    context.startActivity(Intent.createChooser(intent, "Share community post"))
                                })
                                if (!isOwner) {
                                    DropdownMenuItem(text = { Text("Report") }, onClick = {
                                        menuExpanded = false
                                        if (FirebaseAuth.getInstance().currentUser == null) {
                                            onGuestAction("Login or sign up to report this Community post.")
                                        } else showReportSheet = true
                                    })
                                }
                            }
                        }
                    }
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

                    ActionPill(label = opportunity.action) {
                        onConnect(opportunity)
                    }
                }
            }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete post?") },
            text = { Text("This community post will be permanently deleted.") },
            confirmButton = { TextButton(onClick = {
                confirmDelete = false
                FirebaseFirestore.getInstance().collection("communityProfiles").document(opportunity.id)
                    .delete()
                    .addOnSuccessListener { Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { error -> Toast.makeText(context, "Could not delete post: ${error.localizedMessage}", Toast.LENGTH_LONG).show() }
            }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
    if (showReportSheet) {
        ReportListingSheet(
            onDismiss = { showReportSheet = false },
            onSubmit = { reason, description, onComplete, onFailure ->
                val reporterId = currentUserId
                if (reporterId == null || opportunity.ownerId == null) {
                    onFailure("Sign in to report this listing")
                } else {
                    val reportId = "${opportunity.id}_$reporterId"
                    val reportRef = FirebaseFirestore.getInstance().collection("communityPostReports").document(reportId)
                    reportRef.get().addOnSuccessListener { existingReport ->
                        if (existingReport.exists()) {
                            onFailure("You have already reported this listing")
                            return@addOnSuccessListener
                        }
                        reportRef.set(mapOf(
                            "postId" to opportunity.id,
                            "postOwnerId" to opportunity.ownerId,
                            "reporterId" to reporterId,
                            "reason" to reason,
                            "description" to description,
                            "status" to "OPEN",
                            "createdAt" to FieldValue.serverTimestamp()
                        ))
                        .addOnSuccessListener { onComplete() }
                        .addOnFailureListener { error -> onFailure(
                            "Could not submit report. You may already have reported this listing."
                        ) }
                    }.addOnFailureListener { error -> onFailure("Could not check previous report: ${error.localizedMessage}") }
                }
            }
        )
    }
}

private val reportReasons = listOf(
    "Spam or misleading listing",
    "Fake identity or impersonation",
    "Inappropriate or offensive content",
    "Phone number or contact misuse",
    "Fraud or scam",
    "Not sports-related",
    "Duplicate listing",
    "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportListingSheet(
    onDismiss: () -> Unit,
    onSubmit: (String, String, () -> Unit, (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        containerColor = Color(0xFF101A24),
        contentColor = Color.White,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Report Listing", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Select the reason that best describes the issue.", color = MutedText, fontSize = 12.sp)
            reportReasons.forEach { reason ->
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !isSubmitting) { selectedReason = reason }
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedReason == reason, onClick = { selectedReason = reason }, enabled = !isSubmitting)
                    Text(reason, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 1000) description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Additional details (optional)") },
                minLines = 3,
                maxLines = 5,
                enabled = !isSubmitting
            )
            errorMessage?.let { Text(it, color = Color(0xFFFF7886), fontSize = 12.sp) }
            Button(
                onClick = {
                    val reason = selectedReason ?: run { errorMessage = "Select a reason to continue"; return@Button }
                    isSubmitting = true
                    errorMessage = null
                    onSubmit(reason, description.trim(), {
                        isSubmitting = false
                        onDismiss()
                        Toast.makeText(context, "Report submitted for review", Toast.LENGTH_SHORT).show()
                    }, { message ->
                        isSubmitting = false
                        errorMessage = message
                    })
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent, contentColor = Color(0xFF111800))
            ) {
                Text(if (isSubmitting) "Submitting…" else "Submit Report", fontWeight = FontWeight.Bold)
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
private fun ActionPill(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(9.dp))
            .border(1.dp, GreenAccent, RoundedCornerShape(9.dp))
            .background(Color(0x201B2404))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = GreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ContactDetailsSheet(
    profile: Opportunity,
    photoUrl: String?,
    whatsappNumber: String?,
    isLoading: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF061018)).navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column(Modifier.padding(start = 7.dp)) {
                Text(
                    if (profile.isRequest) "Opportunity Details" else "Contact Details",
                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    if (profile.isRequest) "Review the requirement and contact the poster" else "Reach out to the opportunity owner",
                    color = Color(0xFF9FB3C9), fontSize = 12.sp
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF07141D))
                    .border(1.dp, Color(0xFF16303A), RoundedCornerShape(12.dp)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(photoUrl).build(),
                        contentDescription = "${profile.name} profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(76.dp).clip(CircleShape).border(2.dp, GreenAccent, CircleShape)
                    )
                } else {
                    BlankAvatar(accent = GreenAccent)
                }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(profile.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(profile.role, color = GreenAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    if (profile.isRequest) Text("◷  ${profile.age}", color = Color(0xFF9FB3C9), fontSize = 10.sp)
                    profile.location?.let { MetaLine(it, Modifier.padding(top = 7.dp)) }
                }
            }

            Text(
                if (profile.isRequest) "Requirement Details" else "Opportunity Details",
                color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold
            )
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF07141D))
                    .border(1.dp, Color(0xFF16303A), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp)
            ) {
                if (profile.isRequest) {
                    ContactDetailRow(R.drawable.outline_groups_24, "Category", profile.role)
                    profile.requestTitle?.takeIf { it.isNotBlank() }?.let {
                        ContactDetailRow(R.drawable.baseline_edit_location_24, "Title", it)
                    }
                    profile.description.takeIf { it.isNotBlank() }?.let {
                        ContactDetailRow(R.drawable.baseline_edit_location_24, "Description", it)
                    }
                    profile.requestDate?.takeIf { it.isNotBlank() }?.let {
                        ContactDetailRow(R.drawable.outline_calendar_add_on_24, "Date", it)
                    }
                    profile.requestTime?.takeIf { it.isNotBlank() }?.let {
                        ContactDetailRow(R.drawable.timer, "Time", it)
                    }
                    profile.location?.let { ContactDetailRow(R.drawable.location, "Location", it) }
                    profile.requestFormat?.takeIf { it.isNotBlank() }?.let {
                        ContactDetailRow(R.drawable.grouping, "Match Type / Format", it)
                    }
                } else {
                    profile.organization?.let { ContactDetailRow(R.drawable.groups, "Organization / Team", it) }
                    profile.experience?.let { ContactDetailRow(R.drawable.trophy1, "Experience", it) }
                    profile.groundType?.let { ContactDetailRow(R.drawable.ground, "Ground Type", it) }
                    profile.ballType?.let { ContactDetailRow(R.drawable.tennisball, "Ball Type", it) }
                    profile.chargePerMatch?.takeIf { it.isNotBlank() }?.let {
                        ContactDetailRow(R.drawable.rupee, "Payment", "₹$it / Match")
                    }
                }
            }

            Text("Contact Information", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF07141D))
                    .border(1.dp, Color(0xFF16303A), RoundedCornerShape(12.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(50.dp).clip(RoundedCornerShape(11.dp)).background(Color(0xFF09251F)), contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.whatsappicon), contentDescription = "WhatsApp", modifier = Modifier.size(29.dp))
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("WhatsApp", color = Color(0xFF9FB3C9), fontSize = 11.sp)
                    Text(
                        when { isLoading -> "Loading contact…"; whatsappNumber != null -> whatsappNumber; else -> "Contact unavailable" },
                        color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    Modifier.widthIn(min = 126.dp).height(46.dp).clip(RoundedCornerShape(11.dp))
                        .background(GreenAccent).clickable(enabled = !isLoading && whatsappNumber != null) {
                            val digits = whatsappNumber.orEmpty().filter(Char::isDigit)
                            runCatching { uriHandler.openUri("https://wa.me/$digits") }
                                .onFailure { Toast.makeText(context, "Could not open WhatsApp.", Toast.LENGTH_SHORT).show() }
                        }.padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.whatsappicon), contentDescription = null, modifier = Modifier.size(22.dp))
                        Text("WhatsApp", color = Color(0xFF10170B), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF07141D))
                    .border(1.dp, Color(0xFF16303A), RoundedCornerShape(12.dp)).padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFAFC9E5), modifier = Modifier.size(24.dp))
                    Text("Contact only for this opportunity.", color = Color(0xFFD7E2EA), fontSize = 11.sp, modifier = Modifier.padding(start = 10.dp))
                }
                Text(
                    if (profile.isRequest) "Use these contact details only to discuss this opportunity."
                    else "Please use this number only for discussion about this opportunity.",
                    color = Color(0xFF9FB3C9), fontSize = 11.sp,
                    modifier = Modifier.padding(start = 34.dp, top = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun ContactDetailRow(iconRes: Int, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 61.dp).border(width = 0.dp, color = Color.Transparent)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF09251F)), contentAlignment = Alignment.Center) {
            Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(28.dp))
        }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(label, color = Color(0xFF9FB3C9), fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
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
