package gr.anomologita.anomologita.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.databases.LikesDBHandler;
import gr.anomologita.anomologita.databases.PostsDBHandler;
import gr.anomologita.anomologita.extras.Keys.CheckGroupComplete;
import gr.anomologita.anomologita.extras.Keys.CommentComplete;
import gr.anomologita.anomologita.extras.Keys.CreateGroupComplete;
import gr.anomologita.anomologita.extras.Keys.EndpointGroups;
import gr.anomologita.anomologita.extras.Keys.GetGroupProfileComplete;
import gr.anomologita.anomologita.extras.Keys.GetPostsComplete;
import gr.anomologita.anomologita.extras.Keys.ImageSetComplete;
import gr.anomologita.anomologita.extras.Keys.InputValues;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.extras.Keys.MyGroupsComplete;
import gr.anomologita.anomologita.extras.Keys.MyPostsComplete;
import gr.anomologita.anomologita.extras.Keys.PostComplete;
import gr.anomologita.anomologita.extras.Keys.SearchComplete;
import gr.anomologita.anomologita.objects.Comment;
import gr.anomologita.anomologita.objects.Favorite;
import gr.anomologita.anomologita.objects.GroupProfile;
import gr.anomologita.anomologita.objects.Post;

public class AttemptLogin extends AsyncTask<String, String, String> implements EndpointGroups, InputValues {

    private CommentComplete commentComplete;
    private SearchComplete searchComplete;
    private MyPostsComplete myPostsComplete;
    private MyGroupsComplete myGroupsComplete;
    private GetPostsComplete getPostsComplete;
    private GetGroupProfileComplete getGroupProfileComplete;
    private CreateGroupComplete createGroupComplete;
    private ImageSetComplete imageSetComplete;
    private PostComplete postComplete;
    private CheckGroupComplete checkGroupComplete;

    private final Context context = Anomologita.getAppContext();
    private final JSONParser jsonParser = new JSONParser();

    private String counter, groupName, post, location, message, postID, comment, what, type, text, id,
            liked, name, search, hashtag, image, groupID, sort, topRange, bottomRange, regID, senderRegID, receiverRegID;

    private int mode;
    private GroupProfile groupProfile;
    private Boolean exists;

    private List<Comment> comments;
    private List<Favorite> groupSearches;
    private List<Post> posts;
    private List<GroupProfile> userGroups;

    public AttemptLogin() {

    }

    public void setSubs(String counter, String groupName) {
        mode = LoginMode.SET_SUBSCRIBERS;
        this.counter = counter;
        this.groupName = groupName;
    }

    public void setGroupName(String groupName, String groupID) {
        mode = LoginMode.SET_GROUP_NAME;
        this.groupName = groupName;
        this.groupID = groupID;
    }

    public void setHashtag(String hashtag, String groupName) {
        mode = LoginMode.SET_HASHTAG;
        this.hashtag = hashtag;
        this.groupName = groupName;
    }

    public void setLike(String postID, String liked) {
        mode = LoginMode.SET_LIKE;
        this.postID = postID;
        this.liked = liked;
    }

    public void image(String image, String groupID, ImageSetComplete imageSetComplete) {
        mode = LoginMode.SET_IMAGE;
        this.image = image;
        this.groupID = groupID;
        this.imageSetComplete = imageSetComplete;
    }

    public void setPost(String post, String location, String groupID, PostComplete postComplete) {
        mode = LoginMode.POST;
        this.post = post;
        this.location = location;
        this.groupID = groupID;
        this.postComplete = postComplete;
    }

    public void setComment(String postID, String comment, String what, CommentComplete commentComplete) {
        mode = LoginMode.COMMENT;
        this.postID = postID;
        this.comment = comment;
        this.what = what;
        this.commentComplete = commentComplete;
    }

    public void getSearch(String search, SearchComplete searchComplete) {
        mode = LoginMode.SEARCH;
        this.search = search;
        this.searchComplete = searchComplete;
    }

    public void getUserPosts(MyPostsComplete myPostsComplete) {
        mode = LoginMode.GET_USER_POSTS;
        this.myPostsComplete = myPostsComplete;
    }

    public void deletePost(String postID, MyPostsComplete myPostsComplete) {
        mode = LoginMode.DELETE_POST;
        this.postID = postID;
        this.myPostsComplete = myPostsComplete;
    }

    public void deleteGroup(String groupID) {
        mode = LoginMode.DELETE_GROUP;
        this.groupID = groupID;
    }

    public void getUserGroups(MyGroupsComplete myGroupsComplete) {
        mode = LoginMode.GET_USER_GROUPS;
        this.myGroupsComplete = myGroupsComplete;
    }

    public void getGroup(String groupID, GetGroupProfileComplete getGroupProfileComplete) {
        mode = LoginMode.GET_GROUP;
        this.groupID = groupID;
        this.getGroupProfileComplete = getGroupProfileComplete;
    }

    public void sendNotification(String text, String type, String id, String regID) {
        mode = LoginMode.SEND_NOTIFICATION;
        this.text = text;
        this.type = type;
        this.id = id;
        this.regID = regID;
    }

    public void getPosts(String groupID, String sort, String topRange, String bottomRange, GetPostsComplete getPostsComplete) {
        mode = LoginMode.GET_POSTS;
        this.groupID = groupID;
        this.sort = sort;
        this.topRange = topRange;
        this.bottomRange = bottomRange;
        this.getPostsComplete = getPostsComplete;
    }

