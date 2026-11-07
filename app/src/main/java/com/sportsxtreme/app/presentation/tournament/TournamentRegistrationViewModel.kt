package com.sportsxtreme.app.presentation.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.data.di.AuthDependencies
import com.sportsxtreme.app.domain.model.Tournament
import com.sportsxtreme.app.domain.usecase.CreateTournamentUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TournamentRegistrationViewModel @Inject constructor(
    private val createTournamentUseCase: CreateTournamentUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _tournamentState = MutableStateFlow(Tournament())
    val tournamentState: StateFlow<Tournament> = _tournamentState.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Set hostUid when initialized
        firebaseAuth.currentUser?.let { user ->
            _tournamentState.value = _tournamentState.value.copy(
                hostUid = user.uid,
                email = user.email ?: "",
                phone = user.phoneNumber ?: ""
            )
            viewModelScope.launch {
                val profile = AuthDependencies.authUseCases().getUserProfile(user.uid).data
                val name = profile?.name?.trim().orEmpty().ifBlank { user.displayName.orEmpty() }
                if (name.isNotBlank()) updateField(Field.ORGANIZER_NAME, name)
                val accountPhone = profile?.phoneNumber?.trim().orEmpty()
                val verifiedAuthPhone = user.phoneNumber?.takeIf { it.isNotBlank() }
                val phoneToPrefill = verifiedAuthPhone ?: accountPhone
                if (phoneToPrefill.isNotBlank() && _tournamentState.value.phone.isBlank()) {
                    updateField(Field.PHONE, phoneToPrefill)
                }
            }
        }
    }

    fun updateField(field: Field, value: String) {
        val current = _tournamentState.value
        val normalizedValue = if (field == Field.PHONE) value.filter { it.isDigit() || it == '+' }.take(13) else value
        _tournamentState.value = when (field) {
            Field.NAME -> current.copy(name = value)
            Field.LOGO_URL -> current.copy(logoUrl = value)
            Field.BANNER_URL -> current.copy(bannerUrl = value)
            Field.CITY -> current.copy(city = value)
            Field.GROUND -> current.copy(ground = value)
            Field.ORGANIZER_NAME -> current.copy(organizerName = value)
            Field.PHONE -> current.copy(phone = normalizedValue)
            Field.EMAIL -> current.copy(email = value)
            Field.START_DATE -> current.copy(startDate = value)
            Field.DATE_TO_BE_ANNOUNCED -> current.copy(dateToBeAnnounced = value.toBoolean())
        }
    }

    fun updateMode(type: String) {
        _tournamentState.value = _tournamentState.value.copy(type = type)
    }

    fun updateBallType(ballType: String) {
        _tournamentState.value = _tournamentState.value.copy(ballType = ballType)
    }

    fun updateMatchForm(matchForm: String) {
        _tournamentState.value = _tournamentState.value.copy(matchForm = matchForm)
    }

    fun updateLookingForTeams(looking: Boolean) {
        _tournamentState.value = _tournamentState.value.copy(lookingForTeams = looking)
    }

    fun saveTournament() {
        var current = _tournamentState.value
        if (current.hostUid.isBlank()) {
            val user = firebaseAuth.currentUser
            if (user != null) {
                current = current.copy(
                    hostUid = user.uid,
                    email = current.email.ifBlank { user.email ?: "" },
                    phone = current.phone.ifBlank { user.phoneNumber ?: "" }
                )
                _tournamentState.value = current
            } else {
                _uiState.value = UiState.Error("Please sign in to your account before creating a tournament.")
                return
            }
        }
        if (current.name.isBlank()) {
            _uiState.value = UiState.Error("Tournament Name is required")
            return
        }
        if (!isValidIndianPhone(current.phone)) {
            _uiState.value = UiState.Error("Enter a valid Indian mobile number (10 digits, starting with 6–9).")
            return
        }
        if (current.startDate.isBlank() && !current.dateToBeAnnounced) {
            _uiState.value = UiState.Error("Choose a start date or select To be announced")
            return
        }
        if (current.startDate.isNotBlank() && current.dateToBeAnnounced) {
            _uiState.value = UiState.Error("Choose either a start date or To be announced")
            return
        }
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                when (val result = createTournamentUseCase(current)) {
                    is Resource.Success -> {
                        _tournamentState.value = result.data ?: _tournamentState.value
                        _uiState.value = UiState.Success
                    }
                    is Resource.Error -> {
                        _uiState.value = UiState.Error(result.message ?: "Failed to create tournament")
                    }
                    is Resource.Loading -> {
                        _uiState.value = UiState.Loading
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    private fun isValidIndianPhone(value: String): Boolean {
        val digits = value.filter(Char::isDigit)
        val normalized = when {
            digits.length == 10 -> digits
            digits.length == 12 && digits.startsWith("91") -> digits.substring(2)
            else -> return false
        }
        return normalized.matches(Regex("[6-9]\\d{9}")) &&
            (value.count { it == '+' } <= 1) && (!value.contains('+') || value.trim().startsWith("+"))
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    enum class Field {
        NAME, LOGO_URL, BANNER_URL, CITY, GROUND, ORGANIZER_NAME, PHONE, EMAIL, START_DATE, DATE_TO_BE_ANNOUNCED
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
