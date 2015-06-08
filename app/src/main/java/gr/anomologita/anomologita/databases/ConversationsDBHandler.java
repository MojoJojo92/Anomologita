package gr.anomologita.anomologita.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import gr.anomologita.anomologita.objects.Conversation;

import java.util.ArrayList;
import java.util.List;

public class ConversationsDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "conversationsManager",
            TABLE_CONVERSATIONS = "conversations",
            KEY_CONVERSATION_ID = "conversationID",
            KEY_SENDER_REG_ID = "senderID",
            KEY_NAME = "name",
            KEY_RECEIVER_REG_ID = "receiverID",
            KEY_HASHTAG = "hashtag",
            KEY_POST_ID = "postID",
            KEY_LAST_TEXT = "lastMessage",
            KEY_TIME = "time",
            KEY_LAST_SENDER_ID = "lastSenderID",
            KEY_SEEN = "seen";

    public ConversationsDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CONVERSATIONS + "(" +
                KEY_CONVERSATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_SENDER_REG_ID + " TEXT, " +
                KEY_NAME + " TEXT, " +
                KEY_RECEIVER_REG_ID + " TEXT, " +
                KEY_HASHTAG + " TEXT, " +
                KEY_POST_ID + " TEXT, " +
                KEY_LAST_TEXT + " TEXT, " +
                KEY_TIME + " TEXT, " +
                KEY_LAST_SENDER_ID + " TEXT, " +
                KEY_SEEN + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATIONS);
        onCreate(db);
    }

    public void createConversation(Conversation conversation) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SENDER_REG_ID, conversation.getSenderRegID());
        values.put(KEY_NAME, conversation.getName());
        values.put(KEY_RECEIVER_REG_ID, conversation.getReceiverRegID());
        values.put(KEY_HASHTAG, conversation.getHashtag());
        values.put(KEY_POST_ID, conversation.getPostID());
        values.put(KEY_LAST_TEXT, conversation.getLastMessage());
        values.put(KEY_TIME, conversation.getTime());
        values.put(KEY_LAST_SENDER_ID, conversation.getLastSenderID());
        values.put(KEY_SEEN, conversation.getSeen());
        db.insert(TABLE_CONVERSATIONS, null, values);
        db.close();
    }

    public Conversation getConversation(String postID) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONVERSATIONS + " WHERE " + KEY_POST_ID + " = '" + postID + "'", null);
        Conversation conversation = new Conversation();
        if (cursor != null){
            cursor.moveToFirst();
            conversation.setConversationID(cursor.getInt(0));
            conversation.setSenderRegID(cursor.getString(1));
            conversation.setName(cursor.getString(2));
            conversation.setReceiverRegID(cursor.getString(3));
            conversation.setHashtag(cursor.getString(4));
            conversation.setPostID(cursor.getString(5));
            conversation.setLastMessage(cursor.getString(6));
            conversation.setTime(cursor.getString(7));
            conversation.setLastSenderID(cursor.getString(8));
            conversation.setSeen(cursor.getString(9));
        }
        db.close();
        cursor.close();
        return conversation;
    }

    public void deleteConversation(int conversationID) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CONVERSATIONS, KEY_CONVERSATION_ID + "=?", new String[]{String.valueOf(conversationID)});
        db.close();
    }

    public void updateConversation(Conversation conversation) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SENDER_REG_ID, conversation.getSenderRegID());
        values.put(KEY_NAME, conversation.getName());
        values.put(KEY_RECEIVER_REG_ID, conversation.getReceiverRegID());
        values.put(KEY_HASHTAG, conversation.getHashtag());
        values.put(KEY_POST_ID, conversation.getPostID());
        values.put(KEY_LAST_TEXT, conversation.getLastMessage());
        values.put(KEY_TIME, conversation.getTime());
        values.put(KEY_LAST_SENDER_ID, conversation.getLastSenderID());
        values.put(KEY_SEEN, conversation.getSeen());
        db.update(TABLE_CONVERSATIONS, values, KEY_CONVERSATION_ID + "=?", new String[]{String.valueOf(conversation.getConversationID())});
        db.close();
    }

    public List<Conversation> getAllConversations() {
        List<Conversation> conversations = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONVERSATIONS + " ORDER BY " + KEY_TIME + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                Conversation conversation = new Conversation();
                conversation.setConversationID(cursor.getInt(0));
                conversation.setSenderRegID(cursor.getString(1));
                conversation.setName(cursor.getString(2));
                conversation.setReceiverRegID(cursor.getString(3));
                conversation.setHashtag(cursor.getString(4));
                conversation.setPostID(cursor.getString(5));
                conversation.setLastMessage(cursor.getString(6));
                conversation.setTime(cursor.getString(7));
                conversation.setLastSenderID(cursor.getString(8));
                conversation.setSeen(cursor.getString(9));
                conversations.add(conversation);
            }
            while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return conversations;
    }

    public boolean exists(String postID) {
        SQLiteDatabase db = getReadableDatabase();
        String Query = "SELECT * FROM " + TABLE_CONVERSATIONS + " WHERE " + KEY_POST_ID + " = '" + postID + "'";
        Cursor cursor = db.rawQuery(Query, null);
        int count = cursor.getCount();
        db.close();
        cursor.close();
        return count > 0;
    }
}