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
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.network.AttemptLogin;

public class EditPostActivity extends ActionBarActivity implements LoginMode {

    private String postID;
    private String currentPost;
    private String currentLocation;
    private EditText postET, locationET;
    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_post_layout);
        layout = (RelativeLayout) findViewById(R.id.editPostLayout);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentPost = extras.getString("post");
            currentLocation = extras.getString("location");
            postID = extras.getString("postID");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.editPostToolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postET = (EditText) findViewById(R.id.currentPost);
        postET.setText(currentPost);
        locationET = (EditText) findViewById(R.id.currentLocation);
        locationET.setText(currentLocation);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String newPost = postET.getText().toString();
        String newLocation = locationET.getText().toString();
        int id = item.getItemId();
        if (id == R.id.editPostComplete) {
            if (newPost.equals("")) {
                YoYo.with(Techniques.Tada).duration(700).playOn(postET);
                Toast.makeText(this, "Το μήνυμα είναι κενό!!!", Toast.LENGTH_SHORT).show();
            } else if (newLocation.equals("")) {
                YoYo.with(Techniques.Tada).duration(700).playOn(locationET);
                Toast.makeText(this, "Δώσε σχολή, κατοικια ή άλλο", Toast.LENGTH_SHORT).show();
            } else if (newPost.length() > 200) {
                YoYo.with(Techniques.Tada).duration(700).playOn(postET);
                Toast.makeText(this, "Το μήνυμα ξεπερνά τους 200 χαρακτήρες!!!", Toast.LENGTH_SHORT).show();
            } else if (newLocation.length() > 20) {
                YoYo.with(Techniques.Tada).duration(700).playOn(locationET);
                Toast.makeText(this, "Το προσδιοριστικό ξεπερνά τους 20 χαρακτήρες", Toast.LENGTH_SHORT).show();
            } else if (newPost.equals(currentPost) && newLocation.equals(currentLocation)) {
                onBackPressed();
            } else {
                if (Anomologita.isConnected()) {
                    new AttemptLogin(EDIT_POST, postID, newPost, newLocation).execute();
                    onBackPressed();
                } else {
                    YoYo.with(Techniques.Tada).duration(700).playOn(layout);
                    Toast.makeText(Anomologita.getAppContext(), "ΔΕΝ ΥΠΑΡΧΕΙ ΣΘΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
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
