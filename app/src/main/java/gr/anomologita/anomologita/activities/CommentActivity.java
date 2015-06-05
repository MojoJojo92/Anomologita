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
                if (post.getUser_id() != Integer.parseInt(Anomologita.userID)) {
                    String text = "To post " + post.getHashtagName() + " έχει " + (adapter.getItemCount()) + " σχόλια";
                    new AttemptLogin(SEND_NOTIFICATION, text, "comment", String.valueOf(post.getPost_id()), post.getReg_id()).execute();
                }
            } else {
                YoYo.with(Techniques.Tada).duration(700).playOn(layout);
                Toast.makeText(Anomologita.getAppContext(), "ΔΕΝ ΥΠΑΡΧΕΙ ΣΙΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
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
