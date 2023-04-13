package com.lightspeed.unisync.client

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.lang.Long.min
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.concurrent.thread
import kotlin.random.Random

class SyncClient(
    context: Context,
    private val userName: String,
    private val syncUrl: String,
    private val schema: Schema
) {
    private val db = SyncDbHelper(context, schema)
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    private val observers = HashMap<String, HashMap<Int, ()->Unit>>()
    private var nextToken = 7

    fun registerObserver(tableName: String, observer: ()->Unit): Int {
        val token = nextToken ++
        observers.getOrElse(tableName, {HashMap()}).put(token, observer)
        return token
    }

    fun unregisterObserver(tableName: String, observerToken: Int) {
        observers[tableName]?.remove(observerToken)
    }

    init {
        // log in
        // start sync worker
        thread {
            syncWorkerLoop()
        }
    }

    private fun syncWorkerLoop() {
        Log.i("SyncClient", "starting sync worker")
        var timeToWait = 50L
        while (true) {
            if (syncTables()) {
                timeToWait = 50L
            } else {
                timeToWait = min(timeToWait * 2, 5000L)
            }
            Thread.sleep(timeToWait)
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
            it.moveToFirst()
            while (!it.isAfterLast) {
                rows[it.getInt(0)] = it.getLong(1)
                it.moveToNext()
            }
        }
        return rows
    }

    private fun syncTables(): Boolean {
        Log.i("SyncClient", "synchronizing tables")
        return runBlocking {
            schema.tables.map { table ->
                val tableName = table.key
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
                        it.moveToFirst()
                        while (!it.isAfterLast) {
                            newRows.add(Row(it))
                            it.moveToNext()
                        }
                    }
                    val currentRows = readRowsAndHash(tableName)
                    val previousRows = readRowsAndHash("status$tableName")
                    val result = callServer(
                        tableName,
                        currentRows,
                        previousRows,
                        newRows
                    ) ?: return@async true
                    Log.i("SyncClient", "got sync result: $result")
                    if (result.invalidRows.isNotEmpty()) {
                        throw RuntimeException("rows returned as invalid: ${result.invalidRows}")
                    }
                    if (result.deletedRows.isNotEmpty()) {
                        db.writableDatabase.delete(
                            tableName,
                            "rowId IN (${result.deletedRows.joinToString { it.toString() }})",
                            arrayOf()
                        )
                    }
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
                    // update status table to reflect new current table
                    db.writableDatabase.delete("status$tableName", null, null)
                    db.writableDatabase.execSQL("INSERT INTO status$tableName SELECT rowId, dataHash FROM $tableName")
                    // sync again soon if anything changed on the server or the client
                    val changed = result.deletedRows.isNotEmpty() || result.newOrModifiedRows.isNotEmpty() || (currentRows.size - previousRows.size != 0)
                    if(changed && observers.containsKey(tableName)) {
                        val obs = observers[tableName]
                        if(obs!!.isNotEmpty()) {
                            Handler(Looper.getMainLooper()).post {
                                obs.forEach {
                                    it.value()
                                }
                            }
                        }
                    }
                    changed
                }
            }.fold(false) { acc, job -> acc || job.await() }
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
            setBody(SyncRequest(tableName, UUID.fromString("0a832d37-b296-4d54-b124-55835087e80f"), currentRows, previousRows, newRows))
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

    private fun newRowId(): Int =
        kotlin.random.Random.nextInt(0x3fff_ffff)


    private fun computeDataHash(table: String, values: ContentValues): Long {
        val sc = schema.tables[table]!!
        val rowVals = sc.columns.map {
            val columnName = it.first
            values.getAsString(columnName)
        }
        return rowVals.hashCode().toLong()
    }
    fun insert(table: String, values: ContentValues): Long {
        // TODO: bother to notify the sync system that this table has changed?
        values.put("rowId", newRowId())
        values.put("dataHash", computeDataHash(table, values))
        values.put("_modified", false)
        return db.writableDatabase.insert(table, null, values)
    }

    fun update(table: String, values: ContentValues, where: String, args: Array<String>): Int {
        // TODO: bother to notify the sync system that this table has changed?
        values.put("dataHash", computeDataHash(table, values))
        values.put("_modified", true)
        return db.writableDatabase.update(table, values, where, args)
    }

    fun delete(table: String, where: String, args: Array<String>) {
        db.writableDatabase.delete(table, where, args)
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
        val c = db.readableDatabase.query(
            table,
            columns,
            selection,
            selectionArgs,
            groupBy,
            orderBy,
            limit
        )
        c.moveToFirst()
        return c
    }

    fun raw() = db.readableDatabase
}