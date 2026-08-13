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
}
