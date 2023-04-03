package com.lightspeed.unisync.client

import android.database.Cursor


data class Row(val id: Int, val dataHash: Long, val data: List<String>) {
    constructor(dbRow: Cursor) : this(
        dbRow.getInt(0),
        dbRow.getLong(1),
        List(dbRow.columnCount - 3) { dbRow.getString(it + 3) })
}

data class InvalidRow(val reason:String, val columnIndex:String, val id: Int, val dataHash: Long, val data: List<String>)

data class SyncResponse(val deletedRows : Set<Int>, val newOrModifiedRows: Set<Row>, val invalidRows: Set<InvalidRow>)

data class Table(val columns: List<Pair<String, String>>)
data class Schema(val version : Int, val tables : Map<String, Table>)