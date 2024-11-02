package com.crowans.smartsearch.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.crowans.smartsearch.data.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ContactsRepository(private val context: Context) {
    fun searchContacts(query: String): Flow<List<Contact>> = flow {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val contacts = queryContacts(normalizedQuery)
        emit(contacts)
    }.flowOn(Dispatchers.IO)

    private suspend fun queryContacts(normalizedQuery: String): List<Contact> {
        val contactsMap = mutableMapOf<String, ContactWithScore>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.TYPE
        )

        val selection = """
            LOWER(${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME}) LIKE ?
        """.trimIndent()

        // Use a single broad selection and filter in memory
        val selectionArgs = arrayOf("%$normalizedQuery%")

        var cursor: Cursor? = null

        try {
            cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null // No SQL sorting - we'll sort in memory
            )

            cursor?.let {
                val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoThumbnailUriIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
                val typeIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)

                while (it.moveToNext()) {
                    val contactId = it.getString(contactIdIndex)
                    val name = it.getString(nameIndex) ?: continue
                    val number = it.getString(numberIndex)?.normalizePhoneNumber() ?: continue
                    val photoThumbnail = it.getString(photoThumbnailUriIndex)
                    val phoneType = it.getInt(typeIndex)

                    val matchScore = calculateMatchScore(name, normalizedQuery)
                    if (matchScore > 0) {
                        val contact = Contact(contactId, name, number, photoThumbnail)
                        val existing = contactsMap[contactId]

                        if (existing == null ||
                            (isPreferredPhoneType(phoneType) && !isPreferredPhoneType(existing.type)) ||
                            matchScore > existing.score) {
                            contactsMap[contactId] = ContactWithScore(contact, matchScore, phoneType)
                        }
                    }
                }
            }
        } finally {
            cursor?.close()
        }

        return contactsMap.values
            .sortedWith(
                compareByDescending<ContactWithScore> { it.score }
                    .thenBy { it.contact.name.lowercase() }
            )
            .map { it.contact }
    }

    private data class ContactWithScore(
        val contact: Contact,
        val score: Int,
        val type: Int
    )

    private fun calculateMatchScore(name: String, query: String): Int {
        val normalizedName = name.lowercase()
        val words = normalizedName.split(Regex("\\s+"))

        // Exact prefix matches for "Tom"
        if (words.any { it.lowercase().startsWith(query) }) {
            val firstWordMatch = words.first().lowercase().startsWith(query)
            return if (firstWordMatch) 1000 else 800
        }

        // Partial matches within words (lower priority)
        if (query.length >= 2 && words.any {
                it.length > query.length &&
                        it.lowercase().contains(query)
            }) {
            return 100
        }

        return 0
    }

    private fun String.normalizePhoneNumber(): String {
        return replace(Regex("[^0-9+]"), "")
    }

    private fun isPreferredPhoneType(type: Int): Boolean {
        return when (type) {
            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
            ContactsContract.CommonDataKinds.Phone.TYPE_MAIN,
            ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE -> true
            else -> false
        }
    }
}