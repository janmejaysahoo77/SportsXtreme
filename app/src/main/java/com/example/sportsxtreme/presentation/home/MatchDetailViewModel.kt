package com.example.sportsxtreme.presentation.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.LiveMatch
import com.example.sportsxtreme.domain.repository.LiveMatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchDetailUiState(
    val isLoading: Boolean = true,
    val match: LiveMatch? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class MatchDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val liveMatchRepository: LiveMatchRepository
) : ViewModel() {

    private val matchId: String = savedStateHandle.get<String>(MatchDetailActivity.EXTRA_MATCH_ID).orEmpty()

    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    init {
        if (matchId.isNotBlank()) observeMatch()
    }

    private fun observeMatch() {
        viewModelScope.launch {
            liveMatchRepository.observeLiveMatch(matchId).collect { resource ->
                when (resource) {
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, match = resource.data, errorMessage = null)
                    }
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = resource.message)
                    }
                }
            }
        }
    }
}