package com.lightspeed.app.domain

import android.content.ContentValues
import android.database.Cursor
import java.util.*

fun <T> collectCursor(c: Cursor, f: (Cursor) -> T): List<T> {
    val l = ArrayList<T>()
    while (!c.isAfterLast) {
        l.add(f(c))
        c.moveToNext()
    }
    return l
}

data class Contact(val localId: Int, val name: String, val color: String, val userId: UUID) {
    companion object {
        fun getContacts(): List<Contact> =
            collectCursor(
                Data.db().query("contacts", arrayOf("name", "color", "userId", "rowId"))
            ) {
                Contact(
                    it.getInt(4),
                    it.getString(0),
                    it.getString(1),
                    UUID.fromString(it.getString(2))
                )
            }

        fun addContact(contact: Contact) {
            val values = with(ContentValues()) {
                put("name", contact.name)
                put("color", contact.color)
                put("userId", contact.userId.toString())
                this
            }
            Data.db().insert("contacts", values)
        }

        fun deleteContact(contact: Contact) {
            Data.db().delete(
                "contacts",
                "rowId = ?",
                arrayOf(contact.localId.toString())
            )
        }
    }
}

data class Message(val id: Int, val contents: String, val mimeType: String, val timestamp: Long) {
    constructor(contents: String) : this(-1, contents, "text/plain", System.currentTimeMillis())

    companion object {
        fun getConversation(otherId: UUID): List<Message> =
            collectCursor(
                Data.db().raw().rawQuery(
                    "SELECT rowId, contents, mimeType, timestamp FROM inbox where senderId = ? " +
                            "UNION SELECT rowId, contents, mimeType, timestamp FROM outbox where receiverId = ? ORDER BY timestamp",
                    arrayOf(otherId.toString(), otherId.toString())
                )
            ) {
                Message(it.getInt(0), it.getString(1), it.getString(2), it.getLong(4))
            }

        fun sendMessage(msg: Message, receiverId: UUID) {
            Data.db().insert("outbox", with(ContentValues()) {
                put("contents", msg.contents)
                put("mimeType", msg.mimeType)
                put("timestamp", msg.timestamp)
                put("receiverId", receiverId.toString())
                this
            })
        }
    }
}