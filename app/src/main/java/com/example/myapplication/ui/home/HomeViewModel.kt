package com.example.myapplication.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.database.UserProfile
import com.example.myapplication.helper.GemmaHelper
import com.example.myapplication.repository.MessageRepository
import com.example.myapplication.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val messages: List<Message> = emptyList(),
    val userProfile: UserProfile? = null,
    val newMessageText: String = "",
    val isGenerating: Boolean = false,
    val gemmaStatus: String = "Initializing Gemma..."
)

class HomeViewModel(
    private val messageRepository: MessageRepository,
    private val userProfileRepository: UserProfileRepository,
    context: Context
) : ViewModel() {

    private val gemmaHelper = GemmaHelper(context)

    private val _newMessageText = MutableStateFlow("")
    private val _isGenerating = MutableStateFlow(false)
    private val _gemmaStatus = MutableStateFlow("Initializing Gemma...")

    val uiState: StateFlow<HomeUiState> = combine(
        messageRepository.getAllMessages(),
        userProfileRepository.userProfile,
        _newMessageText,
        _isGenerating,
        _gemmaStatus
    ) { messages, userProfile, newMessageText, isGenerating, gemmaStatus ->
        val username = userProfile?.username ?: "You"
        val userImagePath = userProfile?.imagePath

        val uiMessages = messages.map {
            Message(
                author = it.author,
                body = it.body,
                imagePath = if (it.author == username || it.author == "You") userImagePath else null
            )
        }

        HomeUiState(
            messages = uiMessages,
            userProfile = userProfile,
            newMessageText = newMessageText,
            isGenerating = isGenerating,
            gemmaStatus = gemmaStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        initializeGemma()
    }

    private fun initializeGemma() {
        viewModelScope.launch {
            val success = gemmaHelper.initialize()
            _gemmaStatus.value = if (success) "Gemma ready" else (gemmaHelper.getError() ?: "Failed to initialize")
        }
    }

    fun updateNewMessageText(text: String) {
        _newMessageText.value = text
    }

    fun sendMessage() {
        val messageText = _newMessageText.value
        if (messageText.isBlank() || _isGenerating.value) return

        val username = uiState.value.userProfile?.username ?: "You"
        _newMessageText.value = ""

        viewModelScope.launch {
            messageRepository.insertMessage(username, messageText)

            if (gemmaHelper.isReady()) {
                _isGenerating.value = true
                val response = gemmaHelper.generateResponse(messageText)
                messageRepository.insertMessage("Gemma", response)
                _isGenerating.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gemmaHelper.close()
    }

    companion object {
        fun provideFactory(
            messageRepository: MessageRepository,
            userProfileRepository: UserProfileRepository,
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(messageRepository, userProfileRepository, context) as T
            }
        }
    }
}
