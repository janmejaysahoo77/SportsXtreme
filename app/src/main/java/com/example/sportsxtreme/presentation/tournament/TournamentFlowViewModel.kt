package com.example.sportsxtreme.presentation.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.domain.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TournamentFlowViewModel @Inject constructor(private val repository: TournamentRepository) : ViewModel() {
    private val _tournament = MutableStateFlow<Tournament?>(null)
    val tournament: StateFlow<Tournament?> = _tournament.asStateFlow()

    fun load(tournamentId: String) = viewModelScope.launch {
        if (tournamentId.isBlank()) return@launch
        val result = repository.getTournament(tournamentId)
        if (result is Resource.Success) _tournament.value = result.data
    }

    fun saveTournamentDetails(name: String, startDate: String, ground: String, ballType: String) = viewModelScope.launch {
        val current = _tournament.value ?: return@launch
        val result = repository.updateTournamentDetails(current.id, name, startDate, ground, ballType)
        if (result is Resource.Success) {
            _tournament.value = current.copy(
                name = name,
                startDate = startDate,
                dateToBeAnnounced = startDate.isBlank(),
                ground = ground,
                ballType = ballType
            )
        }
    }

    fun saveTeamDetails(entryFee: String, numberOfTeams: String) = viewModelScope.launch {
        val current = _tournament.value ?: return@launch
        val requirements = current.requirements.copy(entryFee = entryFee, numberOfTeams = numberOfTeams)
        val result = repository.updateTournamentRequirements(current.id, requirements)
        if (result is Resource.Success) _tournament.value = current.copy(requirements = requirements)
    }
}
