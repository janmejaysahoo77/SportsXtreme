package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.domain.model.TournamentRequirements
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FirebaseFirestoreTournamentDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun createTournament(tournament: Tournament): Resource<Tournament> {
        return try {
            val docRef = firestore.collection("tournaments").document()
            val tournamentWithId = tournament.copy(
                id = docRef.id,
                createdAtEpochMs = System.currentTimeMillis()
            )
            docRef.set(tournamentWithId).await()
            Resource.Success(tournamentWithId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred while creating tournament")
        }
    }

    suspend fun updateTournamentRequirements(tournamentId: String, requirements: TournamentRequirements): Resource<Unit> = try {
        require(tournamentId.isNotBlank()) { "Tournament ID is missing" }
        firestore.collection("tournaments").document(tournamentId)
            .set(mapOf("requirements" to requirements), SetOptions.merge()).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unable to save tournament requirements")
    }

    suspend fun getTournament(tournamentId: String): Resource<Tournament> = try {
        val snapshot = firestore.collection("tournaments").document(tournamentId).get().await()
        val tournament = snapshot.toObject(Tournament::class.java)
            ?.copy(id = snapshot.id)
            ?: return Resource.Error("Tournament was not found")
        Resource.Success(tournament)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unable to load tournament")
    }

    fun observeHostTournaments(hostUid: String): Flow<Resource<List<Tournament>>> = callbackFlow {
        if (hostUid.isBlank()) {
            trySend(Resource.Success(emptyList()))
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("tournaments")
            .whereEqualTo("hostUid", hostUid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Unable to load your tournaments"))
                    return@addSnapshotListener
                }
                val tournaments = snapshots?.documents.orEmpty().mapNotNull { document ->
                    document.toObject(Tournament::class.java)?.copy(id = document.id)
                }.sortedByDescending { it.createdAtEpochMs }
                trySend(Resource.Success(tournaments))
            }
        awaitClose { registration.remove() }
    }
}
