package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseFirestoreTournamentDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun createTournament(tournament: Tournament): Resource<Tournament> {
        return try {
            val docRef = firestore.collection("tournaments").document()
            val tournamentWithId = tournament.copy(id = docRef.id)
            docRef.set(tournamentWithId).await()
            Resource.Success(tournamentWithId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred while creating tournament")
        }
    }
}
