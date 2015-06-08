package gr.anomologita.anomologita.network;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.sql.Timestamp;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.ChatActivity;
import gr.anomologita.anomologita.activities.MainActivity;
import gr.anomologita.anomologita.databases.ChatDBHandler;
import gr.anomologita.anomologita.databases.ConversationsDBHandler;
import gr.anomologita.anomologita.databases.NotificationDBHandler;
import gr.anomologita.anomologita.extras.Keys;
import gr.anomologita.anomologita.objects.ChatMessage;
import gr.anomologita.anomologita.objects.Conversation;
import gr.anomologita.anomologita.objects.Notification;
import gr.anomologita.anomologita.objects.Post;

public class GcmIntentService extends IntentService implements Keys.MyPostsComplete, Keys.LoginMode {
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "GcmIntentService";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (!TextUtils.isEmpty(messageType)) {
                switch (messageType) {
                    case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                        sendChatNotification("Send error: ",extras.toString());
                        break;
                    case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                        sendChatNotification("Deleted messages on server: ",extras.toString());
                        break;
                    case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                        if (extras.containsKey("operation")) {
                            if (extras.getString("operation").equals("chat"))
                                chat(extras);
                            else if (extras.getString("operation").equals("notification"))
                                if (Anomologita.isConnected())
                                   // new AttemptLogin(GET_USER_POSTS, this).execute();
                                notification(extras);
                            break;
                        }
                }
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void chat(Bundle extras) {
        ConversationsDBHandler dbCon = new ConversationsDBHandler(this);
        if (!Anomologita.onChat)
            Anomologita.setChatBadge();
        if (Anomologita.isActivityVisible())
            sendChatNotification(extras.getString("name"),extras.getString("message"));
        if (dbCon.exists(extras.getString("postID"))) {
            Conversation conversation = dbCon.getConversation(extras.getString("postID"));
            conversation.setLastMessage(extras.getString("message"));
            conversation.setName(extras.getString("name"));
            conversation.setHashtag(extras.getString("hashtag"));
            conversation.setSeen("no");
            conversation.setTime((new Timestamp(System.currentTimeMillis())).toString());
            conversation.setLastSenderID(extras.getString("lastSenderID"));
            dbCon.updateConversation(conversation);
        } else {
            Conversation conversation = new Conversation();
            conversation.setSeen("no");
            conversation.setHashtag(extras.getString("hashtag"));
            conversation.setLastMessage(extras.getString("message"));
            if (extras.getString("senderRegID").equals(Anomologita.regID)) {
                conversation.setSenderRegID(extras.getString("senderRegID"));
                conversation.setReceiverRegID(extras.getString("receiverRegID"));
            } else {
                conversation.setSenderRegID(extras.getString("receiverRegID"));
                conversation.setReceiverRegID(extras.getString("senderRegID"));
            }
            conversation.setName(extras.getString("name"));
            conversation.setPostID(extras.getString("postID"));
            conversation.setTime((new Timestamp(System.currentTimeMillis())).toString());
            dbCon.createConversation(conversation);
        }
        ChatDBHandler dbChat = new ChatDBHandler(this);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setTime((new Timestamp(System.currentTimeMillis())).toString());
        chatMessage.setMessage(extras.getString("message"));
        chatMessage.setSenderID(extras.getString("lastSenderID"));
        chatMessage.setConversationID(dbCon.getConversation(extras.getString("postID")).getConversationID());
        dbChat.createMessage(chatMessage);
        dbChat.close();
        dbCon.close();
    }

    private void notification(Bundle extras) {
        NotificationDBHandler db = new NotificationDBHandler(this);
        Notification notification = new Notification();
        notification.setTime((new Timestamp(System.currentTimeMillis())).toString());
        notification.setId(extras.getString("id"));
        notification.setType(extras.getString("type"));
        notification.setText(extras.getString("text"));
        if (Anomologita.isActivityVisible())
            sendNotNotification(extras.getString("text"));
        if (!db.exists(extras.getString("id"), extras.getString("type"))) {
            Anomologita.setNotificationBadge();
            db.createNotification(notification);
            db.close();
        } else {
            Anomologita.setNotificationBadge();
            db.updateNotification(notification);
            db.close();
        }
    }

    private void sendNotNotification(String text) {
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        android.app.Notification mNotify = new android.app.Notification.Builder(this)
                .setContentTitle("Ανομολόγητα")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_stat_a)
                .setContentIntent(pIntent)
                .setSound(sound)
                .build();
        NotificationManager mNM = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotify.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
        mNM.notify(0, mNotify);
    }

    private void sendChatNotification(String name,String msg) {
        if (Anomologita.isActivityVisible()) {
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ChatActivity.class), 0);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    this).setSmallIcon(R.drawable.ic_action_a)
                    .setContentTitle("Ανομολόγητα")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(name + ": "+ msg))
                    .setContentText(name + ": "+ msg);
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    @Override
    public void onGetUserPostsCompleted(List<Post> userPosts) {

    }

    @Override
    public void onDeleteUserPostCompleted() {

    }
}