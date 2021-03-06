package gr.anomologita.anomologita.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import gr.anomologita.anomologita.objects.Favorite;
import gr.anomologita.anomologita.objects.GroupProfile;

public class FavoritesDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "favoritesManager",
            TABLE_FAVORITES = "favorites",
            KEY_ID = "id",
            KEY_NAME = "name",
            KEY_USER_ID = "userID",
            KEY_SUBS = "subs";

    public FavoritesDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_FAVORITES + "(" +
                KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_NAME + " TEXT, " +
                KEY_USER_ID + " TEXT, " +
                KEY_SUBS + " INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    public void createFavorite(GroupProfile groupProfile) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, groupProfile.getGroup_id());
        values.put(KEY_NAME, groupProfile.getGroupName());
        values.put(KEY_USER_ID, groupProfile.getUser_id());
        values.put(KEY_SUBS, groupProfile.getSubscribers());
        db.insert(TABLE_FAVORITES, null, values);
        db.close();
    }

    public Favorite getFavorite(String group_name) {
        if (group_name != null && group_name.contains("'"))
            group_name = group_name.replaceAll("'", "''");
        SQLiteDatabase db = getReadableDatabase();
        String Query = "SELECT * FROM " + TABLE_FAVORITES + " WHERE " + KEY_NAME + " = '" + group_name + "'";
        Cursor cursor1 = db.rawQuery(Query, null);
        cursor1.moveToFirst();
        int id = Integer.parseInt(cursor1.getString(0));
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{KEY_ID, KEY_NAME, KEY_USER_ID, KEY_SUBS}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        Favorite favorite = new Favorite();
        if (cursor != null) {
            cursor.moveToFirst();
            favorite.setId(cursor.getInt(0));
            favorite.set_name(cursor.getString(1));
            favorite.setUserID(cursor.getString(2));
            favorite.setSubs(cursor.getInt(3));
            cursor.close();
        }
        db.close();
        cursor1.close();
        return favorite;
    }

    public void deleteFavorite(int groupID) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_FAVORITES, KEY_ID + "=?", new String[]{String.valueOf(groupID)});
        db.close();
    }

    public void updateFavorite(GroupProfile groupProfile) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, groupProfile.getGroup_id());
        values.put(KEY_NAME, groupProfile.getGroupName());
        values.put(KEY_USER_ID, groupProfile.getUser_id());
        values.put(KEY_SUBS, groupProfile.getSubscribers());
        db.update(TABLE_FAVORITES, values, KEY_ID + "=?", new String[]{String.valueOf(groupProfile.getGroup_id())});
        db.close();
    }

    public List<Favorite> getAllFavorites() {
        List<Favorite> favorites = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FAVORITES, null);
        if (cursor.moveToFirst()) {
            do {
                Favorite favorite = new Favorite();
                favorite.setId(cursor.getInt(0));
                if (cursor.getString(1).contains("''"))
                    favorite.set_name(cursor.getString(1).replaceAll("''", "'"));
                else
                    favorite.set_name(cursor.getString(1));
                favorite.setUserID(cursor.getString(2));
                favorite.setSubs(cursor.getInt(3));
                favorites.add(favorite);
            }
            while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return favorites;
    }

    public boolean exists(String group_name) {
        if (group_name != null && group_name.contains("'"))
            group_name = group_name.replaceAll("'", "''");
        SQLiteDatabase db = getReadableDatabase();
        String Query = "SELECT * FROM " + TABLE_FAVORITES + " WHERE " + KEY_NAME + " = '" + group_name + "'";
        Cursor cursor = db.rawQuery(Query, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }
}
