package com.lightspeed.unisync.client

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.sqlite.transaction
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

data class Row(val id: Int, val dataHash: Long, val data: List<String>) {
    constructor(dbRow: Cursor) : this(
        dbRow.getInt(0),
        dbRow.getLong(1),
        List(dbRow.columnCount - 3) { dbRow.getString(it + 3) })
}

data class InvalidRow(val reason:String, val columnIndex:String, val id: Int, val dataHash: Long, val data: List<String>)

data class SyncResponse(val deletedRows : Set<Int>, val newOrModifiedRows: Set<Row>, val invalidRows: Set<InvalidRow>)

class SyncClient(context: Context, schemaVersion: Int) {
    val sessionId = UUID.randomUUID()
    val db = SyncDbHelper(context, schemaVersion)
    val tableNames = HashSet<String>()

    init {
        // log in
        // read all table names
        db.readableDatabase.query(
            "sqlite_master",
            arrayOf("name"),
            "type='table' AND NOT name LIKE 'status%'",
            null,
            null,
            null,
            null
        ).use { tables ->
            while (!tables.isAfterLast) {
                tableNames.add(tables.getString(0))
                tables.moveToNext()
            }
        }
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

    fun readRowsAndHash(tableName: String): HashMap<Int, Long> {
        val rows = HashMap<Int, Long>()
        db.readableDatabase.query(
            tableName,
            arrayOf("rowId", "dataHash"),
            null,
            null,
            null,
            null,
            null
        ).use {
            while (!it.isAfterLast) {
                rows[it.getInt(0)] = it.getLong(1)
                it.moveToNext()
            }
        }
        return rows
    }

    private fun syncTables() {
        runBlocking {
            tableNames.map { tableName ->
                async {
                    val newRows = HashSet<Row>()
                    // select new or modified rows
                    db.readableDatabase.query(
                        tableName,
                        arrayOf("*"),
                        "_modified OR rowId NOT IN (SELECT rowId FROM status$tableName)",
                        null,
                        null,
                        null,
                        null
                    ).use {
                        while (!it.isAfterLast) {
                            newRows.add(Row(it))
                            it.moveToNext()
                        }
                    }
                    val result = callServer(
                        tableName,
                        readRowsAndHash(tableName),
                        readRowsAndHash("status$tableName"),
                        newRows
                    )
                    db.writableDatabase.delete(tableName, "rowId IN (${result.deletedRows.joinToString { it.toString() }})", arrayOf())
                    for (row in result.newOrModifiedRows) {
                        val values = ContentValues()
                        db.writableDatabase.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
                    }
                }
            }.forEach { it.join() }
        }
    }

    private suspend fun callServer(
        tableName: String,
        currentRows: Map<Int, Long>,
        previousRows: Map<Int, Long>,
        newRows: Set<Row>
    ): SyncResponse {
        TODO("Not yet implemented")
    }

    fun insert(table: String, values: ContentValues): Long {
        // TODO: bother to notify the sync system that this table has changed?
        // TODO: compute new hash value
        return db.writableDatabase.insert(table, null, values)
    }

    fun update(table: String, values: ContentValues, where: String, args: Array<String>): Int {
        // TODO: bother to notify the sync system that this table has changed?
        // TODO: compute new hash value
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