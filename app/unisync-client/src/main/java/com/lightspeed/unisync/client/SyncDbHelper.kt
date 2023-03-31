package com.lightspeed.unisync.client

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SyncDbHelper(context: Context, version: Int) :
    SQLiteOpenHelper(context, "syncdb", null, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        TODO("Not yet implemented")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}