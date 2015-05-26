package gr.anomologita.anomologita.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import gr.anomologita.anomologita.objects.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MessageManager",
            TABLE_MESSAGES = "chats",
            KEY_MESSAGE_ID = "messageID",
            KEY_CONVERSATION_ID = "conversationID",
            KEY_MESSAGE = "message",
            KEY_SENDER_ID = "senderID",
            KEY_TIME = "time";

    public ChatDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_MESSAGES + "(" +
                KEY_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_CONVERSATION_ID + " INTEGER, " +
                KEY_MESSAGE + " TEXT, " +
                KEY_SENDER_ID + " TEXT, " +
                KEY_TIME + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    public void createMessage(ChatMessage message) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CONVERSATION_ID, message.getConversationID());
        values.put(KEY_MESSAGE, message.getMessage());
        values.put(KEY_SENDER_ID, message.getSenderID());
        values.put(KEY_TIME, message.getTime());
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    public List<ChatMessage> getConversationMessages(int conversationID) {
        List<ChatMessage> messages = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSAGES + " WHERE " + KEY_CONVERSATION_ID + " = '" + conversationID + "'", null);
        if (cursor.moveToFirst()) {
            do {
                ChatMessage message = new ChatMessage();
                message.setMessageID(cursor.getInt(0));
                message.setConversationID(cursor.getInt(1));
                message.setMessage(cursor.getString(2));
                message.setSenderID(cursor.getString(3));
                message.setTime(cursor.getString(4));
                messages.add(message);
            }
            while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return messages;
    }

    public void deleteMessage(ChatMessage message) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MESSAGES, KEY_MESSAGE_ID + "=?", new String[]{String.valueOf(message.getMessageID())});
        db.close();
    }

    public void clearAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + TABLE_MESSAGES);
        db.close();
    }

    public int getMessageCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSAGES, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public int updateMessage(ChatMessage message) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CONVERSATION_ID, message.getConversationID());
        values.put(KEY_MESSAGE, message.getMessage());
        values.put(KEY_SENDER_ID, message.getSenderID());
        values.put(KEY_TIME, message.getTime());
        return db.update(TABLE_MESSAGES, values, KEY_MESSAGE_ID + "=?", new String[]{String.valueOf(message.getMessageID())});

    }

    public List<ChatMessage> getAllMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSAGES, null);
        if (cursor.moveToFirst()) {
            do {
                ChatMessage message = new ChatMessage();
                message.setMessageID(cursor.getInt(0));
                message.setConversationID(cursor.getInt(1));
                message.setMessage(cursor.getString(2));
                message.setSenderID(cursor.getString(3));
                message.setTime(cursor.getString(4));
                messages.add(message);
            }
            while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return messages;
    }

    public boolean exists(int messageID) {
        SQLiteDatabase db = getReadableDatabase();
        String Query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + KEY_MESSAGE_ID + " = '" + messageID + "'";
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            return false;
        }
        db.close();
        cursor.close();
        return true;
    }
}