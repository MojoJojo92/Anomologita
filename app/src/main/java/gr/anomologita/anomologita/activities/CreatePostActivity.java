package gr.anomologita.anomologita.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.sql.Timestamp;
import java.util.Random;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.databases.PostsDBHandler;
import gr.anomologita.anomologita.extras.BackAwareEditText;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.extras.Keys.PostComplete;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.Post;

public class CreatePostActivity extends ActionBarActivity implements LoginMode, PostComplete {

    private String groupName, postTxt, location;
    private int groupID;
    private TextView locationSize, postSize;
    private BackAwareEditText postET, locationET;
    private RelativeLayout layout;
    private LinearLayout dummy;
    //  private MMInterstitial interstitial;
    private InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_post_layout);
        layout = (RelativeLayout) findViewById(R.id.editPostLayout);
        dummy = (LinearLayout) findViewById(R.id.dummyView);

        dialog();
        Toolbar toolbar = (Toolbar) findViewById(R.id.editPostToolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        postET = (BackAwareEditText) findViewById(R.id.currentPost);
        postET.setHint("Γράψε το ανομολόγητό σου...");
        locationET = (BackAwareEditText) (findViewById(R.id.currentLocation));
        locationET.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_ENTER;
            }
        });
        locationET.setHint("Γράψε σχολή, περιοχή ή άλλο!\n(π.χ Πανεπιστήμιο Μακεδονίας)");
        postSize = (TextView) findViewById(R.id.postSize);
        setPostSize();
        locationSize = (TextView) findViewById(R.id.locationSize);
        setLocationSize();

        groupName = Anomologita.getCurrentGroupName();
        groupID = Integer.parseInt(Anomologita.getCurrentGroupID());

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
                postET.clearFocus();
                locationET.clearFocus();
                dummy.requestFocus();
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
                locationET.clearFocus();
                postET.clearFocus();
                dummy.requestFocus();
            }
        });

        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getResources().getString(R.string.full_ad));

        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();

       /* interstitial = new MMInterstitial(this);
        MMRequest request = new MMRequest();
        request.setAge("25");
        request.setEthnicity(MMRequest.ETHNICITY_WHITE);
        request.setEducation(MMRequest.EDUCATION_BACHELORS);
        interstitial.setMMRequest(request);
        interstitial.setApid("204172"); */
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

    private void dialog() {
        new MaterialDialog.Builder(this)
                .title("Κανόνες")
                .content(getString(R.string.postRules))
                .positiveText("ΣΥΜΦΩΝΩ")
                .positiveColor(getResources().getColor(R.color.accentColor))
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
            } else if (postTxt.length() > 2000) {
                YoYo.with(Techniques.Tada).duration(700).playOn(postET);
                Toast.makeText(this, "Το μήνυμα ξεπερνά τους 2000 χαρακτήρες!!!", Toast.LENGTH_SHORT).show();
            } else if (location.length() > 50) {
                YoYo.with(Techniques.Tada).duration(700).playOn(locationET);
                Toast.makeText(this, "Το προσδιοριστικό ξεπερνά τους 50 χαρακτήρες", Toast.LENGTH_SHORT).show();
            } else {
                if (Anomologita.isConnected()) {
                    AttemptLogin setPost = new AttemptLogin();
                    setPost.setPost(postTxt, location, String.valueOf(groupID), this);
                    setPost.execute();
                    Random r = new Random();
                    int Low = 1;
                    int High = 2;
                    int R = r.nextInt(High - Low) + Low;
                    if (R == 1) {
                        if (interstitial.isLoaded())
                            interstitial.show();
                      /*  interstitial.setListener(new RequestListener.RequestListenerImpl() {
                            @Override
                            public void requestCompleted(MMAd mmAd) {
                                interstitial.display();
                            }
                        });
                        interstitial.fetch(); */
                    }
                    resultOK();
                } else {
                    YoYo.with(Techniques.Tada).duration(700).playOn(layout);
                    Toast.makeText(Anomologita.getAppContext(), R.string.noInternet, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void resultOK() {
        HidingGroupProfileListener.mGroupProfileOffset = 0;
        Intent intent = new Intent();
        intent.putExtra("refresh", 1);
        setResult(RESULT_OK, intent);
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

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder() .build();
        interstitial.loadAd(adRequest);
    }

    @Override
    public void onBackPressed() {
        HidingGroupProfileListener.mGroupProfileOffset = 0;
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
