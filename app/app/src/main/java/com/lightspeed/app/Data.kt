package com.lightspeed.app

import android.content.Context
import com.lightspeed.unisync.client.Schema
import com.lightspeed.unisync.client.SyncClient

object Data {
    private var db: SyncClient? = null

    fun init(context: Context) {
        db = SyncClient(context, "https://sync", Schema(1, mapOf()))
    }

    fun db(): SyncClient = db ?: throw RuntimeException("must initialize db before access")
}