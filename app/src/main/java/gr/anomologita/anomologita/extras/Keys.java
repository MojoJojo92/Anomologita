package gr.anomologita.anomologita.extras;

import gr.anomologita.anomologita.objects.Comment;
import gr.anomologita.anomologita.objects.Favorite;
import gr.anomologita.anomologita.objects.GroupProfile;
import gr.anomologita.anomologita.objects.Post;

import java.util.List;

public interface Keys {
    public interface EndpointGroups {

        public static final String URL_GET_GROUP = "http://anomologita.gr/getGroup.php";
        public static final String URL_GET_POSTS = "http://anomologita.gr/getPosts.php";
        public static final String URL_GET_COMMENTS = "http://anomologita.gr/getComments.php";
        public static final String URL_GET_USER_POSTS = "http://anomologita.gr/getUserPosts.php";
        public static final String URL_GET_USER_GROUPS = "http://anomologita.gr/getUserGroups.php";
        public static final String URL_SET_LIKE = "http://anomologita.gr/setLike.php";
        public static final String URL_SET_SUBSCRIBERS = "http://anomologita.gr/setSubscribers.php";
        public static final String URL_SET_GROUP = "http://anomologita.gr/setGroup.php";
        public static final String URL_SET_USER = "http://anomologita.gr/setUser.php";
        public static final String URL_POST = "http://anomologita.gr/post.php";
        public static final String URL_SEARCH = "http://anomologita.gr/search.php";
        public static final String URL_COMMENT = "http://anomologita.gr/comment.php";
        public static final String URL_SET_GROUP_IMG = "http://anomologita.gr/setGroupImage.php";
        public static final String URL_EDIT_HASHTAG = "http://anomologita.gr/editHashtag.php";
        public static final String URL_DELETE_POST = "http://anomologita.gr/deletePost.php";
        public static final String URL_DELETE_GROUP = "http://anomologita.gr/deleteGroup.php";
        public static final String URL_CHECK_GROUP = "http://anomologita.gr/checkGroup.php";
        public static final String URL_SET_GROUP_NAME = "http://anomologita.gr/editGroupName.php";
        public static final String URL_EDIT_POST = "http://anomologita.gr/editPost.php";
        public static final String URL_SEND_MESSAGE = "http://anomologita.gr/GCM.php";
        public static final String URL_SEND_NOTIFICATION = "http://anomologita.gr/gcmLike.php";

        public static final String ACTION_BUTTON_TAG = "post";
        public static final String TAG_SUCCESS = "success";
        public static final String TAG_MESSAGE = "message";

        public static final int NEW = 0;
        public static final int TOP = 1;

        public static final int MY_GROUPS = 0;
        public static final int MY_POSTS = 1;

    }

    public interface InputValues {

        public static final String KEY_POST_ID = "id_post";
        public static final String KEY_GROUP_NAME = "group_name";
        public static final String KEY_SUBSCRIBERS = "subscribers";
        public static final String KEY_GROUP_HASHTAG = "hashtag_name";
        public static final String KEY_USER_ID = "user_id";
        public static final String KEY_HASHTAG = "hashtag";
        public static final String KEY_POST = "anomologito";
        public static final String KEY_LOCATION = "location";
        public static final String KEY_RATING = "rating";
        public static final String KEY_COMMENT_COUNT = "comment_count";
        public static final String KEY_DATE = "date";
        public static final String KEY_GROUP_ID = "group_id";
        public static final String KEY_REG_ID = "reg_id";

    }

    public interface LoginMode {

        public static final int SET_SUBSCRIBERS = 0;
        public static final int POST = 2;
        public static final int COMMENT = 5;
        public static final int PERSONAL_MESSAGE = 9;
        public static final int SEARCH = 10;
        public static final int GET_USER_POSTS = 11;
        public static final int DELETE_POST = 12;
        public static final int GET_USER_GROUPS = 13;
        public static final int GET_POSTS = 14;
        public static final int GET_GROUP = 15;
        public static final int SET_LIKE = 16;
        public static final int SET_HASHTAG = 17;
        public static final int DELETE_GROUP = 18;
        public static final int SET_IMAGE = 19;
        public static final int CREATE_GROUP = 21;
        public static final int CHECK_GROUP = 23;
        public static final int SET_GROUP_NAME = 24;
        public static final int EDIT_POST = 26;
        public static final int SEND_NOTIFICATION = 28;

    }

    public interface Preferences {
        public static final String USER_ID = "userID";
        public static final String CURRENT_GROUP_NAME = "currentGroupName";
        public static final String CURRENT_GROUP_ID = "currentGroupID";
        public static final String CHAT_BADGES = "messages";
        public static final String NOTIFICATION_BADGES = "notifications";
        public static final String NOTIFICATIONS = "not";

    }

    public interface PostComplete {
        void onPostComplete(String postID, String hashtag);
    }

    public interface CommentComplete {
        void onCommentCompleted(List<Comment> comments, String what);
    }

    public interface SearchComplete {
        void onSearchCompleted(List<Favorite> groupSearches);
    }

    public interface MyPostsComplete {
        void onGetUserPostsCompleted(List<Post> userPosts);
        void onDeleteUserPostCompleted();
    }

    public interface MyGroupsComplete {
        void onGetUserGroupsCompleted(List<GroupProfile> userGroups);
    }

    public interface GetGroupProfileComplete {
        void onGetGroupComplete(GroupProfile groupProfile);
    }

    public interface GetPostsComplete {
        void onGetPostsComplete(List<Post> posts);
    }

    public interface ImageSetComplete {
        void onImageSetComplete();
    }

    public interface CreateGroupComplete {
        void onCreateGroupComplete(String groupID);
    }

    public interface CheckGroupComplete {
        void onCheckGroupComplete(Boolean exists);
    }
}
