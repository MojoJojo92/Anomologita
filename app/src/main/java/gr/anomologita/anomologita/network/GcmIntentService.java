package gr.anomologita.anomologita.network;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.sql.Timestamp;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.MainActivity;
import gr.anomologita.anomologita.databases.ChatDBHandler;
import gr.anomologita.anomologita.databases.ConversationsDBHandler;
import gr.anomologita.anomologita.databases.NotificationDBHandler;
import gr.anomologita.anomologita.databases.PostsDBHandler;
import gr.anomologita.anomologita.extras.Keys;
import gr.anomologita.anomologita.objects.ChatMessage;
import gr.anomologita.anomologita.objects.Conversation;
import gr.anomologita.anomologita.objects.Notification;
import gr.anomologita.anomologita.objects.Post;

public class GcmIntentService extends IntentService implements Keys.MyPostsComplete, Keys.LoginMode {

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
                        sendChatNotification("Send error: ", extras.toString());
                        break;
                    case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                        sendChatNotification("Deleted messages on server: ", extras.toString());
                        break;
                    case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                        if (extras.containsKey("operation")) {
                            if (extras.getString("operation").equals("chat")) {
                                chat(extras);
                            } else if (extras.getString("operation").equals("notification")) {
                                if (Anomologita.isConnected()) {
                                    AttemptLogin attemptLogin = new AttemptLogin();
                                    attemptLogin.getUserPosts(this);
                                    attemptLogin.execute();
                                }else {
                                    Toast.makeText(this, R.string.noInternet, Toast.LENGTH_SHORT).show();
                                }
                                notification(extras);
                            }
                            break;
                        } else {
                            sendAnnouncement(extras.getString("Notice"));
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
            sendChatNotification(extras.getString("name"), extras.getString("message"));

        String sender, receiver;
        PostsDBHandler db = new PostsDBHandler(this);
        if (db.exists(Integer.parseInt(extras.getString("postID")))) {
            sender = extras.getString("senderRegID");
            receiver = extras.getString("receiverRegID");
        } else {
            sender = extras.getString("receiverRegID");
            receiver = extras.getString("senderRegID");
        }
        db.close();

        if (dbCon.exists(extras.getString("postID"), sender, receiver)) {
            Conversation conversation = dbCon.getConversation(extras.getString("postID"), sender, receiver);
            conversation.setLastMessage(extras.getString("message"));
            conversation.setName(extras.getString("name"));
            conversation.setHashtag(extras.getString("hashtag"));
            conversation.setTime((new Timestamp(System.currentTimeMillis())).toString());
            conversation.setLastSenderID(extras.getString("lastSenderID"));
            if (Anomologita.onChat)
                conversation.setSeen("yes");
            else
                conversation.setSeen("no");
            dbCon.updateConversation(conversation);
        } else {
            Conversation conversation = new Conversation();
            conversation.setSeen("no");
            conversation.setHashtag(extras.getString("hashtag"));
            conversation.setLastSenderID(extras.getString("lastSenderID"));
            conversation.setLastMessage(extras.getString("message"));
            conversation.setSenderRegID(extras.getString("senderRegID"));
            conversation.setReceiverRegID(extras.getString("receiverRegID"));
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
        chatMessage.setConversationID(dbCon.getConversation(extras.getString("postID"), sender, receiver).getConversationID());
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
        if (Anomologita.isConnected() && Anomologita.areNotificationsOn()) {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
            android.app.Notification mNotify = new android.app.Notification.Builder(this)
                    .setContentTitle("Ανομολόγητα")
                    .setContentText(text)
                    .setSmallIcon(R.drawable.ic_stat_a)
                    .setContentIntent(pIntent)
                    .build();
            if (Anomologita.isNotificationSoundOn())
                mNotify.sound = sound;
            NotificationManager mNM = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotify.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
            mNM.notify(0, mNotify);
        }
    }

    private void sendChatNotification(String name, String msg) {
        if (Anomologita.isConnected() && Anomologita.areNotificationsOn()) {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
            android.app.Notification mNotify = new android.app.Notification.Builder(this)
                    .setContentTitle("Ανομολόγητα")
                    .setContentText(name + ": " + msg)
                    .setSmallIcon(R.drawable.ic_stat_a)
                    .setContentIntent(pIntent)
                    .build();
            if (Anomologita.isNotificationSoundOn())
                mNotify.sound = sound;
            NotificationManager mNM = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotify.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
            mNM.notify(0, mNotify);
        }
    }

    private void sendAnnouncement(String msg) {
        if (Anomologita.isConnected()) {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
            android.app.Notification mNotify = new android.app.Notification.Builder(this)
                    .setContentTitle("Ανομολόγητα")
                    .setContentText(msg)
                    .setSmallIcon(R.drawable.ic_stat_a)
                    .setContentIntent(pIntent)
                    .build();
            if (Anomologita.isNotificationSoundOn())
                mNotify.sound = sound;
            NotificationManager mNM = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotify.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
            mNM.notify(0, mNotify);
        }
    }

    @Override
    public void onGetUserPostsCompleted(List<Post> userPosts) {

    }

    @Override
    public void onDeleteUserPostCompleted() {

    }
}