package gr.anomologita.anomologita;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.TypedValue;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import gr.anomologita.anomologita.activities.MainActivity;
import gr.anomologita.anomologita.extras.Keys.Preferences;
import gr.anomologita.anomologita.fragments.MainFragment;
import gr.anomologita.anomologita.network.GCMRegister;
import gr.anomologita.anomologita.objects.Conversation;
import gr.anomologita.anomologita.objects.Post;

public class Anomologita extends Application implements Preferences {

    public static Post currentPost;
    public static String userID = null, regID = null;
    public static Conversation conversation;
    public static boolean refresh = false, onChat = false;
    public static MainFragment fragmentNew = null, fragmentTop = null;
    private static SharedPreferences SP;
    private static Anomologita sInstance;
    private static boolean activityVisible;

    public void onCreate() {
        super.onCreate();
        sInstance = this;
        userID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID).trim();
        conversation = null;
        SP = PreferenceManager.getDefaultSharedPreferences(this);
        regID = new GCMRegister().registerGCM();
        getUserID();
    }

    public static Anomologita getsInstance() {
        return sInstance;
    }

    public static boolean isActivityVisible() {
        return !activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static void getUserID() {
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

    public static void setChatBadge() {
        SharedPreferences.Editor prefsEditor = SP.edit();
        prefsEditor.putInt(CHAT_BADGES, getChatBadges() + 1);
        prefsEditor.apply();
    }

    public static void emptyChatBadges() {
        SharedPreferences.Editor prefsEditor = SP.edit();
        prefsEditor.putInt(CHAT_BADGES, 0);
        prefsEditor.apply();
    }

    public static void setNotificationBadge() {
        SharedPreferences.Editor prefsEditor = SP.edit();
        prefsEditor.putInt(NOTIFICATION_BADGES, getNotificationBadges() + 1);
        prefsEditor.apply();
    }

    public static void emptyNotificationBadges() {
        SharedPreferences.Editor prefsEditor = SP.edit();
        prefsEditor.putInt(NOTIFICATION_BADGES, 0);
        prefsEditor.apply();
    }

    public static void StartMain() {
        Intent intent = new Intent(getAppContext(), MainActivity.class);
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

    public static Context getAppContext() {
        return sInstance.getApplicationContext();
    }

    public static boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) sInstance.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    public static int convert(int dp) {
        Resources r = getsInstance().getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static String getTime(String time, int offset) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        try {
            Timestamp t2 = new Timestamp(System.currentTimeMillis() + offset * 1000 * 60);
            Date postDate = dateFormat.parse(time);
            Date currentDate = dateFormat.parse(t2.toString());
            int months = currentDate.getMonth() - postDate.getMonth();
            int days = currentDate.getDay() - postDate.getDay();
            int hours = currentDate.getHours() - postDate.getHours();
            int minutes = currentDate.getMinutes() - postDate.getMinutes();
            if (months != 0) {
                return (postDate.getDate() + "/" + postDate.getMonth() + "/" + (postDate.getYear() - 100));
            } else if (days > 0) {
                if (days == 1)
                    return ("Χθές");
                else
                    return (postDate.getDate() + "/" + postDate.getMonth() + "/" + (postDate.getYear() - 100));
            } else if (hours > 0) {
                if (hours == 1)
                    return ("1 hr");
                else
                    return (hours + " hrs");
            } else if (minutes > 0) {
                return (minutes + " min");
            } else {
                return "τώρα";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "τώρα";
    }
}
