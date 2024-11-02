package com.crowans.smartsearch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.crowans.smartsearch.ui.components.SearchOverlay
import com.crowans.smartsearch.ui.theme.SmartSearchTheme

@Composable
fun SearchScreen() {
    SmartSearchTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            SearchOverlay(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )
        }
    }
}