package com.example.sportsxtreme.presentation.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds App Link tokens until the user is authenticated. Validation and all
 * Firestore writes are performed by callable Cloud Functions, never here.
 */
@HiltViewModel
class InviteLinkViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _pendingInviteToken = MutableStateFlow<String?>(
        savedStateHandle[PENDING_INVITE_TOKEN]
    )
    val pendingInviteToken: StateFlow<String?> = _pendingInviteToken.asStateFlow()

    private val _pendingTeamInviteToken = MutableStateFlow<String?>(
        savedStateHandle[PENDING_TEAM_INVITE_TOKEN]
    )
    val pendingTeamInviteToken: StateFlow<String?> = _pendingTeamInviteToken.asStateFlow()

    fun receiveInviteToken(token: String) {
        savedStateHandle[PENDING_INVITE_TOKEN] = token
        _pendingInviteToken.value = token
    }

    fun consumeInviteToken(token: String) {
        if (_pendingInviteToken.value == token) {
            savedStateHandle[PENDING_INVITE_TOKEN] = null
            _pendingInviteToken.value = null
        }
    }

    fun receiveTeamInviteToken(token: String) {
        savedStateHandle[PENDING_TEAM_INVITE_TOKEN] = token
        _pendingTeamInviteToken.value = token
    }

    fun consumeTeamInviteToken(token: String) {
        if (_pendingTeamInviteToken.value == token) {
            savedStateHandle[PENDING_TEAM_INVITE_TOKEN] = null
            _pendingTeamInviteToken.value = null
        }
    }

    private companion object {
        const val PENDING_INVITE_TOKEN = "pending_invite_token"
        const val PENDING_TEAM_INVITE_TOKEN = "pending_team_invite_token"
    }
}
