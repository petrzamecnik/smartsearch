package com.crowans.smartsearch.data.model

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoThumbnailUri: String? = null
)