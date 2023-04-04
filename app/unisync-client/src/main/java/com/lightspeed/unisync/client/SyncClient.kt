package com.lightspeed.unisync.client

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

class SyncClient(context: Context, private val syncUrl: String, private val schema: Schema) {
    private val sessionId = UUID.randomUUID()
    private val db = SyncDbHelper(context, schema)
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

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
            schema.tables.map { table ->
                val tableName = table.key
                launch {
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
                    ) ?: return@launch
                    if (result.invalidRows.isNotEmpty()) {
                        throw RuntimeException("rows returned as invalid: ${result.invalidRows}")
                    }
                    db.writableDatabase.delete(
                        tableName,
                        "rowId IN (${result.deletedRows.joinToString { it.toString() }})",
                        arrayOf()
                    )
                    for (row in result.newOrModifiedRows) {
                        val values =
                            table.value.columns.foldIndexed(ContentValues()) { index: Int, vals: ContentValues, col: Pair<String, String> ->
                                vals.put(col.first, row.data[index])
                                vals
                            }
                        values.put("rowId", row.id)
                        values.put("dataHash", row.dataHash)
                        values.put("_modified", false)
                        db.writableDatabase.insertWithOnConflict(
                            tableName,
                            null,
                            values,
                            SQLiteDatabase.CONFLICT_REPLACE
                        )
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
    ): SyncResponse? {
        val resp = httpClient.post(syncUrl) {
            contentType(ContentType.Application.Json)
            setBody(SyncRequest(tableName, sessionId, currentRows, previousRows, newRows))
        }
        if (resp.status.isSuccess()) {
            return resp.body()
        } else {
            Log.e(
                "SyncClient",
                "failed to make network request: " + resp.status + " " + resp.body()
            )
            return null
        }
    }

    private fun newRowId(): Long =
        kotlin.random.Random.nextLong(0x3fff_ffff_ffff_ffff)


    private fun computeDataHash(values: ContentValues): String =
        BigInteger(1, values.keySet().fold(MessageDigest.getInstance("MD5")) { h, s ->
            h.update(s.toByteArray())
            h
        }.digest()).toString(Character.MAX_RADIX)

    fun insert(table: String, values: ContentValues): Long {
        // TODO: bother to notify the sync system that this table has changed?
        values.put("rowId", newRowId())
        values.put("dataHash", computeDataHash(values))
        values.put("_modified", false)
        return db.writableDatabase.insert(table, null, values)
    }

    fun update(table: String, values: ContentValues, where: String, args: Array<String>): Int {
        // TODO: bother to notify the sync system that this table has changed?
        values.put("dataHash", computeDataHash(values))
        values.put("_modified", true)
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