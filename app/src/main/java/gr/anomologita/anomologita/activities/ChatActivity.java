package gr.anomologita.anomologita.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.ChatMessage;
import gr.anomologita.anomologita.objects.Conversation;

public class ChatActivity extends ActionBarActivity implements LoginMode {

    final Handler handler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refresh();
            handler.postDelayed(this, 500);
        }
    };
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private Conversation conversation;
    private EditText editText;
    private ChatDBHandler db;
    private int conversationID;
    private String receiverName, receiverRegID, message, hashtag, postID;
    private RelativeLayout layout, messageLayout;
    private Boolean connection = true;
    private ProgressWheel wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_comment_layout);
        layout = (RelativeLayout) findViewById(R.id.chatCommentLayout);
        editText = (EditText) findViewById(R.id.editText);
        messageLayout = (RelativeLayout) findViewById(R.id.textField);
        wheel = (ProgressWheel) findViewById(R.id.wheel);
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
        if (receiverName.equals("Εγώ"))
            toolbar.setTitle("Συνομιλία με Ανώνυμο");
        else
            toolbar.setTitle("Συνομιλία με " + receiverName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(100);
        animator.setRemoveDuration(100);

        db = new ChatDBHandler(this);
        adapter = new ChatAdapter(this);
        adapter.setMainData(db.getConversationMessages(conversationID));

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setItemAnimator(animator);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);

        handler.postDelayed(runnable, 500);
    }

    public void refresh() {
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
    }

    public void chatOk(View view) {
        message = editText.getText().toString();
        if (message.equals("")) {
            YoYo.with(Techniques.Tada).duration(700).playOn(messageLayout);
            Toast.makeText(this, "Είναι κενό!!!", Toast.LENGTH_SHORT).show();
        } else if (message.length() > 100) {
            YoYo.with(Techniques.Tada).duration(700).playOn(messageLayout);
            Toast.makeText(this, "Ξεπέρασες τους 100 χαρακτήρες", Toast.LENGTH_SHORT).show();
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            editText.setText("");
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            if (Anomologita.isConnected()) {
                new AttemptLogin(PERSONAL_MESSAGE, Anomologita.regID, receiverRegID, receiverName, message, hashtag, "chat", String.valueOf(postID)).execute();
                newChatMessage();
                updateConversation();
                connection = true;
            } else if (connection) {
                YoYo.with(Techniques.Tada).duration(700).playOn(layout);
                Toast.makeText(this, "ΔΕΝ ΥΠΑΡΧΕΙ ΣΙΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
                connection = false;
            }
        }
    }

    private void newChatMessage() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setTime((new Timestamp(System.currentTimeMillis())).toString());
        chatMessage.setMessage(message);
        chatMessage.setSenderID(Anomologita.userID);
        chatMessage.setConversationID(conversationID);
        db.createMessage(chatMessage);
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
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, ConversationsActivity.class);
        startActivity(i);
        db.close();
        handler.removeCallbacks(runnable);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        finish();
    }
}