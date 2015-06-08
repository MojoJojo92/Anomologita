package gr.anomologita.anomologita.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LikesDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "likesManager",
            TABLE_LIKES = "likes",
            POST_ID = "post_id",
            LIKED = "liked";

    public LikesDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_LIKES + "(" +
                POST_ID + " INTEGER PRIMARY KEY, " +
                LIKED + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIKES);
        onCreate(db);
    }

    public void createLikes(int post_id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(POST_ID, post_id);
        values.put(LIKED, 1);
        db.insert(TABLE_LIKES, null, values);
        db.close();
    }

    public boolean exists(int post_id){
        SQLiteDatabase db = getReadableDatabase();
        String Query = "Select * from " + TABLE_LIKES + " where " + POST_ID + " = " + post_id;
        Cursor cursor = db.rawQuery(Query, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public void deleteLike(int post_id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LIKES, POST_ID + "=?", new String[]{String.valueOf(post_id)});
        db.close();
    }
}
