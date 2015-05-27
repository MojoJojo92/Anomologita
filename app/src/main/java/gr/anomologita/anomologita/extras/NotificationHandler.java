package gr.anomologita.anomologita.extras;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.ChatActivity;
import gr.anomologita.anomologita.activities.MainActivity;

import java.util.List;

public class NotificationHandler {

    public NotificationHandler() {
    }

    public void chatNotification(Context context){
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(context, ChatActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context,0,intent,0);
        android.app.Notification mNotify = new android.app.Notification.Builder(context)
                .setContentTitle("Ανομολόγητα")
                .setContentText("Νέο μήνυμα")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setSound(sound)
                //.addAction(0,"Nothing",pIntent)
                .build();
        NotificationManager mNM = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotify.flags |= Notification.FLAG_AUTO_CANCEL;
        mNM.notify(0,mNotify);

    }

    public void commentNotification(Context context, String hashtag){

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context,0,intent,0);
        android.app.Notification mNotify = new android.app.Notification.Builder(context)
                .setContentTitle("Ανομολόγητα")
                .setContentText("Το πόστ " + hashtag + " έχει νέα σχόλια")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setSound(sound)
                        //.addAction(0,"Nothing",pIntent)
                .build();
        NotificationManager mNM = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotify.flags |= Notification.FLAG_AUTO_CANCEL;
        mNM.notify(0,mNotify);
    }

    public void likeNotification(Context context, String hashtag, String count){
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context,0,intent,0);
        android.app.Notification mNotify = new android.app.Notification.Builder(context)
                .setContentTitle("Ανομολόγητα")
                .setContentText("Το πόστ " + hashtag+ " αρέσει σε " + count + " άτομα")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setSound(sound)
                        //.addAction(0,"Nothing",pIntent)
                .build();
        NotificationManager mNM = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotify.flags |= Notification.FLAG_AUTO_CANCEL;
        mNM.notify(0,mNotify);
    }

    public void subscribedNotification(Context context, String groupName, int subCount){
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context,0,intent,0);
        android.app.Notification mNotify = new android.app.Notification.Builder(context)
                .setContentTitle("Ανομολόγητα")
                .setContentText("Το γρούπ "+ groupName+" έχει "+ subCount+" ακόλουθους" )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setSound(sound)
                        //.addAction(0,"Nothing",pIntent)
                .build();
        NotificationManager mNM = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotify.flags |= Notification.FLAG_AUTO_CANCEL;
        mNM.notify(0,mNotify);
    }

    public Boolean isOn() {
        ActivityManager activityManager = (ActivityManager) Anomologita.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(1);
        boolean isActivityFound = false;
        if (services.get(0).topActivity.getPackageName().equalsIgnoreCase(Anomologita.getAppContext().getPackageName())) {
            isActivityFound = true;
        }
        return isActivityFound;
    }

    public Boolean isChatOn(Context context) {
        ActivityManager activityManager = (ActivityManager) Anomologita.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(1);
        boolean isActivityFound = false;
        if (services.contains(context)) {
            isActivityFound = true;
        }
        return isActivityFound;
    }
}
