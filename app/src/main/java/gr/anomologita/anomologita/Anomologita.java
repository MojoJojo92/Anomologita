package gr.anomologita.anomologita;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import gr.anomologita.anomologita.activities.MainActivity;
import gr.anomologita.anomologita.databases.FavotitesDBHandler;
import gr.anomologita.anomologita.extras.Keys.Preferences;
import gr.anomologita.anomologita.network.GCMRegister;
import gr.anomologita.anomologita.objects.Conversation;
import gr.anomologita.anomologita.objects.Post;

public class Anomologita extends Application implements Preferences {

    private static Anomologita sInstance;
    private static boolean activityVisible;
    public static Post currentPost;
    private static SharedPreferences SP;
    public static String userID;
    public static String regID;
    public static Conversation conversation;

    public void onCreate() {
        super.onCreate();
        sInstance = this;
        conversation = null;
        SP = PreferenceManager.getDefaultSharedPreferences(this);
        regID = new GCMRegister().registerGCM();
        getUserID();
    }

    public static Anomologita getsInstance() {
        return sInstance;
    }

    public static boolean isVisible() {
        return activityVisible;
    }

    public static void getUserID() {
        if (SP.contains(USER_ID))
            userID = SP.getString(USER_ID, null);
    }

    public static int getNotificationBadges() {
        if (SP.contains(NOTIFICATION_BADGES))
            return SP.getInt(NOTIFICATION_BADGES, 0);
        return 0;
    }

    public static int getChatBadges() {
        if (SP.contains(CHAT_BADGES))
            return SP.getInt(CHAT_BADGES, 0);
        return 0;
    }

    public static void setUserID(String id) {
        SharedPreferences.Editor prefsEditor = SP.edit();
        prefsEditor.putString(USER_ID, id);
        prefsEditor.apply();
        userID = id;
    }

    public static void setChatBadge() {
        SharedPreferences.Editor prefsEditor = SP.edit();
        prefsEditor.putInt(CHAT_BADGES, getChatBadges()+1);
        prefsEditor.apply();
    }

    public static void emptyChatBadges() {
        SharedPreferences.Editor prefsEditor = SP.edit();
        prefsEditor.putInt(CHAT_BADGES, 0);
        prefsEditor.apply();
    }

    public static void setNotificationBadge() {
        SharedPreferences.Editor prefsEditor = SP.edit();
        prefsEditor.putInt(NOTIFICATION_BADGES, getNotificationBadges()+1);
        prefsEditor.apply();
    }

    public static void emptyNotificationBadges() {
        SharedPreferences.Editor prefsEditor = SP.edit();
        prefsEditor.putInt(NOTIFICATION_BADGES, 0);
        prefsEditor.apply();
    }

    public static void StartMain(){
        Intent intent = new Intent(getAppContext(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getAppContext().startActivity(intent);
    }

    public static String getCurrentGroupID() {
        if (SP.contains(CURRENT_GROUP_ID))
            return SP.getString(CURRENT_GROUP_ID, null);
        else
            return null;
    }

    public static void setCurrentGroupID(String currentGroupID) {
        SharedPreferences.Editor prefsEditor = SP.edit();
        prefsEditor.putString(CURRENT_GROUP_ID, currentGroupID);
        prefsEditor.apply();
    }

    public static String getCurrentGroupName() {
        if (SP.contains(CURRENT_GROUP_NAME))
            return SP.getString(CURRENT_GROUP_NAME, null);
        else
            return null;
    }

    public static void setCurrentGroupName(String currentGroupName) {
        SharedPreferences.Editor prefsEditor = SP.edit();
        prefsEditor.putString(CURRENT_GROUP_NAME, currentGroupName);
        prefsEditor.apply();
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static Context getAppContext() {
        return sInstance.getApplicationContext();
    }

    public static boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) sInstance.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            return true;
        else
            return false;
    }

    public static void clearFavorites() {
        FavotitesDBHandler db = new FavotitesDBHandler(getAppContext());
        db.clearAll();
    }

    private void clearPreferences() {
        SharedPreferences.Editor prefsEditor = SP.edit();
        prefsEditor.clear();
        prefsEditor.apply();
    }

    public static int convert(int dp) {
        Resources r = getsInstance().getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
}
