package com.crowans.smartsearch.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import androidx.collection.LruCache
import com.crowans.smartsearch.data.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ContactsRepository(private val context: Context) {
    // Cache for storing contacts
    private val contactsCache = LruCache<String, List<Contact>>(100)
    private val mutex = Mutex()
    private var cachedContacts: List<Contact>? = null

    init {
        // Preload contacts in background
        preloadContacts()
    }

    private fun preloadContacts() {
        Thread {
            loadAllContacts()
        }.apply {
            priority = Thread.MIN_PRIORITY
            start()
        }
    }

    private fun loadAllContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.TYPE
        )

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )?.use { cursor ->
            val contactMap = mutableMapOf<String, Contact>()

            while (cursor.moveToNext()) {
                val contact = cursor.toContact()
                // Keep only the first number for each contact to avoid duplicates
                if (!contactMap.containsKey(contact.id)) {
                    contactMap[contact.id] = contact
                }
            }
            contacts.addAll(contactMap.values)
        }

        return contacts
    }

    fun searchContacts(query: String): Flow<List<Contact>> = flow {
        if (query.length < 2) {
            emit(emptyList())
            return@flow
        }

        val normalizedQuery = query.trim().lowercase()
        val results = mutex.withLock {
            // Try to get from cache first
            contactsCache.get(normalizedQuery) ?: run {
                // If not in cache, search in memory
                val contacts = cachedContacts ?: loadAllContacts().also {
                    cachedContacts = it
                }

                contacts.filter { contact ->
                    when {
                        // Exact matches get highest priority
                        contact.name.lowercase().startsWith(normalizedQuery) -> true
                        // Then check if any word starts with query
                        contact.name.split(" ").any {
                            it.lowercase().startsWith(normalizedQuery)
                        } -> true
                        // Finally check if phone number contains query
                        normalizedQuery.all { it.isDigit() } &&
                                contact.phoneNumber.contains(normalizedQuery) -> true
                        else -> false
                    }
                }.sortedBy { it.name }
                    .take(15) // Limit results for better performance
                    .also {
                        // Cache the results
                        contactsCache.put(normalizedQuery, it)
                    }
            }
        }

        emit(results)
    }.flowOn(Dispatchers.IO)

    private fun Cursor.toContact(): Contact {
        val idIndex = getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
        val nameIndex = getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val photoIndex = getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

        return Contact(
            id = getString(idIndex) ?: "",
            name = getString(nameIndex) ?: "",
            phoneNumber = getString(numberIndex)?.normalizePhoneNumber() ?: "",
            photoThumbnailUri = getString(photoIndex)
        )
    }

    private fun String.normalizePhoneNumber(): String =
        replace(Regex("[^0-9+]"), "")
}