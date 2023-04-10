package com.lightspeed.app

import android.content.Context
import com.lightspeed.unisync.client.Schema
import com.lightspeed.unisync.client.SyncClient
import com.lightspeed.unisync.client.Table

object Data {
    private var db: SyncClient? = null

    fun init(context: Context) {
        if (db != null) return

        db = SyncClient(
            context, "test-user-1", "http://10.0.2.2:3001/sync",
            Schema(
                1, mapOf(
                    Pair(
                        "contacts", Table(
                            listOf(
                                Pair("name", "TEXT"),
                                Pair("phoneNumber", "TEXT"),
                                Pair("eyeColor", "TEXT")
                            )
                        )
                    )
                )
            )
        )
    }

    fun db(): SyncClient = db ?: throw RuntimeException("must initialize db before access")
}