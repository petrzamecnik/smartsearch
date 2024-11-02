package com.crowans.smartsearch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crowans.smartsearch.R

@Composable
fun SearchOverlay(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val shape = RoundedCornerShape(50.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.3f))
            .height(60.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(R.string.search_icon_description),
            tint = Color.White,
            modifier = Modifier
                .size(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(Color.White),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            stringResource(R.string.search_placeholder),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(R.string.settings_icon_description),
            tint = Color.White,
            modifier = Modifier
                .size(28.dp)  // Slightly smaller than search icon
                .clickable { onSettingsClick() }
        )
    }
}