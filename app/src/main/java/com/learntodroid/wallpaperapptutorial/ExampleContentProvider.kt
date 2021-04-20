package com.learntodroid.wallpaperapptutorial

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import java.sql.DriverManager.println

class ExampleContentProvider : ContentProvider() {
    companion object {
        const val AUTHORITY = "com.learntodroid.wallpaperapptutorial.ExampleContentProvider"
    }

    private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    override fun onCreate(): Boolean {
        println("KELVIN created provider")
        sUriMatcher.addURI(AUTHORITY, "table3", 1)
        sUriMatcher.addURI(AUTHORITY, "table3/#", 2)
        return true;
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        println("Kelvin queried with $uri")
        val cursor = MatrixCursor(arrayOf("name", "value"))
        cursor.addRow(arrayOf("a", "b"))
        return cursor
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

}