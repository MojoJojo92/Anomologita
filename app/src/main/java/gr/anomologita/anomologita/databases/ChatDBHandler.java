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
}