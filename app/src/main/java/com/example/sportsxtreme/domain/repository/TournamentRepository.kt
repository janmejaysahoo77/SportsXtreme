package com.example.sportsxtreme.domain.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament

interface TournamentRepository {
    suspend fun createTournament(tournament: Tournament): Resource<Tournament>
}
