package com.example.sportsxtreme.presentation.tournament

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

/** Lets an authenticated captain register one managed team through a tournament invite link. */
class TournamentTeamInviteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = intent.getStringExtra(EXTRA_TOKEN).orEmpty()
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (token.isBlank() || uid.isBlank()) { finish(); return }
        setContent { TournamentInviteScreen(token, uid, onDone = { finish() }) }
    }
    companion object { const val EXTRA_TOKEN = "tournament_invite_token" }
}

@Composable
private fun TournamentInviteScreen(token: String, uid: String, onDone: () -> Unit) {
    val context = LocalContext.current
    var teams by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var submitting by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(uid) {
        FirebaseFirestore.getInstance().collection("teams").whereArrayContains("memberIds", uid).get()
            .addOnSuccessListener { snapshots ->
                teams = snapshots.documents.mapNotNull { doc ->
                    val members = doc.get("members") as? List<*> ?: emptyList<Any>()
                    val mine = members.filterIsInstance<Map<*, *>>().firstOrNull { it["userId"] == uid }
                    val roles = mine?.get("roles") as? List<*> ?: emptyList<Any>()
                    if (roles.any { it == "CAPTAIN" || it == "ADMIN" }) doc.id to (doc.getString("teamName") ?: doc.getString("name") ?: "Team") else null
                }
                loading = false
            }.addOnFailureListener { loading = false }
    }
    Column(Modifier.fillMaxSize().background(Color(0xFF07111E)).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("JOIN TOURNAMENT", color = Color(0xFFC6FF00), fontSize = 13.sp, fontWeight = FontWeight.Black)
        Text("Register your team", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Text("Choose a team you captain. The organiser will see it immediately in the tournament.", color = Color(0xFF9CAAB8), fontSize = 14.sp)
        when {
            loading -> Text("Loading your teams…", color = Color(0xFF9CAAB8))
            teams.isEmpty() -> Text("You need to be a captain or admin of a team before you can register it.", color = Color(0xFF9CAAB8))
            else -> teams.forEach { (id, name) ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF102238)).clickable(enabled = !submitting) {
                    submitting = true
                    FirebaseFunctions.getInstance().getHttpsCallable("joinTournamentInvite").call(mapOf("token" to token, "teamId" to id))
                        .addOnSuccessListener { Toast.makeText(context, "$name registered successfully", Toast.LENGTH_LONG).show(); onDone() }
                        .addOnFailureListener { error -> submitting = false; Toast.makeText(context, error.message ?: "Unable to register team", Toast.LENGTH_LONG).show() }
                }.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                    Text(if (submitting) "JOINING…" else "REGISTER", color = Color(0xFFC6FF00), fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
