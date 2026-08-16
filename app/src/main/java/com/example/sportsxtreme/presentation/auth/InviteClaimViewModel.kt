package com.example.sportsxtreme.presentation.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.repository.MatchInviteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class InviteClaimViewModel @Inject constructor(
    private val repository: MatchInviteRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    fun claim(token: String, onFinished: (Resource<com.example.sportsxtreme.domain.model.ClaimedMatchInvite>) -> Unit) {
        if (savedStateHandle.get<String>("claiming_token") == token) return
        savedStateHandle["claiming_token"] = token
        viewModelScope.launch {
            val result = repository.claimInvite(token)
            savedStateHandle["claiming_token"] = null
            onFinished(result)
        }
    }
}
