package gr.anomologita.anomologita.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Base64;
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
import com.bumptech.glide.signature.StringSignature;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.extras.Keys;
import gr.anomologita.anomologita.extras.Keys.ImageSetComplete;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.network.AttemptLogin;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static android.util.Base64.DEFAULT;

public class EditGroupActivity extends ActionBarActivity implements LoginMode, ImageSetComplete, Keys.CheckGroupComplete {

    private static final int SELECT_PICTURE = 1;
    private String currentGroupName, currentHashtag, newHashtag, newGroupName, groupID = null;
    private ImageView picture;
    private EditText groupNameET, hashtagET;
    private RelativeLayout layout;
    private Boolean imageChanged = false;

    private static String encodeToBase64(Bitmap image) {
        System.gc();
        if (image == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, DEFAULT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group_layout);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentHashtag = extras.getString("hashtag");
            currentGroupName = extras.getString("name");
            groupID = extras.getString("id");
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

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dStart, int dEnd) {
                for (int i = start; i < end; i++) {
                    if (Character.isSpaceChar(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        hashtagET.setFilters(new InputFilter[] { filter });
        groupNameET.setText(currentGroupName);
        hashtagET.setText(currentHashtag);

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
        getMenuInflater().inflate(R.menu.menu_edit_group, menu);
        Glide.clear(getWindow().getDecorView().findViewById(android.R.id.content));
        Glide.with(this).load("http://anomologita.gr/img/" + groupID + ".png")
                .signature(new StringSignature(UUID.randomUUID().toString()))
                .fitCenter()
                .into(picture);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                imageChanged = true;
                Uri selectedImageUri = data.getData();
                BitmapPool pool = Glide.get(this).getBitmapPool();
                Glide.with(this).load(selectedImageUri).asBitmap().transform(new CropCircleTransformation(pool), new FitCenter(pool)).into(picture);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.editGroupComplete) {
            editGroup();
        } else if (id == R.id.delete) {
            new MaterialDialog.Builder(this)
                    .title("Διαγραφή Γκρουπ")
                    .iconRes(R.drawable.ic_error_triangle)
                    .content("Σίγουρα θέλεις να διαγράψεις αυτό το γκρουπ;")
                    .positiveText("NAI")
                    .positiveColorRes(R.color.accentColor)
                    .neutralText("OXI")
                    .neutralColorRes(R.color.primaryColor)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            deleteGroup();
                        }
                    })
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void editGroup() {
        newHashtag = hashtagET.getText().toString().replace("#","");
        newGroupName = groupNameET.getText().toString();
        if (newHashtag.equals("")) {
            YoYo.with(Techniques.Tada).duration(700).playOn(hashtagET);
            Toast.makeText(Anomologita.getAppContext(), "Το hashtag είναι κενό!!!", Toast.LENGTH_SHORT).show();
        } else if (newGroupName.equals("")) {
            YoYo.with(Techniques.Tada).duration(700).playOn(groupNameET);
            Toast.makeText(Anomologita.getAppContext(), "Το όνομα είναι κενό!!!", Toast.LENGTH_SHORT).show();
        } else if (newHashtag.length() > 20) {
            YoYo.with(Techniques.Tada).duration(700).playOn(hashtagET);
            Toast.makeText(Anomologita.getAppContext(), "Το hashtag δεν πρέπει να ξεπερνά τους 20 χαρακτήρες!!!", Toast.LENGTH_SHORT).show();
        } else if (newGroupName.length() > 30) {
            YoYo.with(Techniques.Tada).duration(700).playOn(groupNameET);
            Toast.makeText(Anomologita.getAppContext(), "Το όνομα δεν πρέπει να ξεπερνά τους 30 χαρακτήρες!!!", Toast.LENGTH_SHORT).show();
        } else {
            checkChanges();
        }
    }

    private void deleteGroup() {
        if (Anomologita.isConnected()) {
            AttemptLogin deleteGroup = new AttemptLogin();
            deleteGroup.deleteGroup(groupID);
            deleteGroup.execute();
            Toast.makeText(this, "Το γκρουπ " + currentGroupName + " έχει διαγραφεί", Toast.LENGTH_SHORT).show();
            Anomologita.setCurrentGroupName(null);
            Anomologita.setCurrentGroupID(null);
            Anomologita.setCurrentGroupUserID(null);
            returnResult();
        }
    }

    private void checkChanges() {
        if (Anomologita.isConnected()) {
            if (imageChanged) {
                Bitmap bitmap = ((BitmapDrawable) picture.getDrawable()).getBitmap();
                String image = encodeToBase64(bitmap);
                AttemptLogin setImage = new AttemptLogin();
                setImage.image(image, groupID, this);
                setImage.execute();
            } else if (!newGroupName.equals(currentGroupName)) {
                AttemptLogin checkGroup = new AttemptLogin();
                checkGroup.checkGroup(newGroupName, this);
                checkGroup.execute();
            } else if (!newHashtag.equals(currentHashtag)) {
                AttemptLogin setHashtag = new AttemptLogin();
                setHashtag.setHashtag(groupID, newHashtag);
                setHashtag.execute();
            }
        } else {
            YoYo.with(Techniques.Tada).duration(700).playOn(layout);
            Toast.makeText(Anomologita.getAppContext(), R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
        returnResult();
    }

    @Override
    public void onImageSetComplete() {
        if (!newGroupName.equals(currentGroupName)) {
            AttemptLogin checkGroup = new AttemptLogin();
            checkGroup.checkGroup(newGroupName, this);
            checkGroup.execute();
        } else if (!newHashtag.equals(currentHashtag)) {
            AttemptLogin setHashtag = new AttemptLogin();
            setHashtag.setHashtag(groupID, newHashtag);
            setHashtag.execute();
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        } else {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        }
    }

    @Override
    public void onCheckGroupComplete(Boolean exists) {
        if (exists) {
            YoYo.with(Techniques.Tada).duration(700).playOn(groupNameET);
            Toast.makeText(Anomologita.getAppContext(), "Δυστυχως το όνομα υπάρχει", Toast.LENGTH_SHORT).show();
        } else {
            if (Anomologita.isConnected()) {
                AttemptLogin setGroupName = new AttemptLogin();
                setGroupName.setGroupName(newGroupName, groupID);
                setGroupName.execute();
                if (!newHashtag.equals(currentHashtag)) {
                    AttemptLogin setHashtag = new AttemptLogin();
                    setHashtag.setHashtag(groupID, newHashtag);
                    setHashtag.execute();
                }
                Anomologita.setCurrentGroupName(newGroupName);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            }
        }
    }

    private void returnResult() {
        Intent intent = new Intent();
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
