package com.example.myapplication.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileEditUiState(
    val username: String = "",
    val selectedImageUri: Uri? = null,
    val displayImagePath: String? = null,
    val isSaving: Boolean = false
)

class ProfileEditViewModel(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _username = MutableStateFlow("")
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    private val _displayImagePath = MutableStateFlow<String?>(null)
    private val _isSaving = MutableStateFlow(false)

    val uiState: StateFlow<ProfileEditUiState> = combine(
        _username,
        _selectedImageUri,
        _displayImagePath,
        _isSaving
    ) { username, selectedImageUri, displayImagePath, isSaving ->
        ProfileEditUiState(
            username = username,
            selectedImageUri = selectedImageUri,
            displayImagePath = displayImagePath,
            isSaving = isSaving
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileEditUiState()
    )

    init {
        loadCurrentProfile()
    }

    private fun loadCurrentProfile() {
        viewModelScope.launch {
            userProfileRepository.userProfile.collect { profile ->
                profile?.let {
                    if (_username.value.isEmpty()) {
                        _username.value = it.username
                    }
                    if (_displayImagePath.value == null && _selectedImageUri.value == null) {
                        _displayImagePath.value = it.imagePath
                    }
                }
            }
        }
    }

    fun updateUsername(username: String) {
        _username.value = username
    }

    fun selectImage(uri: Uri) {
        _selectedImageUri.value = uri
        _displayImagePath.value = null
    }

    fun saveProfile(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            userProfileRepository.updateProfile(_username.value, _selectedImageUri.value)
            _isSaving.value = false
            onComplete()
        }
    }

    companion object {
        fun provideFactory(
            userProfileRepository: UserProfileRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileEditViewModel(userProfileRepository) as T
            }
        }
    }
}
