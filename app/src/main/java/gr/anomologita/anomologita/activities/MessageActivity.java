package gr.anomologita.anomologita.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.sql.Timestamp;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.databases.ChatDBHandler;
import gr.anomologita.anomologita.databases.ConversationsDBHandler;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.ChatMessage;
import gr.anomologita.anomologita.objects.Conversation;

public class MessageActivity extends ActionBarActivity implements LoginMode {

    private String hashtag, message, regID, postID, name;
    private EditText personalMessageET, nameET;
    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            hashtag = extras.getString("hashtag");
            regID = extras.getString("regID");
            postID = extras.getString("postID");
        }
        setContentView(R.layout.edit_post_layout);
        layout = (RelativeLayout) findViewById(R.id.editPostLayout);

        personalMessageET = (EditText) findViewById(R.id.currentPost);
        personalMessageET.setHint("Γράψε το μήνυμά σου...");
        nameET = (EditText) findViewById(R.id.currentLocation);
        nameET.setHint("Γράψε όνομα ή ψευδώνυμο!");

        Toolbar toolbar = (Toolbar) findViewById(R.id.editPostToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        message = personalMessageET.getText().toString();
        name = nameET.getText().toString();
        int id = item.getItemId();
        if (id == R.id.createPost) {
            if (message.equals("")) {
                YoYo.with(Techniques.Tada).duration(700).playOn(personalMessageET);
                Toast.makeText(this, "Το μήνυμα είναι κενό!!!", Toast.LENGTH_SHORT).show();
            } else if (name.equals("")) {
                YoYo.with(Techniques.Tada).duration(700).playOn(nameET);
                Toast.makeText(this, "Δώσε ένα όνομα ή ψευδώνμο", Toast.LENGTH_SHORT).show();
            } else if (message.length() > 200) {
                YoYo.with(Techniques.Tada).duration(700).playOn(personalMessageET);
                Toast.makeText(this, "Το μήνυμα ξεπερνά τους 200 χαρακτήρες!!!", Toast.LENGTH_SHORT).show();
            } else if (name.length() > 20) {
                YoYo.with(Techniques.Tada).duration(700).playOn(nameET);
                Toast.makeText(this, "Το όνομα ξεπερνά τους 20 χαρακτήρες", Toast.LENGTH_SHORT).show();
            } else {
                if (Anomologita.isConnected()) {
                    AttemptLogin sendMessage = new AttemptLogin();
                    sendMessage.sendMessage(Anomologita.regID, regID, name, message, hashtag, postID);
                    sendMessage.execute();
                    createConversation();
                    Toast.makeText(this, "Το μήνυμα εστάλη", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else {
                    YoYo.with(Techniques.Tada).duration(700).playOn(layout);
                    Toast.makeText(Anomologita.getAppContext(), R.string.noInternet, Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void newChatMessage() {
        ConversationsDBHandler dbCon = new ConversationsDBHandler(this);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationID(dbCon.getConversation(postID).getConversationID());
        chatMessage.setTime(dbCon.getConversation(postID).getTime());
        chatMessage.setMessage(message);
        chatMessage.setSenderID(Anomologita.userID);
        ChatDBHandler dbChat = new ChatDBHandler(this);
        dbChat.createMessage(chatMessage);
        dbChat.close();
        dbCon.close();
    }

    private void createConversation() {
        Conversation conversation = new Conversation();
        conversation.setTime((new Timestamp(System.currentTimeMillis())).toString());
        conversation.setName(name);
        conversation.setSeen("yes");
        conversation.setPostID(postID);
        conversation.setLastMessage(message);
        conversation.setHashtag(hashtag);
        conversation.setReceiverRegID(regID);
        conversation.setSenderRegID(Anomologita.regID);
        ConversationsDBHandler dbCon = new ConversationsDBHandler(this);
        dbCon.createConversation(conversation);
        dbCon.close();
        newChatMessage();
    }

    @Override
    public void onBackPressed() {
        HidingGroupProfileListener.mGroupProfileOffset = 0;
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
