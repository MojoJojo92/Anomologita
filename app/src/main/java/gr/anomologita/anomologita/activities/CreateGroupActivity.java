package gr.anomologita.anomologita.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.io.ByteArrayOutputStream;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.databases.FavoritesDBHandler;
import gr.anomologita.anomologita.extras.Keys.CheckGroupComplete;
import gr.anomologita.anomologita.extras.Keys.CreateGroupComplete;
import gr.anomologita.anomologita.extras.Keys.ImageSetComplete;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.GroupProfile;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static android.util.Base64.DEFAULT;
import static android.util.Base64.encodeToString;

public class CreateGroupActivity extends ActionBarActivity implements LoginMode, CreateGroupComplete, ImageSetComplete, CheckGroupComplete {

    private static final int SELECT_PICTURE = 1;
    private ImageView picture;
    private EditText groupNameET, hashtagET;
    private String image, groupName, hashtag, groupID;
    private RelativeLayout layout;
    private int requestCode;

    private static String encodeToBase64(Bitmap image) {
        System.gc();
        if (image == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return encodeToString(b, DEFAULT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group_layout);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            requestCode = extras.getInt("requestCode");
        }

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

        layout = (RelativeLayout) findViewById(R.id.createGroupLayout);
        picture = (ImageView) findViewById(R.id.groupImage);
        groupNameET = (EditText) findViewById(R.id.groupName);
        groupNameET.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_ENTER;
            }
        });
        hashtagET = (EditText) findViewById(R.id.hashTag);
        hashtagET.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_ENTER;
            }
        });

        InputFilter hashtagFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dStart, int dEnd) {
                for (int i = start; i < end; i++) {
                    if (Character.isSpaceChar(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        InputFilter nameFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dStart, int dEnd) {
                if (end > 40)
                    return source.subSequence(start, 40);
                return null;
            }
        };

        hashtagET.setFilters(new InputFilter[]{hashtagFilter});
        groupNameET.setFilters(new InputFilter[]{nameFilter});

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_group, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                BitmapPool pool = Glide.get(this).getBitmapPool();
                Glide.with(this).load(selectedImageUri).asBitmap().transform(new CropCircleTransformation(pool), new FitCenter(pool)).into(picture);
            }
        }
    }

    @Override
    public void onCreateGroupComplete(String groupID) {
        this.groupID = groupID;
        if (image == null) {
            Bitmap bitmap = ((BitmapDrawable) picture.getDrawable()).getBitmap();
            image = encodeToBase64(bitmap);
        }
        if (Anomologita.isConnected()) {
            AttemptLogin setImage = new AttemptLogin();
            setImage.image(image, groupID, this);
            setImage.execute();
        }
    }

    @Override
    public void onImageSetComplete() {
        Anomologita.setCurrentGroupID(groupID);
        Anomologita.setCurrentGroupName(groupName);
        Anomologita.setCurrentGroupUserID(Anomologita.userID);
        GroupProfile groupProfile = new GroupProfile();
        groupProfile.setGroup_id(Integer.parseInt(groupID));
        groupProfile.setGroupName(groupName);
        groupProfile.setUser_id(Anomologita.userID);
        groupProfile.setSubscribers(0);
        FavoritesDBHandler db = new FavoritesDBHandler(this);
        db.createFavorite(groupProfile);
        db.close();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
        if (requestCode == 3)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        else
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.createGroupComplete) {
            hashtag = hashtagET.getText().toString().replace("#", "");
            groupName = groupNameET.getText().toString();
            if (hashtag.equals("")) {
                YoYo.with(Techniques.Tada).duration(700).playOn(hashtagET);
                Toast.makeText(this, "Το hashtag είναι κενό!!!", Toast.LENGTH_SHORT).show();
            } else if (groupName.equals("")) {
                YoYo.with(Techniques.Tada).duration(700).playOn(groupNameET);
                Toast.makeText(this, "Το όνομα είναι κενό!!!", Toast.LENGTH_SHORT).show();
            } else if (hashtag.length() > 20) {
                YoYo.with(Techniques.Tada).duration(700).playOn(hashtagET);
                Toast.makeText(this, "Το hashtag δεν πρέπει να ξεπερνά τους 20 χαρακτήρες!!!", Toast.LENGTH_SHORT).show();
            } else if (groupName.length() > 40) {
                YoYo.with(Techniques.Tada).duration(700).playOn(groupNameET);
                Toast.makeText(this, "Το όνομα δεν πρέπει να ξεπερνά τους 40 χαρακτήρες!!!", Toast.LENGTH_SHORT).show();
            } else {
                if (Anomologita.isConnected()) {
                    AttemptLogin checkGroup = new AttemptLogin();
                    checkGroup.checkGroup(groupName, this);
                    checkGroup.execute();
                    layout.setAlpha((float) 0.3);
                } else {
                    YoYo.with(Techniques.Tada).duration(700).playOn(layout);
                    Toast.makeText(this, R.string.noInternet, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void progressDialog() {
        new MaterialDialog.Builder(this)
                .title("Δημιουργία Γκρουπ")
                .content("Παρακαλώ Περιμένετε")
                .cancelable(false)
                .progress(true, 0)
                .widgetColorRes(R.color.primaryColor)
                .show();
    }

    @Override
    public void onCheckGroupComplete(Boolean exists) {
        if (exists) {
            YoYo.with(Techniques.Tada).duration(700).playOn(groupNameET);
            Toast.makeText(Anomologita.getAppContext(), "Δυστυχως το όνομα υπάρχει", Toast.LENGTH_SHORT).show();
            layout.setAlpha((float) 1);
        } else {
            if (Anomologita.isConnected()) {
                AttemptLogin createGroup = new AttemptLogin();
                createGroup.createGroup(hashtag, groupName, this);
                createGroup.execute();
                progressDialog();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
        if (requestCode == 3)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        else
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }
}
