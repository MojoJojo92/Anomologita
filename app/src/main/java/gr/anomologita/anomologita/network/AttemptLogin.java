package gr.anomologita.anomologita.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.FitCenter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.databases.LikesDBHandler;
import gr.anomologita.anomologita.extras.Keys.CheckGroupComplete;
import gr.anomologita.anomologita.extras.Keys.CommentComplete;
import gr.anomologita.anomologita.extras.Keys.CreateGroupComplete;
import gr.anomologita.anomologita.extras.Keys.EndpointGroups;
import gr.anomologita.anomologita.extras.Keys.GetConComplete;
import gr.anomologita.anomologita.extras.Keys.GetGroupProfileComplete;
import gr.anomologita.anomologita.extras.Keys.GetPostsComplete;
import gr.anomologita.anomologita.extras.Keys.ImageEditComplete;
import gr.anomologita.anomologita.extras.Keys.ImageSetComplete;
import gr.anomologita.anomologita.extras.Keys.InputValues;
import gr.anomologita.anomologita.extras.Keys.MessComplete;
import gr.anomologita.anomologita.extras.Keys.MyGroupsComplete;
import gr.anomologita.anomologita.extras.Keys.MyPostsComplete;
import gr.anomologita.anomologita.extras.Keys.NotificationsComplete;
import gr.anomologita.anomologita.extras.Keys.PostComplete;
import gr.anomologita.anomologita.extras.Keys.SearchComplete;
import gr.anomologita.anomologita.objects.ChatMessage;
import gr.anomologita.anomologita.objects.Comment;
import gr.anomologita.anomologita.objects.Conversation;
import gr.anomologita.anomologita.objects.GroupProfile;
import gr.anomologita.anomologita.objects.GroupSearch;
import gr.anomologita.anomologita.objects.Notification;
import gr.anomologita.anomologita.objects.Post;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class AttemptLogin extends AsyncTask<String, String, String> implements EndpointGroups, InputValues {

    private GetConComplete getConComplete;
    private MessComplete messComplete;
    private CommentComplete commentComplete;
    private NotificationsComplete notificationsComplete;
    private SearchComplete searchComplete;
    private MyPostsComplete myPostsComplete;
    private MyGroupsComplete myGroupsComplete;
    private GetPostsComplete getPostsComplete;
    private GetGroupProfileComplete getGroupProfileComplete;
    private ImageEditComplete imageEditComplete;
    private CreateGroupComplete createGroupComplete;
    private ImageSetComplete imageSetComplete;
    private PostComplete postComplete;
    private CheckGroupComplete checkGroupComplete;

    private JSONParser jsonParser = new JSONParser();
    private String counter, groupName, post, location, conID, receiverID, message, postID, comment, what, type, text, id,
            liked, commented, name, search, hashtag, image, subscribed, groupID, sort, topRange, bottomRange, regID, operation, senderRegID, receiverRegID;
    private int mode;
    private Context context = Anomologita.getAppContext();
    private GroupProfile groupProfile;
    private Bitmap imageBitmap;
    private Uri uri;
    private Boolean exists;

    private List<Conversation> conversations;
    private List<ChatMessage> chatMessages;
    private List<Comment> comments;
    private List<Notification> likedNotifications, commentedNotifications, subscribedNotifications;
    private List<GroupSearch> groupSearches;
    private List<Post> posts;
    private List<GroupProfile> userGroups;

    public AttemptLogin(int mode, String s1, String s2) {
        if (mode == 0) {
            this.mode = mode;
            this.counter = s1;
            this.groupName = s2;
        } else if (mode == 16) {
            this.mode = mode;
            this.postID = s1;
            this.liked = s2;
        } else if (mode == 17) {
            this.mode = mode;
            this.groupID = s1;
            this.hashtag = s2;
        } else if (mode == 21) {
            this.mode = mode;
            this.hashtag = s1;
            this.groupName = s2;
        } else if (mode == 24) {
            this.mode = mode;
            this.groupName = s1;
            this.groupID = s2;
        }
    }

    public AttemptLogin(int mode, String image, String groupID, ImageSetComplete imageSetComplete) {
        this.mode = mode;
        this.image = image;
        this.groupID = groupID;
        this.imageSetComplete = imageSetComplete;
    }

    public AttemptLogin(int mode, GetConComplete getConComplete) {
        this.mode = mode;
        this.getConComplete = getConComplete;
    }

    public AttemptLogin(int mode, String post, String location, String groupID, PostComplete postComplete) {
        this.mode = mode;
        this.post = post;
        this.location = location;
        this.groupID = groupID;
        this.postComplete = postComplete;
    }

    public AttemptLogin(int mode, String conID, MessComplete messComplete) {
        this.mode = mode;
        this.conID = conID;
        this.messComplete = messComplete;
    }

    public AttemptLogin(int mode, String conID, String receiverID, String message, MessComplete messComplete) {
        this.mode = mode;
        this.conID = conID;
        this.receiverID = receiverID;
        this.message = message;
        this.messComplete = messComplete;
    }

    public AttemptLogin(int mode, String postID, String comment, String what, CommentComplete commentComplete) {
        this.mode = mode;
        this.postID = postID;
        this.comment = comment;
        this.what = what;
        this.commentComplete = commentComplete;
    }

    public AttemptLogin(int mode, NotificationsComplete notificationsComplete) {
        this.mode = mode;
        this.notificationsComplete = notificationsComplete;
    }

    public AttemptLogin(int mode, String senderRegID, String receiverRegID, String name, String message, String hashtag, String operation, String postID) {
        this.mode = mode;
        this.senderRegID = senderRegID;
        this.receiverRegID = receiverRegID;
        this.name = name;
        this.hashtag = hashtag;
        this.message = message;
        this.operation = operation;
        this.postID = postID;
    }

    public AttemptLogin(int mode, String search, SearchComplete searchComplete) {
        this.mode = mode;
        this.search = search;
        this.searchComplete = searchComplete;
    }

    public AttemptLogin(int mode, MyPostsComplete myPostsComplete) {
        this.mode = mode;
        this.myPostsComplete = myPostsComplete;
    }

    public AttemptLogin(int mode, String postID, MyPostsComplete myPostsComplete) {
        this.mode = mode;
        this.postID = postID;
        this.myPostsComplete = myPostsComplete;
    }

    public AttemptLogin(int mode, String s) {
        if (mode == 25) {
            this.mode = mode;
            this.conID = s;
        } else if (mode == 27) {
            this.mode = mode;
            this.regID = s;
        } else {
            this.mode = mode;
            this.groupID = s;
        }
    }

    public AttemptLogin(int mode, MyGroupsComplete myGroupsComplete) {
        this.mode = mode;
        this.myGroupsComplete = myGroupsComplete;
    }

    public AttemptLogin(int mode, String groupID, GetGroupProfileComplete getGroupProfileComplete) {
        this.mode = mode;
        this.groupID = groupID;
        this.getGroupProfileComplete = getGroupProfileComplete;
    }

    public AttemptLogin(int mode, String text, String type, String id, String regID) {
        this.mode = mode;
        this.text = text;
        this.type = type;
        this.id = id;
        this.regID = regID;
    }

    public AttemptLogin(int mode, String groupID, String sort, String topRange, String bottomRange, GetPostsComplete getPostsComplete) {
        this.mode = mode;
        this.groupID = groupID;
        this.sort = sort;
        this.topRange = topRange;
        this.bottomRange = bottomRange;
        this.getPostsComplete = getPostsComplete;
    }

    public AttemptLogin(int mode, Uri uri, ImageEditComplete imageEditComplete) {
        this.mode = mode;
        this.uri = uri;
        this.imageEditComplete = imageEditComplete;
    }

    public AttemptLogin(int mode, String hashtag, String groupName, CreateGroupComplete createGroupComplete, String s) {
        this.mode = mode;
        this.hashtag = hashtag;
        this.groupName = groupName;
        this.createGroupComplete = createGroupComplete;
    }

    public AttemptLogin(int mode, String groupName, CheckGroupComplete checkGroupComplete) {
        this.mode = mode;
        this.groupName = groupName;
        this.checkGroupComplete = checkGroupComplete;
    }

    public AttemptLogin(int mode, String s1, String s2, String s3) {
        this.mode = mode;
        this.postID = s1;
        this.post = s2;
        this.location = s3;
    }

    @Override
    protected String doInBackground(String... args) {
        switch (mode) {
            case 0:
                return setSubscribers();
            case 2:
                return post();
            case 5:
                return comment();
            case 9:
                return personalMessage();
            case 10:
                return search();
            case 11:
                return getUserPosts();
            case 12:
                return deletePost();
            case 13:
                return getUserGroups();
            case 14:
                return getPosts();
            case 15:
                return getGroup();
            case 16:
                return setLike();
            case 17:
                return setHashtag();
            case 18:
                return deleteGroup();
            case 19:
                return setImage();
            case 20:
                return editImage();
            case 21:
                return createGroup();
            case 23:
                return checkGroup();
            case 24:
                return setGroupName();
            case 25:
                return deleteConversation();
            case 26:
                return editPost();
            case 28:
                return sendNotification();
            default:
                return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        switch (mode) {
            case 1:
                getConComplete.onGetConversationsCompleted(conversations);
                break;
            case 2:
                postComplete.onPostComplete(postID, hashtag);
                break;
            case 3:
                messComplete.onGetMessagesCompleted(chatMessages);
                break;
            case 4:
                messComplete.onSetMessagesCompleted();
                break;
            case 5:
                commentComplete.onCommentCompleted(comments, what);
                break;
            case 6:
                notificationsComplete.onCheckUserCompleted(liked, commented, subscribed);
                break;
            case 7:
                notificationsComplete.onCheckLikedCompleted(likedNotifications);
                break;
            case 8:
                notificationsComplete.onCheckCommentedCompleted(commentedNotifications);
                break;
            case 10:
                searchComplete.onSearchCompleted(groupSearches);
                break;
            case 11:
                myPostsComplete.onGetUserPostsCompleted(posts);
                break;
            case 12:
                myPostsComplete.onDeleteUserPostCompleted();
                break;
            case 13:
                myGroupsComplete.onGetUserGroupsCompleted(userGroups);
                break;
            case 14:
                getPostsComplete.onGetPostsComplete(posts);
                break;
            case 15:
                getGroupProfileComplete.onGetGroupComplete(groupProfile);
                break;
            case 19:
                imageSetComplete.onImageSetComplete();
                break;
            case 20:
                imageEditComplete.onImageEditComplete(imageBitmap);
                break;
            case 21:
                createGroupComplete.onCreateGroupComplete(groupID);
                break;
            case 22:
                notificationsComplete.onCheckSubscribedCompleted(subscribedNotifications);
                break;
            case 23:
                checkGroupComplete.onCheckGroupComplete(exists);
                break;
            default:
                break;
        }
    }

    private String setSubscribers() {
        int success;
        try {
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("group_name", groupName));
            params.add(new BasicNameValuePair("counter", counter.trim()));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(EndpointGroups.URL_SET_SUBSCRIBERS, "POST", params);

            Log.d("Login attempt", json.toString());
            success = json.getInt(EndpointGroups.TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(EndpointGroups.TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(EndpointGroups.TAG_MESSAGE));
                return json.getString(EndpointGroups.TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String post() {
        int success;
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("post", post));
            params.add(new BasicNameValuePair("where", location));
            params.add(new BasicNameValuePair("group_id", groupID));
            params.add(new BasicNameValuePair("user_id", Anomologita.userID));
            params.add(new BasicNameValuePair("group_name", Anomologita.getCurrentGroupName()));
            params.add(new BasicNameValuePair("reg_id", Anomologita.regID));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(URL_POST, "POST", params);
            Log.d("Login attempt", json.toString());

            success = json.getInt(TAG_SUCCESS);
            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                postID = String.valueOf(json.getInt("postID"));
                hashtag = json.getString("hashtag");
                return json.getString(TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String comment() {
        int success;
        if (what.equals("setComment")) {
            try {
                List<BasicNameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("comment", comment));
                params.add(new BasicNameValuePair("post_id", postID));
                Log.d("request!", "starting");
                JSONObject json = jsonParser.makeHttpRequest(EndpointGroups.URL_COMMENT, "POST", params);
                Log.d("Login PreCount attempt", json.toString());

                success = json.getInt(EndpointGroups.TAG_SUCCESS);

                if (success == 1) {
                    Log.d("Login Successful!", json.toString());
                    return json.getString(EndpointGroups.TAG_MESSAGE);
                } else {
                    Log.d("Login Failure!", json.getString(EndpointGroups.TAG_MESSAGE));
                    return json.getString(EndpointGroups.TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                comments = new ArrayList<>();
                List<BasicNameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("post_id", postID));

                JSONObject json = jsonParser.makeHttpRequest(EndpointGroups.URL_GET_COMMENTS, "GET", params);

                for (int i = 0; i < json.length() - 2; i++) {
                    Comment comment = new Comment();
                    comment.setComment(json.getJSONObject(String.valueOf(i)).getString("Comment"));
                    comment.setCommentID(json.getJSONObject(String.valueOf(i)).getInt("comment_id"));
                    comment.setPostID(json.getJSONObject(String.valueOf(i)).getInt("id_post"));
                    comments.add(comment);
                }

                Log.d("Login attempt", json.toString());
                success = json.getInt(EndpointGroups.TAG_SUCCESS);
                Log.d("success", String.valueOf(success));

                if (success == 1) {
                    Log.d("Login Successful!", json.toString());
                    return json.getString(EndpointGroups.TAG_MESSAGE);
                } else {
                    Log.d("Login Failure!", json.getString(EndpointGroups.TAG_MESSAGE));
                    return json.getString(EndpointGroups.TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String personalMessage() {
        int success;
        try {
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("senderRegID", senderRegID));
            params.add(new BasicNameValuePair("receiverRegID", receiverRegID));
            params.add(new BasicNameValuePair("postHashtag", hashtag));
            params.add(new BasicNameValuePair("message", message));
            params.add(new BasicNameValuePair("operation", operation));
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("postID", postID));
            params.add(new BasicNameValuePair("lastSenderID", Anomologita.userID));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(EndpointGroups.URL_SEND_MESSAGE, "POST", params);
            Log.d("Login attempt", json.toString());
            success = json.getInt(TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String search() {
        int success;
        try {
            groupSearches = new ArrayList<>();

            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("search", search));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(URL_SEARCH, "GET", params);

            for (int i = 0; i < json.length() - 2; i++) {
                GroupSearch groupSearch = new GroupSearch();
                groupSearch.setTitle(json.getJSONObject(String.valueOf(i)).getString(KEY_GROUP_NAME));
                groupSearch.setGroupID(json.getJSONObject(String.valueOf(i)).getInt(KEY_GROUP_ID));
                groupSearches.add(groupSearch);
            }

            Log.d("Login attempt", json.toString());
            success = json.getInt(TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getUserPosts() {
        int success;
        try {
            posts = new ArrayList<>();
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("user_id", Anomologita.userID));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(URL_GET_USER_POSTS, "GET", params);

            for (int i = 0; i < json.length() - 2; i++) {
                Post myPost = new Post();
                myPost.setPost_txt(json.getJSONObject(String.valueOf(i)).getString(KEY_POST));
                myPost.setLikes(json.getJSONObject(String.valueOf(i)).getInt(KEY_RATING));
                myPost.setComments(json.getJSONObject(String.valueOf(i)).getInt(KEY_COMMENT_COUNT));
                myPost.setHashtagName(json.getJSONObject(String.valueOf(i)).getString(KEY_HASHTAG));
                myPost.setLocation(json.getJSONObject(String.valueOf(i)).getString(KEY_LOCATION));
                myPost.setPost_id(json.getJSONObject(String.valueOf(i)).getInt(KEY_POST_ID));
                myPost.setGroup_name(json.getJSONObject(String.valueOf(i)).getString(KEY_GROUP_NAME));
                myPost.setTimestamp(json.getJSONObject(String.valueOf(i)).getString(KEY_DATE));
                posts.add(myPost);
            }
            Collections.reverse(posts);

            Log.d("Login attempt", json.toString());
            success = json.getInt(TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String deletePost() {
        int success;
        try {
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("post_id", postID));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(URL_DELETE_POST, "POST", params);

            Log.d("Login attempt", json.toString());
            success = json.getInt(TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getUserGroups() {
        int success;
        try {
            userGroups = new ArrayList<>();
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("user_id", Anomologita.userID));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(URL_GET_USER_GROUPS, "GET", params);

            for (int i = 0; i < json.length() - 2; i++) {
                GroupProfile groupProfile = new GroupProfile();
                groupProfile.setGroupName(json.getJSONObject(String.valueOf(i)).getString(KEY_GROUP_NAME));
                groupProfile.setHashtag_name(json.getJSONObject(String.valueOf(i)).getString(KEY_GROUP_HASHTAG));
                groupProfile.setSubscribers(json.getJSONObject(String.valueOf(i)).getInt(KEY_SUBSCRIBERS));
                groupProfile.setGroup_id(json.getJSONObject(String.valueOf(i)).getInt(KEY_GROUP_ID));
                userGroups.add(groupProfile);
            }
            Collections.reverse(userGroups);

            Log.d("Login attempt", json.toString());
            success = json.getInt(TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPosts() {
        int success;
        try {
            posts = new ArrayList<>();
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("group_id", groupID));
            params.add(new BasicNameValuePair("sort", sort));
            params.add(new BasicNameValuePair("top_range", topRange));
            params.add(new BasicNameValuePair("bottom_range", bottomRange));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(URL_GET_POSTS, "GET", params);

            for (int i = 0; i < json.length() - 2; i++) {

                Post post = new Post();
                post.setPost_txt(json.getJSONObject(String.valueOf(i)).getString(KEY_POST));
                post.setLocation(json.getJSONObject(String.valueOf(i)).getString(KEY_LOCATION));
                post.setPost_id(json.getJSONObject(String.valueOf(i)).getInt(KEY_POST_ID));
                post.setUser_id(json.getJSONObject(String.valueOf(i)).getInt(KEY_USER_ID));
                post.setTimestamp(json.getJSONObject(String.valueOf(i)).getString(KEY_DATE));
                LikesDBHandler db = new LikesDBHandler(context);
                if (db.exists(post.getPost_id())) {
                    post.setLiked(true);
                } else
                    post.setLiked(false);
                post.setLikes(json.getJSONObject(String.valueOf(i)).getInt(KEY_RATING));
                post.setComments(json.getJSONObject(String.valueOf(i)).getInt(KEY_COMMENT_COUNT));
                post.setHashtagName(json.getJSONObject(String.valueOf(i)).getString(KEY_HASHTAG));
                post.setGroup_id(json.getJSONObject(String.valueOf(i)).getInt(KEY_GROUP_ID));
                post.setReg_id(json.getJSONObject(String.valueOf(i)).getString(KEY_REG_ID));
                posts.add(post);
                db.close();

            }

            Log.d("Login attempt", json.toString());
            success = json.getInt(EndpointGroups.TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(EndpointGroups.TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(EndpointGroups.TAG_MESSAGE));
                return json.getString(EndpointGroups.TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getGroup() {
        int success;
        try {
            groupProfile = new GroupProfile();
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("group_id", groupID));

            //tempSubs = group.getSubscribers();

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(EndpointGroups.URL_GET_GROUP, "GET", params);
            groupProfile.setGroupName(json.getJSONObject(String.valueOf(0)).getString(KEY_GROUP_NAME));
            groupProfile.setSubscribers(json.getJSONObject(String.valueOf(0)).getInt(KEY_SUBSCRIBERS));
            groupProfile.setHashtag_name(json.getJSONObject(String.valueOf(0)).getString(KEY_GROUP_HASHTAG));
            groupProfile.setUser_id(json.getJSONObject(String.valueOf(0)).getInt(KEY_USER_ID));
            groupProfile.setGroup_id(json.getJSONObject(String.valueOf(0)).getInt(KEY_GROUP_ID));
            groupProfile.setRegID(json.getJSONObject(String.valueOf(0)).getString(KEY_REG_ID));

            Log.d("Login attempt", json.toString());
            success = json.getInt(EndpointGroups.TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(EndpointGroups.TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(EndpointGroups.TAG_MESSAGE));
                return json.getString(EndpointGroups.TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String setLike() {
        int success;
        try {
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("post_id", postID.trim()));
            params.add(new BasicNameValuePair("like", liked.trim()));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(URL_SET_LIKE, "POST", params);

            Log.d("Login attempt", json.toString());
            success = json.getInt(TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String setHashtag() {
        int success;
        try {

            if (!hashtag.equals(null)) {
                List<BasicNameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("new_hashtag", hashtag));
                params.add(new BasicNameValuePair("group_id", groupID));

                Log.d("request!", "starting");
                JSONObject json = jsonParser.makeHttpRequest(URL_EDIT_HASHTAG, "POST", params);
                Log.d("Login attempt", json.toString());
                success = json.getInt(TAG_SUCCESS);
                Log.d("success", String.valueOf(success));

                if (success == 1) {
                    Log.d("Login Successful!", json.toString());
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String deleteGroup() {
        int success;
        try {
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("group_id", groupID));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(URL_DELETE_GROUP, "POST", params);

            Log.d("Login attempt", json.toString());
            success = json.getInt(TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String setImage() {
        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("image", image));
        params.add(new BasicNameValuePair("group_id", groupID));

        HttpEntity httpEntity = null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();  // Default HttpClient

            HttpPost httpPost = new HttpPost(EndpointGroups.URL_SET_GROUP_IMG);
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            httpEntity = httpResponse.getEntity();
            String entityResponse = EntityUtils.toString(httpEntity);

            Log.e("Entity Response  : ", entityResponse);
            return "1";

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String editImage() {
        try {
            BitmapPool pool = Glide.get(context).getBitmapPool();
            imageBitmap = Glide.with(context).load(uri).asBitmap().transform(new CropCircleTransformation(pool), new FitCenter(pool)).into(250, 250).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String createGroup() {
        int success;
        try {
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("group_name", groupName));
            params.add(new BasicNameValuePair("subscribers", "0"));
            params.add(new BasicNameValuePair("hashtag_name", hashtag));
            params.add(new BasicNameValuePair("hashtag_counter", "0"));
            params.add(new BasicNameValuePair("user_id", Anomologita.userID));
            params.add(new BasicNameValuePair("regID", Anomologita.regID));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(EndpointGroups.URL_SET_GROUP, "POST",
                    params);
            Log.d("Login attempt", json.toString());

            success = json.getInt(EndpointGroups.TAG_SUCCESS);
            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                groupID = String.valueOf(json.getInt("id"));
                return json.getString(EndpointGroups.TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(EndpointGroups.TAG_MESSAGE));
                return json.getString(EndpointGroups.TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String checkGroup() {
        int success;
        try {
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("group_name", groupName));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(URL_CHECK_GROUP, "GET", params);

            Log.d("Login attempt", json.toString());
            success = json.getInt(TAG_SUCCESS);
            Log.d("success", String.valueOf(success));
            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                if (json.getInt("exists") == 0)
                    exists = false;
                else
                    exists = true;
                return json.getString(TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String setGroupName() {
        int success;
        try {
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("group_id", groupID));
            params.add(new BasicNameValuePair("new_name", groupName));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(URL_SET_GROUP_NAME, "POST", params);
            Log.d("Login attempt", json.toString());
            success = json.getInt(TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String deleteConversation() {
        int success;
        try {
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("conversation_id", conID));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(URL_DELETE_CONVERSATION, "POST", params);

            Log.d("Login attempt", json.toString());
            success = json.getInt(TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String editPost() {
        int success;
        try {
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("post_id", postID));
            params.add(new BasicNameValuePair("new_post", post));
            params.add(new BasicNameValuePair("new_location", location));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(URL_EDIT_POST, "POST", params);
            Log.d("Login attempt", json.toString());
            success = json.getInt(TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String sendNotification() {
        int success;
        try {
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("text", text.trim()));
            params.add(new BasicNameValuePair("type", type));
            params.add(new BasicNameValuePair("id", id));
            params.add(new BasicNameValuePair("registrationIDs", regID));
            params.add(new BasicNameValuePair("operation", "notification"));

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(EndpointGroups.URL_SEND_NOTIFICATION, "POST", params);
            Log.d("Login attempt", json.toString());
            success = json.getInt(TAG_SUCCESS);
            Log.d("success", String.valueOf(success));

            if (success == 1) {
                Log.d("Login Successful!", json.toString());
                return json.getString(TAG_MESSAGE);
            } else {
                Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                return json.getString(TAG_MESSAGE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}