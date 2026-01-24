package com.example.myapplication.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.database.UserProfile
import com.example.myapplication.repository.UserProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val userProfile: UserProfile? = null
)

class ProfileViewModel(
    userProfileRepository: UserProfileRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = userProfileRepository.userProfile
        .map { profile -> ProfileUiState(userProfile = profile) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileUiState()
        )

    companion object {
        fun provideFactory(
            userProfileRepository: UserProfileRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(userProfileRepository) as T
            }
        }
    }
}
