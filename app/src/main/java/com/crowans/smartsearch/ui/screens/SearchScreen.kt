package com.crowans.smartsearch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crowans.smartsearch.ui.components.SearchOverlay
import com.crowans.smartsearch.ui.components.SearchResults
import com.crowans.smartsearch.ui.theme.SmartSearchTheme
import com.crowans.smartsearch.ui.viewmodel.SearchViewModel


@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel()
) {
    val searchState by viewModel.searchState.collectAsState()

    SmartSearchTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column {
                SearchOverlay(
                    searchQuery = searchState.query,
                    onSearchQueryChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    onSettingsClick = {
                        // Implement settings navigation
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SearchResults(
                    searchState = searchState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}