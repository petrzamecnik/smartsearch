package com.crowans.smartsearch.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crowans.smartsearch.ui.components.SearchOverlay
import com.crowans.smartsearch.ui.theme.SmartSearchTheme

@Composable
fun SearchScreen() {
    SmartSearchTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            SearchOverlay(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                onSettingsClick = {
                    // implement settings screen and navige to it
                }
            )
        }
    }
}



