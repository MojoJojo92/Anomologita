package gr.anomologita.anomologita.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import gr.anomologita.anomologita.objects.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "notificationsManager",
            TABLE_NOTIFICATIONS = "notifications",
            KEY_NOTIFICATION_ID = "notificationID",
            KEY_TEXT = "text",
            KEY_TYPE = "type",
            KEY_ID = "id",
            KEY_TIME = "time",
            KEY_ADMIN_ID = "adminID";

    public NotificationDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATIONS + "(" +
                KEY_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_TEXT + " TEXT, " +
                KEY_TYPE + " TEXT, " +
                KEY_ID + " TEXT, " +
                KEY_TIME + " TEXT, " +
                KEY_ADMIN_ID + "TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion){
            switch (upgradeTo){
                case 2:
                    String sql = "ALTER TABLE " + TABLE_NOTIFICATIONS + " ADD COLUMN " + KEY_ADMIN_ID + " TEXT";
                    db.execSQL(sql);
                    break;
            }
            upgradeTo++;
        }
    }

    public void createNotification(Notification notification) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TEXT, notification.getText());
        values.put(KEY_TYPE, notification.getType());
        values.put(KEY_ID, notification.getId());
        values.put(KEY_TIME, notification.getTime());
        values.put(KEY_ADMIN_ID, notification.getAdminID());
        db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
    }

    public void deleteNotification(Notification notification) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NOTIFICATIONS, KEY_NOTIFICATION_ID + "=?", new String[]{String.valueOf(notification.getNotificationID())});
        db.close();
    }

    public void updateNotification(Notification notification) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NOTIFICATIONS +
                " SET " + KEY_TEXT + " = '" + notification.getText() +
                "', " + KEY_TIME + " = '" + notification.getTime() +
                "' WHERE " + KEY_TYPE + " = '" + notification.getType() +
                "' AND " + KEY_ID + " = '" + notification.getId() +
                "' AND " + KEY_ADMIN_ID + " = '" + notification.getAdminID() + "'");
        db.close();
    }

    public List<Notification> getAllNotifications() {
        List<Notification> notifications = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTIFICATIONS + " ORDER BY " + KEY_TIME + " ASC", null);
        if (cursor.moveToFirst()) {
            do {
                Notification notification = new Notification();
                notification.setNotificationID(cursor.getInt(0));
                notification.setText(cursor.getString(1));
                notification.setType(cursor.getString(2));
                notification.setId(cursor.getString(3));
                notification.setTime(cursor.getString(4));
                notification.setAdminID(cursor.getString(5));
                notifications.add(notification);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notifications;
    }

    public boolean exists(String id, String type){
        SQLiteDatabase db = getReadableDatabase();
        String Query = "SELECT * FROM " + TABLE_NOTIFICATIONS + " WHERE " + KEY_ID + " = '" + id +"' AND " + KEY_TYPE + " = '" + type +"'";
        Cursor cursor = db.rawQuery(Query, null);
        int count = cursor.getCount();
        db.close();
        cursor.close();
        return count > 0;
    }
}
