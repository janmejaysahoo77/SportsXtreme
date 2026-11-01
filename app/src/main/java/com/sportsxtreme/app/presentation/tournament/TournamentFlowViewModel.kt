package com.sportsxtreme.app.presentation.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.Tournament
import com.sportsxtreme.app.domain.repository.TournamentRepository
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

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    fun load(tournamentId: String) = viewModelScope.launch {
        if (tournamentId.isBlank()) {
            _isLoading.value = false
            _loadError.value = "Tournament ID is missing"
            return@launch
        }
        _isLoading.value = true
        _loadError.value = null
        val result = repository.getTournament(tournamentId)
        _isLoading.value = false
        when (result) {
            is Resource.Success -> {
                _tournament.value = result.data
            }
            is Resource.Error -> {
                _loadError.value = result.message ?: "Unable to load tournament"
            }
            is Resource.Loading -> {
                // already loading
            }
        }
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

    fun saveTournamentImage(logoUrl: String? = null, bannerUrl: String? = null, onResult: (Resource<Unit>) -> Unit) = viewModelScope.launch {
        val current = _tournament.value ?: return@launch onResult(Resource.Error("Tournament is still loading"))
        val result = repository.updateTournamentImages(current.id, logoUrl, bannerUrl)
        if (result is Resource.Success) {
            _tournament.value = current.copy(
                logoUrl = logoUrl ?: current.logoUrl,
                bannerUrl = bannerUrl ?: current.bannerUrl
            )
        }
        onResult(result)
    }

    fun deleteTournament(onResult: (Resource<Unit>) -> Unit) = viewModelScope.launch {
        val current = _tournament.value ?: return@launch onResult(Resource.Error("Tournament is still loading"))
        onResult(repository.deleteTournament(current.id))
    }
}
