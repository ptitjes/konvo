package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import androidx.lifecycle.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.storage.*
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

    private val _conversations = MutableStateFlow<List<ConversationDigest>>(emptyList())
    val conversations: StateFlow<List<ConversationDigest>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        println("Initializing ConversationListViewModel")
        // Subscribe to repository conversations stream
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository
                .getDigests(sort = Sort.UpdatedDesc)
                .catch { e -> _error.value = e.message ?: "Failed to load conversations" }
                .onEach { list ->
                    _conversations.value = list
                    _isLoading.value = false
                }
                .collect()
        }
    }

    fun delete(conversation: ConversationDigest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.delete(conversation.id)
            } catch (e: Throwable) {
                _error.value = e.message ?: "Failed to delete conversation"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
