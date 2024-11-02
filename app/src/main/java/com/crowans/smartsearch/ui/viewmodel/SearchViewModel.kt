package com.crowans.smartsearch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crowans.smartsearch.data.repository.ContactsRepository
import com.crowans.smartsearch.ui.state.SearchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val contactsRepository = ContactsRepository(application)
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchState.update { it.copy(query = query) }
        if (query.isNotEmpty()) {
            searchContacts(query)
        } else {
            _searchState.update { it.copy(contacts = emptyList()) }
        }
    }

    private fun searchContacts(query: String) {
        viewModelScope.launch {
            _searchState.update { it.copy(isLoading = true, error = null) }
            try {
                val contacts = contactsRepository.searchContacts(query)
                _searchState.update { it.copy(contacts = contacts, isLoading = false) }
            } catch (e: Exception) {
                _searchState.update {
                    it.copy(
                        error = "Failed to search contacts: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
}