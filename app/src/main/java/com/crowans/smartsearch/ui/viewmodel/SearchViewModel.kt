package com.crowans.smartsearch.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.crowans.smartsearch.data.repository.AppsRepository
import com.crowans.smartsearch.data.repository.ContactsRepository
import com.crowans.smartsearch.ui.state.SearchState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val contactsRepository = ContactsRepository(application)
    private val appsRepository = AppsRepository(application)

    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(200) // Reduced debounce time
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .collect { query -> search(query) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchState.update { it.copy(query = query) }
        if (query.length < 2) {
            _searchState.update {
                it.copy(contacts = emptyList(), apps = emptyList(), isLoading = false)
            }
            searchJob?.cancel()
        } else {
            queryFlow.value = query
        }
    }

    private fun search(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _searchState.update { it.copy(isLoading = true, error = null) }

                // Launch parallel searches
                launch {
                    contactsRepository.searchContacts(query)
                        .collect { contacts ->
                            _searchState.update { it.copy(contacts = contacts) }
                        }
                }

                launch {
                    val apps = appsRepository.searchApps(query)
                    _searchState.update { it.copy(apps = apps) }
                }

                _searchState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _searchState.update {
                    it.copy(
                        error = "Search failed: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
}