package com.lightspeed.unisync.client

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SyncDbHelper(context: Context, private val schema: Schema) :
    SQLiteOpenHelper(context, "syncdb", null, schema.version) {
    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            schema.tables.forEach {
                val tableName = it.key
                val table = it.value
                db.execSQL(
                    "CREATE TABLE $tableName (" +
                            "rowId INTEGER PRIMARY KEY," +
                            "dataHash INTEGER NOT NULL," +
                            "_modified INTEGER NOT NULL," +
                            "${table.columns.joinToString { "${it.first} ${it.second}" }})"
                )
                db.execSQL(
                    "CREATE TABLE status$tableName (" +
                            "rowId INTEGER PRIMARY KEY," +
                            "dataHash INTEGER NOT NULL)"
                )
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}