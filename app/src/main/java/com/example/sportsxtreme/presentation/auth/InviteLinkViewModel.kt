package com.example.sportsxtreme.presentation.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds an invite token received from an Android App Link until a later phase
 * consumes it. No invite validation or claiming occurs here.
 */
@HiltViewModel
class InviteLinkViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _pendingInviteToken = MutableStateFlow<String?>(
        savedStateHandle[PENDING_INVITE_TOKEN]
    )
    val pendingInviteToken: StateFlow<String?> = _pendingInviteToken.asStateFlow()

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

    private companion object {
        const val PENDING_INVITE_TOKEN = "pending_invite_token"
    }
}
