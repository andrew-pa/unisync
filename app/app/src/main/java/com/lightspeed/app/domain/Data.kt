package com.lightspeed.app.domain

import android.content.Context
import com.lightspeed.unisync.client.Schema
import com.lightspeed.unisync.client.SyncClient
import com.lightspeed.unisync.client.Table

object Data {
    private var db: SyncClient? = null

    fun init(context: Context) {
        if (db != null) return

        db = SyncClient(
            context, "test-user-1", "https://sync",
            Schema(
                1, mapOf(
                    Pair(
                        "contacts", Table(
                            listOf(
                                Pair("name", "TEXT"),
                                Pair("color", "TEXT"),
                                Pair("userId", "TEXT")
                            )
                        )
                    ),
                    Pair(
                        "inbox", Table(
                            listOf(
                                Pair("senderId", "TEXT"),
                                Pair("contents", "TEXT"),
                                Pair("mimeType", "TEXT"),
                                Pair("timestamp", "INT")
                            )
                        )
                    ),
                    Pair(
                        "outbox", Table(
                            listOf(
                                Pair("receiverId", "TEXT"),
                                Pair("contents", "TEXT"),
                                Pair("mimeType", "TEXT"),
                                Pair("timestamp", "INT")
                            )
                        )
                    )
                )
            )
        )
    }

    fun db(): SyncClient = db ?: throw RuntimeException("must initialize db before access")
}