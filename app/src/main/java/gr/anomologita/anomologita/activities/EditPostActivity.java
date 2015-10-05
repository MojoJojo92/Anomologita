package gr.anomologita.anomologita.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.extras.BackAwareEditText;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.extras.Keys.MyPostsComplete;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.Post;

public class EditPostActivity extends ActionBarActivity implements LoginMode, MyPostsComplete {

    private String postID, currentPost, currentLocation;
    private BackAwareEditText postET, locationET;
    private TextView postSize, locationSize;
    private RelativeLayout layout;
    private LinearLayout dummy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_post_layout);
        layout = (RelativeLayout) findViewById(R.id.editPostLayout);
        dummy = (LinearLayout) findViewById(R.id.dummyView);

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

        postET = (BackAwareEditText) findViewById(R.id.currentPost);
        postET.setText(currentPost);
        locationET = (BackAwareEditText) findViewById(R.id.currentLocation);
        locationET.setText(currentLocation);
        postSize = (TextView) findViewById(R.id.postSize);
        setPostSize();
        locationSize = (TextView) findViewById(R.id.locationSize);
        setLocationSize();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        postET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setPostSize();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        postET.setBackPressedListener(new BackAwareEditText.BackPressedListener() {
            @Override
            public void onImeBack(BackAwareEditText editText) {
                dummy.findFocus();
            }
        });

        locationET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setLocationSize();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        locationET.setBackPressedListener(new BackAwareEditText.BackPressedListener() {
            @Override
            public void onImeBack(BackAwareEditText editText) {
                dummy.findFocus();
            }
        });
    }

    private void setPostSize() {
        postSize.setText((postET.getText()).length() + "/2000");
        if ((postET.getText()).length() >= 2000)
            postSize.setTextColor(getResources().getColor(R.color.primaryColor));
        else
            postSize.setTextColor(getResources().getColor(R.color.secondaryTextColor));
    }

    private void setLocationSize() {
        locationSize.setText((locationET.getText()).length() + "/50");
        if ((locationET.getText()).length() >= 50)
            locationSize.setTextColor(getResources().getColor(R.color.primaryColor));
        else
            locationSize.setTextColor(getResources().getColor(R.color.secondaryTextColor));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.editPostComplete)
            editPost();
        else if (id == R.id.delete)
            new MaterialDialog.Builder(this)
                    .title("Διαγραφή Ανομολογήτου")
                    .iconRes(R.drawable.ic_error_triangle)
                    .content("Σίγουρα θέλεις να διαγράψεις αυτό το ανομολόγητο;")
                    .positiveText("NAI")
                    .positiveColorRes(R.color.accentColor)
                    .neutralText("OXI")
                    .neutralColorRes(R.color.primaryColor)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            deletePost();
                        }
                    })
                    .show();
        return super.onOptionsItemSelected(item);
    }

    private void deletePost() {
        if (Anomologita.isConnected()) {
            AttemptLogin deletePost = new AttemptLogin();
            deletePost.deletePost(postID, this);
            deletePost.execute();
        }else {
            Toast.makeText(this, R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
    }

    private void editPost() {
        String newPost = postET.getText().toString();
        String newLocation = locationET.getText().toString();
        if (newPost.equals("")) {
            YoYo.with(Techniques.Tada).duration(700).playOn(postET);
            Toast.makeText(this, "Το μήνυμα είναι κενό!!!", Toast.LENGTH_SHORT).show();
        } else if (newLocation.equals("")) {
            YoYo.with(Techniques.Tada).duration(700).playOn(locationET);
            Toast.makeText(this, "Δώσε σχολή, κατοικια ή άλλο", Toast.LENGTH_SHORT).show();
        } else if (newPost.length() > 2000) {
            YoYo.with(Techniques.Tada).duration(700).playOn(postET);
            Toast.makeText(this, "Το μήνυμα ξεπερνά τους 2000 χαρακτήρες!!!", Toast.LENGTH_SHORT).show();
        } else if (newLocation.length() > 50) {
            YoYo.with(Techniques.Tada).duration(700).playOn(locationET);
            Toast.makeText(this, "Το προσδιοριστικό ξεπερνά τους 50 χαρακτήρες", Toast.LENGTH_SHORT).show();
        } else if (newPost.equals(currentPost) && newLocation.equals(currentLocation)) {
            onBackPressed();
        } else {
            if (Anomologita.isConnected()) {
                AttemptLogin editPost = new AttemptLogin();
                editPost.editPost(postID, newPost, newLocation);
                editPost.execute();
                resultOK();
            } else {
                YoYo.with(Techniques.Tada).duration(700).playOn(layout);
                Toast.makeText(Anomologita.getAppContext(), R.string.noInternet, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onGetUserPostsCompleted(List<Post> userPosts) {

    }

    @Override
    public void onDeleteUserPostCompleted() {
        Toast.makeText(this, "Το ανομολόγητο έχει διαγραφεί", Toast.LENGTH_SHORT).show();
        resultOK();
    }

    private void resultOK() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
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
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
