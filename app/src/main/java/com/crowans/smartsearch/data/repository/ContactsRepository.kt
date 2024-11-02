package com.crowans.smartsearch.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.crowans.smartsearch.data.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(private val context: Context) {
    suspend fun searchContacts(query: String): List<Contact> = withContext(Dispatchers.IO) {
        val contactsMap = mutableMapOf<String, Contact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET
        )

        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")
        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"

        var cursor: Cursor? = null

        try {
            cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.let {
                val contactIdIndex =
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex =
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoThumbnailUriIndex =
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
                val typeIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                val accountTypeIndex =
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET)

                while (it.moveToNext()) {
                    val contactId = it.getString(contactIdIndex)
                    val name = it.getString(nameIndex)
                    val number = it.getString(numberIndex)?.normalizePhoneNumber()
                    val photoThumbnail = it.getString(photoThumbnailUriIndex)
                    val phoneType = it.getInt(typeIndex)

                    // Only add if we haven't seen this contact before or if this is a preferred number
                    if (!contactsMap.containsKey(contactId) || isPreferredPhoneType(phoneType)) {
                        contactsMap[contactId] =
                            Contact(contactId, name, number.toString(), photoThumbnail)
                    }
                }
            }
        } finally {
            cursor?.close()
        }

        return@withContext contactsMap.values.toList()
    }

    private fun String.normalizePhoneNumber(): String {
        // Remove all non-digit characters except '+'
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