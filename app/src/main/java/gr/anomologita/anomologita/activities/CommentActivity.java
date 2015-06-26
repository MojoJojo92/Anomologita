package gr.anomologita.anomologita.activities;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
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

    private final Handler handler = new Handler();

    private CommentAdapter adapter;
    private RecyclerView recyclerView;
    private Post post;
    private ProgressWheel wheel;
    private EditText commentET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_comment_layout);
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

        adapter = new CommentAdapter(this);
        adapter.setPost(post);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        wheel.spin();
        handler.postDelayed(runnable, 500);
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getComments();
            handler.postDelayed(this, 500);
        }
    };

    void getComments() {
        if (Anomologita.isConnected()) {
            AttemptLogin setComment = new AttemptLogin();
            setComment.setComment(String.valueOf(post.getPost_id()), null, "getComments", this);
            setComment.execute();
        } else {
            Toast.makeText(Anomologita.getAppContext(),R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCommentCompleted(List<Comment> comments, String what) {
        if (what.equals("getComments")) {
            adapter.setComments(comments);
            wheel.stopSpinning();
            if (comments.size() > adapter.getItemCount()) {
                for (int i = comments.size(); i < comments.size(); i++) {
                    adapter.addComment(comments.get(i));
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            } else {
                adapter.notifyDataSetChanged();
            }
        } else {
            if (Anomologita.isConnected()){
                AttemptLogin setComment = new AttemptLogin();
                setComment.setComment(String.valueOf(post.getPost_id()), null, "getComments", this);
                setComment.execute();
            }else {
                Toast.makeText(Anomologita.getAppContext(),R.string.noInternet, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setLike(String like, Post post) {
        if (Anomologita.isConnected()) {
            String text;
            AttemptLogin setLike = new AttemptLogin();
            setLike.setLike(String.valueOf(post.getPost_id()), like);
            setLike.execute();
            if (!post.getUser_id().equals(Anomologita.userID)) {
                int likes = post.getLikes() + Integer.parseInt(like);
                if (likes == 1)
                    text = "Το ανομολόγητο σου " + post.getHashtagName() + "\nπλέον  αρέσει σε " + likes + " άτομο";
                else
                    text = "Το ανομολόγητο σου " + post.getHashtagName() + "\nπλέον  αρέσει σε " + likes + " άτομα";
                AttemptLogin sendNotification = new AttemptLogin();
                sendNotification.sendNotification(text, "like", String.valueOf(post.getPost_id()), post.getReg_id());
                sendNotification.execute();
            }
        }else {
            Toast.makeText(Anomologita.getAppContext(),R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
    }

    public void commentDialog(final Comment comment){
        boolean wrapInScrollView = true;
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Επεξεργασία")
                .customView(R.layout.comment_dialog_layout, wrapInScrollView)
                .iconRes(R.drawable.ic_setting_light)
                .positiveText("OK")
                .neutralText("ΔΙΑΓΡΑΦΗ")
                .positiveColorRes(R.color.accentColor)
                .neutralColorRes(R.color.primaryColor)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        editComment(comment);
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        super.onNeutral(dialog);
                        deleteComment(comment);
                    }
                })
                .show();
        commentET = (EditText) dialog.getView().findViewById(R.id.comment);
        commentET.setText(comment.getComment());
    }

    private void editComment(Comment comment){
        if(Anomologita.isConnected()){
            if(!(commentET.getText().toString()).equals(comment.getComment())){
                AttemptLogin editComment = new AttemptLogin();
                editComment.editComment(comment.getCommentID(),commentET.getText().toString());
                editComment.execute();
            }
        }else {
            Toast.makeText(Anomologita.getAppContext(),R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteComment(Comment comment){
        if(Anomologita.isConnected()){
            AttemptLogin deleteComment = new AttemptLogin();
            deleteComment.deleteComment(comment.getCommentID(), String.valueOf(post.getPost_id()));
            deleteComment.execute();
        }else {
            Toast.makeText(Anomologita.getAppContext(),R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
    }

    private void okClick() {
        EditText commentET = (EditText) findViewById(R.id.editText);
        String comment = commentET.getText().toString();
        if (comment.equals("")) {
            YoYo.with(Techniques.Tada).duration(700).playOn(commentET);
            Toast.makeText(Anomologita.getAppContext(), "Κενό Σχόλιο", Toast.LENGTH_SHORT).show();
        } else {
            if (Anomologita.isConnected()) {
                    wheel.spin();
                    AttemptLogin setComment = new AttemptLogin();
                    setComment.setComment(String.valueOf(post.getPost_id()), comment, "setComment", this);
                    setComment.execute();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(commentET.getWindowToken(), 0);
                    commentET.setText("");
                if (!post.getUser_id().equals(Anomologita.userID)) {
                    String text = "To ανομολόγητο σου " + post.getHashtagName() + " έχει νέα σχόλια";
                    AttemptLogin sendNotification = new AttemptLogin();
                    sendNotification.sendNotification(text, "comment", String.valueOf(post.getPost_id()), post.getReg_id());
                    sendNotification.execute();
                }
            } else {
                YoYo.with(Techniques.Tada).duration(700).playOn(commentET);
                Toast.makeText(Anomologita.getAppContext(), R.string.noInternet, Toast.LENGTH_SHORT).show();
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
        handler.removeCallbacks(runnable);
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
