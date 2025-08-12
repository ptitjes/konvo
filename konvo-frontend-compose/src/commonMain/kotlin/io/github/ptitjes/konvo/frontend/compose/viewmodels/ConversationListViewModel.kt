package io.github.ptitjes.konvo.frontend.compose.viewmodels

import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * ViewModel responsible for listing conversations and handling selection/deletion.
 *
 * Manual DI: [repository] is provided by the application entry point.
 */
class ConversationListViewModel(
    private val repository: ConversationRepository,
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _selectedConversation = MutableStateFlow<Conversation?>(null)
    val selectedConversation: StateFlow<Conversation?> = _selectedConversation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Subscribe to repository conversations stream
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository
                .getConversations(sort = Sort.UpdatedDesc)
                .catch { e -> _error.value = e.message ?: "Failed to load conversations" }
                .onEach { list ->
                    _conversations.value = list
                    // Keep selection only if it still exists
                    _selectedConversation.value = _selectedConversation.value?.takeIf { conversation ->
                        list.any { it.id == conversation.id }
                    }
                    _isLoading.value = false
                }
                .collect()
        }
    }

    fun select(conversation: Conversation?) {
        _selectedConversation.value = conversation
    }

    fun delete(conversation: Conversation) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.deleteConversation(conversation.id)
                if (_selectedConversation.value?.id == conversation.id) {
                    _selectedConversation.value = null
                }
            } catch (e: Throwable) {
                _error.value = e.message ?: "Failed to delete conversation"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
