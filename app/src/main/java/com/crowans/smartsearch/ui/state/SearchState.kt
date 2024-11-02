package com.crowans.smartsearch.ui.state

import com.crowans.smartsearch.data.model.Contact

data class SearchState(
    val query: String = "",
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)