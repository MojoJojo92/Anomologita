package gr.anomologita.anomologita.network;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.ChatActivity;
import gr.anomologita.anomologita.databases.ChatDBHandler;
import gr.anomologita.anomologita.databases.ConversationsDBHandler;
import gr.anomologita.anomologita.databases.NotificationDBHandler;
import gr.anomologita.anomologita.extras.NotificationHandler;
import gr.anomologita.anomologita.objects.ChatMessage;
import gr.anomologita.anomologita.objects.Conversation;
import gr.anomologita.anomologita.objects.Notification;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.sql.Timestamp;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    public static final String TAG = "GcmIntentService";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if(!TextUtils.isEmpty(messageType)){
                switch (messageType) {
                    case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                        sendNotification("Send error: " + extras.toString());
                        break;
                    case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                        sendNotification("Deleted messages on server: " + extras.toString());
                        break;
                    case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                        if(extras.containsKey("operation")){
                            if (extras.getString("operation").equals("chat")) {
                                chat(extras);
                            }else if(extras.getString("operation").equals("notification")){
                                notification(extras);
                            } else {
                                sendNotification("Received Message : " + extras.getString("message"));
                                Log.i(TAG, "Received: " + extras.toString());
                            }
                            break;
                        }
                }
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);


    }

    private void sendNotification(String msg) {

        if (new NotificationHandler().isOn()) {


        } else {
            mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ChatActivity.class), 0);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    this).setSmallIcon(R.drawable.ic_action_a)
                    .setContentTitle("GCM Notification")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                    .setContentText(msg);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
    }

    private void chat(Bundle extras) {
        ConversationsDBHandler dbCon = new ConversationsDBHandler(this);
        if (dbCon.exists(extras.getString("postID"))) {
            Conversation conversation = dbCon.getConversation(extras.getString("postID"));
            conversation.setLastMessage(extras.getString("message"));
            conversation.setName(extras.getString("name"));
            conversation.setTime((new Timestamp(System.currentTimeMillis())).toString());
            conversation.setLastSenderID(extras.getString("lastSenderID"));
            dbCon.updateConversation(conversation);
            if (!new NotificationHandler().isChatOn(this))
                Anomologita.setChatBadge();
            if (!new NotificationHandler().isOn())
                sendNotification(extras.getString("message"));
        } else {
            Conversation conversation = new Conversation();
            conversation.setSeen("no");
            conversation.setHashtag(extras.getString("hashtag"));
            conversation.setLastMessage(extras.getString("message"));
            Log.e("sender",extras.getString("senderRegID"));
            Log.e("res",extras.getString("receiverRegID"));
            if(extras.getString("senderRegID").equals(Anomologita.regID)){
                conversation.setSenderRegID(extras.getString("senderRegID"));
                conversation.setReceiverRegID(extras.getString("receiverRegID"));
            }else {
                conversation.setSenderRegID(extras.getString("receiverRegID"));
                conversation.setReceiverRegID(extras.getString("senderRegID"));
            }
            conversation.setName(extras.getString("name"));
            conversation.setPostID(extras.getString("postID"));
            conversation.setTime((new Timestamp(System.currentTimeMillis())).toString());
            dbCon.createConversation(conversation);
            Anomologita.setNotificationBadge();
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

    private void notification(Bundle extras){
        NotificationDBHandler db = new NotificationDBHandler(this);
        Notification notification = new Notification();
        notification.setTime((new Timestamp(System.currentTimeMillis())).toString());
        notification.setId(extras.getString("id"));
        notification.setType(extras.getString("type"));
        notification.setText(extras.getString("text"));
        db.createNotification(notification);
        db.close();
    }
}