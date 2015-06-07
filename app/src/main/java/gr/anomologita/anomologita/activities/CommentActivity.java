package gr.anomologita.anomologita.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.adapters.CommentAdapter;
import gr.anomologita.anomologita.databases.ConversationsDBHandler;
import gr.anomologita.anomologita.extras.Keys.CommentComplete;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.Comment;
import gr.anomologita.anomologita.objects.Post;

public class CommentActivity extends ActionBarActivity implements CommentComplete, LoginMode {

    private CommentAdapter adapter;
    private RelativeLayout layout;
    private RecyclerView recyclerView;
    private Post post;
    private ProgressWheel wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_comment_layout);
        layout = (RelativeLayout) findViewById(R.id.chatCommentLayout);
        post = Anomologita.currentPost;
        wheel = (ProgressWheel) findViewById(R.id.wheel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        adapter = new CommentAdapter(this);
        adapter.setPost(post);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        wheel.spin();
        getComments();
    }

    public void editPost(Post post) {
        Intent i = new Intent(this, EditPostActivity.class);
        i.putExtra("post", post.getPost_txt());
        i.putExtra("location", post.getLocation());
        startActivityForResult(i, 3);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void newMessage(Post post) {
        ConversationsDBHandler db = new ConversationsDBHandler(this);
        String postID = String.valueOf(post.getPost_id());
        if (db.exists(postID)) {
            Intent i = new Intent(this, ChatActivity.class);
            Anomologita.conversation = db.getConversation(postID);
            startActivity(i);
        } else {
            Intent i = new Intent(this, MessageActivity.class);
            i.putExtra("hashtag", post.getHashtagName());
            i.putExtra("userID", post.getUser_id());
            i.putExtra("regID", post.getReg_id());
            i.putExtra("postID", String.valueOf(post.getPost_id()));
            startActivity(i);
        }
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        finish();
    }


    void getComments() {
        if (Anomologita.isConnected()) {
            new AttemptLogin(COMMENT, String.valueOf(post.getPost_id()), null, "getComments", this).execute();
        } else {
            YoYo.with(Techniques.Tada).duration(700).playOn(layout);
            Toast.makeText(Anomologita.getAppContext(), "ΔΕΝ ΥΠΑΡΧΕΙ ΣΙΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCommentCompleted(List<Comment> comments, String what) {
        if (what.equals("getComments")) {
            adapter.setComments(comments);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            wheel.stopSpinning();
        } else {
            if (Anomologita.isConnected())
                new AttemptLogin(COMMENT, String.valueOf(post.getPost_id()), null, "getComments", this).execute();
        }
    }

    public void setLike(String like, Post post) {
        if (Anomologita.isConnected()) {
            String text;
            new AttemptLogin(SET_LIKE, String.valueOf(post.getPost_id()), like).execute();
            if (!post.getUser_id().equals(Anomologita.userID)) {
                int likes = post.getLikes() + Integer.parseInt(like);
                if (likes == 1)
                    text = "Το ανομολόγητο σου " + post.getHashtagName() + "\nπλέον  αρέσει σε " + likes + " άτομο";
                else
                    text = "Το ανομολόγητο σου " + post.getHashtagName() + "\nπλέον  αρέσει σε " + likes + " άτομα";
                new AttemptLogin(SEND_NOTIFICATION, text, "like", String.valueOf(post.getPost_id()), post.getReg_id()).execute();
            }
        }
    }

    public void okClick(View view) {
        view.clearFocus();
        EditText commentET = (EditText) findViewById(R.id.editText);
        String comment = commentET.getText().toString();
        if (comment.equals("")) {
            YoYo.with(Techniques.Tada).duration(700).playOn(layout);
            Toast.makeText(Anomologita.getAppContext(), "Κενό Σχόλιο", Toast.LENGTH_SHORT).show();
        } else {
            if (Anomologita.isConnected()) {
                wheel.spin();
                new AttemptLogin(COMMENT, String.valueOf(post.getPost_id()), comment, "setComment", this).execute();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(commentET.getWindowToken(), 0);
                commentET.setText("");
                if (!post.getUser_id().equals(Anomologita.userID)) {
                    String text = "To ανομολόγητο σου " + post.getHashtagName() + " έχει νέα σχόλια";
                    new AttemptLogin(SEND_NOTIFICATION, text, "comment", String.valueOf(post.getPost_id()), post.getReg_id()).execute();
                }
            } else {
                YoYo.with(Techniques.Tada).duration(700).playOn(layout);
                Toast.makeText(Anomologita.getAppContext(), "ΔΕΝ ΥΠΑΡΧΕΙ ΣΙΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    String text = data.getExtras().getString("text");
                    adapter.setPost(text);
                    break;
                case Activity.RESULT_CANCELED:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
