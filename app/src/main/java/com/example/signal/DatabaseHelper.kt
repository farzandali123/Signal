package com.example.signal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.util.UUID

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, getCustomDatabasePath(context), null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "signal.db"
        private const val DATABASE_VERSION = 3 // Incremented to ensure migration for image_url

        // Define the custom database path
        fun getCustomDatabasePath(context: Context): String {
            val directory = File(context.getExternalFilesDir(null), "Database")
            if (!directory.exists()) {
                directory.mkdirs() // Ensure the folder exists
            }
            val databaseFile = File(directory, DATABASE_NAME)
            return databaseFile.absolutePath
        }

        // Table Names and Column Names
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"

        const val TABLE_PROFILE = "profile"
        const val COLUMN_PROFILE_ID = "id"
        const val COLUMN_FIRST_NAME = "first_name"
        const val COLUMN_LAST_NAME = "last_name"
        const val COLUMN_PROFILE_PICTURE_URI = "profile_picture_uri"
        const val COLUMN_USER_EMAIL = "email"

        const val TABLE_PIN = "pin"
        const val COLUMN_PIN_ID = "id"
        const val COLUMN_USER_PIN = "pin"

        const val TABLE_CHATS = "chats"
        const val COLUMN_CHAT_ID = "chat_id"
        const val COLUMN_OTHER_USER_NAME = "other_user_name"
        const val COLUMN_LAST_MESSAGE = "last_message"

        const val TABLE_MESSAGES = "messages"
        const val COLUMN_MESSAGE_ID = "message_id"
        const val COLUMN_MESSAGE_CHAT_ID = "chat_id"
        const val COLUMN_MESSAGE_SENDER_ID = "sender_id"
        const val COLUMN_MESSAGE_TEXT = "text"
        const val COLUMN_MESSAGE_IMAGE_URL = "image_url"
        const val COLUMN_MESSAGE_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        try {
            // Users Table
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                    $COLUMN_USER_ID TEXT PRIMARY KEY,
                    $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                    $COLUMN_PASSWORD TEXT NOT NULL
                )
            """)

            // Profile Table
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_PROFILE (
                    $COLUMN_PROFILE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_FIRST_NAME TEXT,
                    $COLUMN_LAST_NAME TEXT,
                    $COLUMN_PROFILE_PICTURE_URI TEXT,
                    $COLUMN_USER_EMAIL TEXT,
                    FOREIGN KEY($COLUMN_USER_EMAIL) REFERENCES $TABLE_USERS($COLUMN_EMAIL)
                )
            """)

            // PIN Table
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_PIN (
                    $COLUMN_PIN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USER_PIN TEXT NOT NULL
                )
            """)

            // Chats Table
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_CHATS (
                    $COLUMN_CHAT_ID TEXT PRIMARY KEY,
                    $COLUMN_OTHER_USER_NAME TEXT NOT NULL,
                    $COLUMN_LAST_MESSAGE TEXT
                )
            """)

            // Messages Table
            db?.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_MESSAGES (
                    $COLUMN_MESSAGE_ID TEXT PRIMARY KEY,
                    $COLUMN_MESSAGE_CHAT_ID TEXT NOT NULL,
                    $COLUMN_MESSAGE_SENDER_ID TEXT NOT NULL,
                    $COLUMN_MESSAGE_TEXT TEXT,
                    $COLUMN_MESSAGE_IMAGE_URL TEXT,
                    $COLUMN_MESSAGE_TIMESTAMP INTEGER NOT NULL,
                    FOREIGN KEY($COLUMN_MESSAGE_CHAT_ID) REFERENCES $TABLE_CHATS($COLUMN_CHAT_ID)
                )
            """)

            Log.d("DatabaseHelper", "Database tables created successfully")
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error creating tables: ${e.message}")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
            if (oldVersion < 3) {
                db?.execSQL("ALTER TABLE $TABLE_MESSAGES ADD COLUMN $COLUMN_MESSAGE_IMAGE_URL TEXT")
                Log.d("DatabaseHelper", "Database upgraded: Added $COLUMN_MESSAGE_IMAGE_URL column to $TABLE_MESSAGES table")
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error upgrading database: ${e.message}")
        }
    }

    // --- USERS TABLE METHODS ---
    fun registerUser(email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
        }
        return try {
            val result = db.insert(TABLE_USERS, null, values)
            Log.d("DatabaseHelper", "User registered: $email")
            result != -1L
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error registering user: ${e.message}")
            false
        }
    }

    fun authenticateUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(email, password)
        )
        val authenticated = cursor.count > 0
        cursor.close()
        Log.d("DatabaseHelper", "User authenticated: $email, result=$authenticated")
        return authenticated
    }

    // --- PROFILE TABLE METHODS ---
    fun saveProfile(firstName: String, lastName: String?, profilePictureUri: String?, email: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FIRST_NAME, firstName)
            put(COLUMN_LAST_NAME, lastName)
            put(COLUMN_PROFILE_PICTURE_URI, profilePictureUri)
            put(COLUMN_USER_EMAIL, email)
        }
        val result = db.replace(TABLE_PROFILE, null, values)
        db.close()
        Log.d("DatabaseHelper", "Profile saved for email: $email")
        return result
    }

    fun isProfileExists(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PROFILE,
            arrayOf(COLUMN_PROFILE_ID),
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    // --- CHATS TABLE METHODS ---
    fun saveChatLocally(chatId: String, otherUserName: String, lastMessage: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CHAT_ID, chatId)
            put(COLUMN_OTHER_USER_NAME, otherUserName)
            put(COLUMN_LAST_MESSAGE, lastMessage)
        }
        val result = db.replace(TABLE_CHATS, null, values)
        db.close()
        return result != -1L
    }

    fun getAllChats(): List<Chat> {
        val db = readableDatabase
        val chats = mutableListOf<Chat>()
        val cursor = db.query(TABLE_CHATS, null, null, null, null, null, "$COLUMN_CHAT_ID ASC")
        if (cursor.moveToFirst()) {
            do {
                val chatId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHAT_ID))
                val otherUserName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OTHER_USER_NAME))
                val lastMessage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_MESSAGE))
                chats.add(Chat(chatId, otherUserName, lastMessage))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return chats
    }

    // --- MESSAGES TABLE METHODS ---
    fun saveMessageLocally(chatId: String, messageText: String?, senderId: String, imageUrl: String?, timestamp: Long): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MESSAGE_ID, UUID.randomUUID().toString())
            put(COLUMN_MESSAGE_CHAT_ID, chatId)
            put(COLUMN_MESSAGE_SENDER_ID, senderId)
            put(COLUMN_MESSAGE_TEXT, messageText)
            put(COLUMN_MESSAGE_IMAGE_URL, imageUrl)
            put(COLUMN_MESSAGE_TIMESTAMP, timestamp)
        }
        val result = db.insert(TABLE_MESSAGES, null, values)
        db.close()
        return result != -1L
    }

    fun getMessagesForChat(chatId: String): List<Message> {
        val db = readableDatabase
        val messages = mutableListOf<Message>()
        val cursor = db.query(
            TABLE_MESSAGES,
            null,
            "$COLUMN_MESSAGE_CHAT_ID = ?",
            arrayOf(chatId),
            null, null, "$COLUMN_MESSAGE_TIMESTAMP ASC"
        )
        if (cursor.moveToFirst()) {
            do {
                val messageId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_ID))
                val senderId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_SENDER_ID))
                val text = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_TEXT)) ?: ""
                val imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_IMAGE_URL)) ?: ""
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE_TIMESTAMP))
                messages.add(Message(messageId, chatId, senderId, text, imageUrl, timestamp))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return messages
    }
}

// --- DATA CLASSES ---
data class Chat(
    val chatId: String,
    val otherUserName: String,
    val lastMessage: String
)

data class Message(
    val messageId: String,
    val chatId: String,
    val senderId: String,
    val text: String,
    val imageUrl: String = "",
    val timestamp: Long
)
