package gr.anomologita.anomologita.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import gr.anomologita.anomologita.objects.Post;

import java.util.ArrayList;
import java.util.List;

public class PostsDBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "postsManager",
            TABLE_POSTS = "posts",
            KEY_ID = "id",
            KEY_POST_ID = "postID",
            KEY_HASHTAG = "name",
            KEY_POST = "imageUri",
            KEY_LOCATION = "location",
            KEY_RATING = "rating",
            KEY_COMMENTS = "comments",
            KEY_GROUP = "groupName",
            KEY_GROUP_ID = "groupID",
            KEY_TIME = "time";


    public PostsDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_POSTS + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_POST_ID + " INTEGER, " +
                KEY_HASHTAG + " TEXT, " +
                KEY_POST + " TEXT, " +
                KEY_LOCATION + " TEXT, " +
                KEY_RATING + " INTEGER, " +
                KEY_COMMENTS + " INTEGER, " +
                KEY_GROUP + " TEXT, " +
                KEY_GROUP_ID + " INTEGER, " +
                KEY_TIME + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(db);
    }

    public void createPost(Post post) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_POST_ID, post.getPost_id());
        values.put(KEY_HASHTAG, post.getHashtagName());
        values.put(KEY_POST, post.getPost_txt());
        values.put(KEY_LOCATION, post.getLocation());
        values.put(KEY_RATING, post.getLikes());
        values.put(KEY_COMMENTS, post.getComments());
        values.put(KEY_GROUP, post.getGroup_name());
        values.put(KEY_GROUP_ID, post.getGroup_id());
        values.put(KEY_TIME, post.getTimestamp());
        db.insert(TABLE_POSTS, null, values);
        db.close();
    }

    public Post getPost(int postID) {
        SQLiteDatabase db = getReadableDatabase();
        String Query = "SELECT * FROM " + TABLE_POSTS + " WHERE " + KEY_POST_ID + " = '" + postID +"'";
        Cursor cursor1 = db.rawQuery(Query, null);
        cursor1.moveToFirst();
        int id = cursor1.getInt(0);
        Log.e("Query", String.valueOf(id));
        Cursor cursor = db.query(TABLE_POSTS,
                new String[]{KEY_ID,
                        KEY_POST_ID,
                        KEY_HASHTAG,
                        KEY_POST,
                        KEY_LOCATION,
                        KEY_RATING,
                        KEY_COMMENTS,
                        KEY_GROUP,
                        KEY_GROUP_ID,
                        KEY_TIME}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) cursor.moveToFirst();
        Post post = new Post();
        post.setDbID(cursor.getInt(0));
        post.setPost_id(cursor.getInt(1));
        post.setHashtagName(cursor.getString(2));
        post.setPost_txt(cursor.getString(3));
        post.setLocation(cursor.getString(4));
        post.setLikes(cursor.getInt(5));
        post.setComments(cursor.getInt(6));
        post.setGroup_name(cursor.getString(7));
        post.setGroup_id(cursor.getInt(8));
        post.setTimestamp(cursor.getString(9));
        db.close();
        cursor.close();
        cursor1.close();
        return post;
    }

    public void deletePost(Post post) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_POSTS, KEY_ID + "=?", new String[]{String.valueOf(post.getDbID())});
        db.close();
    }

    public void clearAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + TABLE_POSTS);
        db.close();
    }

    public int getPostCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_POSTS, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public int updatePost(Post post) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_POST_ID, post.getPost_id());
        values.put(KEY_HASHTAG, post.getHashtagName());
        values.put(KEY_POST, post.getPost_txt());
        values.put(KEY_LOCATION, post.getLocation());
        values.put(KEY_RATING, post.getLikes());
        values.put(KEY_COMMENTS, post.getComments());
        values.put(KEY_GROUP, post.getGroup_name());
        values.put(KEY_GROUP_ID, post.getGroup_id());
        values.put(KEY_TIME, post.getTimestamp());
        return db.update(TABLE_POSTS, values, KEY_ID + "=?", new String[]{String.valueOf(post.getDbID())});

    }

    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_POSTS, null);
        if (cursor.moveToFirst()) {
            do {
                Post post = new Post();
                post.setDbID(cursor.getInt(0));
                post.setPost_id(cursor.getInt(1));
                post.setHashtagName(cursor.getString(2));
                post.setPost_txt(cursor.getString(3));
                post.setLocation(cursor.getString(4));
                post.setLikes(cursor.getInt(5));
                post.setComments(cursor.getInt(6));
                post.setGroup_name(cursor.getString(7));
                post.setGroup_id(cursor.getInt(8));
                post.setTimestamp(cursor.getString(9));
                posts.add(post);
            }
            while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return posts;
    }

    public boolean exists(int postID){
        SQLiteDatabase db = getReadableDatabase();
        String Query = "SELECT * FROM " + TABLE_POSTS + " WHERE " + KEY_POST_ID + " = '" + postID +"'";
        Cursor cursor = db.rawQuery(Query, null);
        int count = cursor.getCount();
        db.close();
        cursor.close();
        return count > 0;
    }
}
