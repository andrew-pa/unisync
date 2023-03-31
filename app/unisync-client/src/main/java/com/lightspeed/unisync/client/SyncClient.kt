package com.lightspeed.unisync.client

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class SyncClient(context: Context, schemaVersion: Int) {
    val sessionId = UUID.randomUUID()
    val db = SyncDbHelper(context, schemaVersion)

    init {
        // log in
        // start sync worker
        runBlocking {
            launch { syncWorkerLoop() }
        }
    }

    private suspend fun syncWorkerLoop() {
        var timeToWait = 50L
        while (true) {
            syncTables()
            timeToWait *= 2
            delay(timeToWait)
        }
    }

    private fun syncTables() {
        TODO("Not yet implemented")
    }

    fun insert(table: String, values: ContentValues): Long {
        // TODO: bother to notify the sync system that this table has changed?
        return db.writableDatabase.insert(table, null, values)
    }

    fun update(table: String, values: ContentValues, where: String, args: Array<String>): Int {
        return db.writableDatabase.update(table, values, where, args)
    }

    // TODO: transactions

    fun query(
        table: String,
        columns: Array<String>,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        groupBy: String? = null,
        orderBy: String? = null,
        limit: String? = null
    ): Cursor {
        return db.readableDatabase.query(
            table,
            columns,
            selection,
            selectionArgs,
            groupBy,
            orderBy,
            limit
        )
    }
}