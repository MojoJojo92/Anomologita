package gr.anomologita.anomologita.activities;

import android.content.Intent;
import android.graphics.Color;
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

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.databases.PostsDBHandler;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.extras.Keys.PostComplete;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.Post;

public class CreatePostActivity extends ActionBarActivity implements LoginMode, PostComplete {

    private String groupName;
    private int groupID;
    private String postTxt;
    private String location;
    private EditText postET, locationET;
    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_post_layout);
        layout = (RelativeLayout) findViewById(R.id.editPostLayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.editPostToolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        postET = (EditText) findViewById(R.id.currentPost);
        postET.setHint("Γράψε το ανομολόγητό σου...");
        locationET = (EditText) findViewById(R.id.currentLocation);
        locationET.setHint("Απο που το στέλνεις?");

        groupName = Anomologita.getCurrentGroupName();
        groupID = Integer.parseInt(Anomologita.getCurrentGroupID());

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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
        PostsDBHandler db = new PostsDBHandler(this);
        db.createPost(post);
        db.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        postTxt = postET.getText().toString();
        location = locationET.getText().toString();
        int id = item.getItemId();
        if (id == R.id.createPost) {
            if (postTxt.equals("")) {
                YoYo.with(Techniques.Tada).duration(700).playOn(postET);
                Toast.makeText(this, "Το μήνυμα είναι κενό!!!", Toast.LENGTH_SHORT).show();
            } else if (location.equals("")) {
                YoYo.with(Techniques.Tada).duration(700).playOn(locationET);
                Toast.makeText(this, "Δώσε σχολή, κατοικια ή άλλο", Toast.LENGTH_SHORT).show();
            } else if (postTxt.length() > 1000) {
                YoYo.with(Techniques.Tada).duration(700).playOn(postET);
                Toast.makeText(this, "Το μήνυμα ξεπερνά τους 1000 χαρακτήρες!!!", Toast.LENGTH_SHORT).show();
            } else if (location.length() > 20) {
                YoYo.with(Techniques.Tada).duration(700).playOn(locationET);
                Toast.makeText(this, "Το προσδιοριστικό ξεπερνά τους 30 χαρακτήρες", Toast.LENGTH_SHORT).show();
            } else {
                if (Anomologita.isConnected()) {
                    new AttemptLogin(POST, postTxt, location, String.valueOf(groupID), this).execute();
                    onBackPressed();
                } else {
                    YoYo.with(Techniques.Tada).duration(700).playOn(layout);
                    Toast.makeText(Anomologita.getAppContext(), "ΔΕΝ ΥΠΑΡΧΕΙ ΣΘΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
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