package gr.anomologita.anomologita.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.sql.Timestamp;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.databases.PostsDBHandler;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.extras.Keys.PostComplete;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.Post;

public class CreatePostActivity extends ActionBarActivity implements LoginMode, PostComplete {

    private String groupName, postTxt, location;
    private int groupID;
    private EditText postET, locationET;
    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_post_layout);
        layout = (RelativeLayout) findViewById(R.id.editPostLayout);

        dialog();
        Toolbar toolbar = (Toolbar) findViewById(R.id.editPostToolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        postET = (EditText) findViewById(R.id.currentPost);
        postET.setHint("Γράψε το ανομολόγητό σου...");
        locationET = (EditText) findViewById(R.id.currentLocation);
        locationET.setHint("Γράψε σχολή, περιοχή ή άλλο!\n(π.χ Πανεπιστήμιο Μακεδονίας)");

        groupName = Anomologita.getCurrentGroupName();
        groupID = Integer.parseInt(Anomologita.getCurrentGroupID());

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void dialog() {
        new MaterialDialog.Builder(this)
                .title("TEST")
                .content("Bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla" +
                        "Bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla")
                .positiveText("ΣΥΜΦΩΝΩ")
                .positiveColor(getResources().getColor(R.color.primaryColor))
                .cancelable(false)
                .negativeText("ΔΙΑΦΩΝΩ")
                .negativeColor(getResources().getColor(R.color.primaryColor))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        onBackPressed();
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
    }

    @Override
    public void onPostComplete(String postID, String hashtag) {
        Post post = new Post();
        post.setLikes(0);
        post.setComments(0);
        post.setPost_id(Integer.parseInt(postID));
        post.setHashtagName(hashtag);
        post.setPost_txt(postTxt);
        post.setLocation(location);
        post.setGroup_name(groupName);
        post.setGroup_id(groupID);
        post.setTimestamp((new Timestamp(System.currentTimeMillis())).toString());
        PostsDBHandler db = new PostsDBHandler(this);
        db.createPost(post);
        db.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        postTxt = postET.getText().toString();
        location = locationET.getText().toString();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(postET.getWindowToken(), 0);
        int id = item.getItemId();
        if (id == R.id.createPost) {
            if (postTxt.equals("")) {
                YoYo.with(Techniques.Tada).duration(700).playOn(postET);
                Toast.makeText(this, "Το μήνυμα είναι κενό!!!", Toast.LENGTH_SHORT).show();
            } else if (location.equals("")) {
                YoYo.with(Techniques.Tada).duration(700).playOn(locationET);
            } else if (postTxt.length() > 1000) {
                YoYo.with(Techniques.Tada).duration(700).playOn(postET);
                Toast.makeText(this, "Το μήνυμα ξεπερνά τους 1000 χαρακτήρες!!!", Toast.LENGTH_SHORT).show();
            } else if (location.length() > 20) {
                YoYo.with(Techniques.Tada).duration(700).playOn(locationET);
                Toast.makeText(this, "Το προσδιοριστικό ξεπερνά τους 30 χαρακτήρες", Toast.LENGTH_SHORT).show();
            } else {
                if (Anomologita.isConnected()) {
                    AttemptLogin setPost = new AttemptLogin();
                    setPost.setPost(postTxt, location, String.valueOf(groupID), this);
                    setPost.execute();
                    onBackPressed();
                } else {
                    YoYo.with(Techniques.Tada).duration(700).playOn(layout);
                    Toast.makeText(Anomologita.getAppContext(), "ΔΕΝ ΥΠΑΡΧΕΙ ΣΙΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
        HidingGroupProfileListener.mGroupProfileOffset = 0;
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
