package com.crowans.smartsearch.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchOverlay(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    SearchBar(
        modifier = modifier,
        query = searchQuery,
        onQueryChange = { searchQuery = it },
        onSearch = { /* Handle search */ },
        active = true,
        onActiveChange = { },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.onSurface
            )
        },
        placeholder = {
            Text("Search anything...")
        },
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        // Search suggestions can be added here
    }
}