    public void createGroup(String hashtag, String groupName, CreateGroupComplete createGroupComplete) {
        mode = LoginMode.CREATE_GROUP;
        this.hashtag = hashtag;
        this.groupName = groupName;
        this.createGroupComplete = createGroupComplete;
    }

    public void checkGroup(String groupName, CheckGroupComplete checkGroupComplete) {
        mode = LoginMode.CHECK_GROUP;
        this.groupName = groupName;
        this.checkGroupComplete = checkGroupComplete;
    }

    public void editPost(String postID, String post, String location) {
        mode = LoginMode.EDIT_POST;
        this.postID = postID;
        this.post = post;
        this.location = location;
    }

    public void sendMessage(String senderRegID, String receiverRegID, String name, String message, String hashtag, String postID) {
        mode = LoginMode.PERSONAL_MESSAGE;
        this.senderRegID = senderRegID;
        this.receiverRegID = receiverRegID;
        this.name = name;
        this.hashtag = hashtag;
        this.message = message;
        this.postID = postID;
    }

    @Override
    protected String doInBackground(String... args) {
        switch (mode) {
            case LoginMode.SET_SUBSCRIBERS:
                return setSubscribers();
            case LoginMode.POST:
                return post();
            case LoginMode.COMMENT:
                return comment();
            case LoginMode.PERSONAL_MESSAGE:
                return personalMessage();
            case LoginMode.SEARCH:
                return search();
            case LoginMode.GET_USER_POSTS:
                return getUserPosts();
            case LoginMode.DELETE_POST:
                return deletePost();
            case LoginMode.GET_USER_GROUPS:
                return getUserGroups();
            case LoginMode.GET_POSTS:
                return getPosts();
            case LoginMode.GET_GROUP:
                return getGroup();
            case LoginMode.SET_LIKE:
                return setLike();
            case LoginMode.SET_HASHTAG:
                return setHashtag();
            case LoginMode.DELETE_GROUP:
                return deleteGroup();
            case LoginMode.SET_IMAGE:
                return setImage();
            case LoginMode.CREATE_GROUP:
                return createGroup();
            case LoginMode.CHECK_GROUP:
                return checkGroup();
            case LoginMode.SET_GROUP_NAME:
                return setGroupName();
            case LoginMode.EDIT_POST:
                return editPost();
            case LoginMode.SEND_NOTIFICATION:
                return sendNotification();
            default:
                return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        switch (mode) {
            case LoginMode.POST:
                postComplete.onPostComplete(postID, hashtag);
                break;
            case LoginMode.COMMENT:
                commentComplete.onCommentCompleted(comments, what);
                break;
            case LoginMode.SEARCH:
                searchComplete.onSearchCompleted(groupSearches);
                break;
            case LoginMode.GET_USER_POSTS:
                myPostsComplete.onGetUserPostsCompleted(posts);
                break;
            case LoginMode.DELETE_POST:
                myPostsComplete.onDeleteUserPostCompleted();
                break;
            case LoginMode.GET_USER_GROUPS:
                myGroupsComplete.onGetUserGroupsCompleted(userGroups);
                break;
            case LoginMode.GET_POSTS:
                getPostsComplete.onGetPostsComplete(posts);
                break;
            case LoginMode.GET_GROUP:
                getGroupProfileComplete.onGetGroupComplete(groupProfile);
                break;
            case LoginMode.SET_IMAGE:
                imageSetComplete.onImageSetComplete();
                break;
            case LoginMode.CREATE_GROUP:
                createGroupComplete.onCreateGroupComplete(groupID);
                break;
            case LoginMode.CHECK_GROUP:
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
            params.add(new BasicNameValuePair("operation", "chat"));
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
                Favorite favorite = new Favorite();
                favorite.set_name(json.getJSONObject(String.valueOf(i)).getString(KEY_GROUP_NAME));
                favorite.setId(json.getJSONObject(String.valueOf(i)).getInt(KEY_GROUP_ID));
                favorite.setSubs(json.getJSONObject(String.valueOf(i)).getInt(KEY_SUBSCRIBERS));
                groupSearches.add(favorite);
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

            PostsDBHandler db = new PostsDBHandler(context);
            db.clearAll();

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
                db.createPost(myPost);
                posts.add(myPost);
            }
            Collections.reverse(posts);
            db.close();

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
                post.setUser_id(json.getJSONObject(String.valueOf(i)).getString(KEY_USER_ID));
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

            Log.d("request!", "starting");
            JSONObject json = jsonParser.makeHttpRequest(EndpointGroups.URL_GET_GROUP, "GET", params);
            groupProfile.setGroupName(json.getJSONObject(String.valueOf(0)).getString(KEY_GROUP_NAME));
            groupProfile.setSubscribers(json.getJSONObject(String.valueOf(0)).getInt(KEY_SUBSCRIBERS));
            groupProfile.setHashtag_name(json.getJSONObject(String.valueOf(0)).getString(KEY_GROUP_HASHTAG));
            groupProfile.setUser_id(json.getJSONObject(String.valueOf(0)).getString(KEY_USER_ID));
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

            if (!(hashtag == null)) {
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

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(EndpointGroups.URL_SET_GROUP_IMG);
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            httpResponse.getEntity();
            return "1";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String createGroup() {
        int success;
        try {
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("group_name", groupName));
            params.add(new BasicNameValuePair("subscribers", "1"));
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
                exists = json.getInt("exists") != 0;
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