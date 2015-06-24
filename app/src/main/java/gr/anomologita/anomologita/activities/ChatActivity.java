package gr.anomologita.anomologita.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.sql.Timestamp;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.adapters.ChatAdapter;
import gr.anomologita.anomologita.databases.ChatDBHandler;
import gr.anomologita.anomologita.databases.ConversationsDBHandler;
import gr.anomologita.anomologita.databases.PostsDBHandler;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.ChatMessage;
import gr.anomologita.anomologita.objects.Conversation;

public class ChatActivity extends ActionBarActivity {

    private final Handler handler = new Handler();

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private Conversation conversation;
    private EditText editText;
    private int conversationID;
    private String receiverName, receiverRegID, message, hashtag, postID;
    private RelativeLayout messageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_comment_layout);
        Anomologita.onChat = true;
        editText = (EditText) findViewById(R.id.editText);
        messageLayout = (RelativeLayout) findViewById(R.id.textField);
        ProgressWheel wheel = (ProgressWheel) findViewById(R.id.wheel);
        wheel.stopSpinning();

        conversation = Anomologita.conversation;
        Anomologita.conversation = null;
        receiverName = conversation.getName();
        if (Anomologita.regID.equals(conversation.getReceiverRegID()))
            receiverRegID = conversation.getSenderRegID();
        else
            receiverRegID = conversation.getReceiverRegID();
        postID = conversation.getPostID();
        conversationID = conversation.getConversationID();
        hashtag = conversation.getHashtag();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        PostsDBHandler dbPosts = new PostsDBHandler(this);
        if (dbPosts.exists(Integer.parseInt(postID)))
            toolbar.setTitle("Συνομιλία με " + receiverName);
        else
            toolbar.setTitle("Συνομιλία με Ανώνυμο");
        dbPosts.close();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView submit = (ImageView) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okClick();
            }
        });

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(100);
        animator.setRemoveDuration(100);


        ChatDBHandler dbChat = new ChatDBHandler(this);
        adapter = new ChatAdapter(this);
        adapter.setMainData(dbChat.getConversationMessages(conversationID));
        dbChat.close();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setItemAnimator(animator);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);

        handler.postDelayed(runnable, 500);
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(this, 500);
        }
    };

    void refresh() {
        ChatDBHandler db = new ChatDBHandler(this);
        int currentCount = adapter.getItemCount();
        int newCount = db.getConversationMessages(conversationID).size();
        if (newCount > currentCount) {
            for (int i = currentCount; i < newCount; i++) {
                adapter.addMessage(db.getConversationMessages(conversationID).get(i));
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        } else {
            adapter.notifyDataSetChanged();
        }
        db.close();
    }

    private void okClick() {
        message = editText.getText().toString();
        if (message.equals("")) {
            YoYo.with(Techniques.Tada).duration(700).playOn(messageLayout);
            Toast.makeText(this, "Είναι κενό!!!", Toast.LENGTH_SHORT).show();
        } else if (message.length() > 500) {
            YoYo.with(Techniques.Tada).duration(700).playOn(messageLayout);
            Toast.makeText(this, "Ξεπέρασες τους 500 χαρακτήρες", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("ok","ok");
            if (Anomologita.isConnected()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                editText.setText("");
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                AttemptLogin sendMessage = new AttemptLogin();
                sendMessage.sendMessage(Anomologita.regID, receiverRegID, receiverName, message, hashtag, String.valueOf(postID));
                sendMessage.execute();
                newChatMessage();
                updateConversation();
            } else {
                YoYo.with(Techniques.Tada).duration(700).playOn(editText);
                Toast.makeText(this, R.string.noInternet, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void newChatMessage() {
        ChatDBHandler db = new ChatDBHandler(this);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setTime((new Timestamp(System.currentTimeMillis())).toString());
        chatMessage.setMessage(message);
        chatMessage.setSenderID(Anomologita.userID);
        chatMessage.setConversationID(conversationID);
        db.createMessage(chatMessage);
        db.close();
    }

    private void updateConversation() {
        ConversationsDBHandler db = new ConversationsDBHandler(this);
        conversation.setLastSenderID(Anomologita.userID);
        conversation.setLastMessage(message);
        conversation.setTime((new Timestamp(System.currentTimeMillis())).toString());
        db.updateConversation(conversation);
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Anomologita.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Anomologita.activityPaused();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Anomologita.onChat = false;
        handler.removeCallbacks(runnable);
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}