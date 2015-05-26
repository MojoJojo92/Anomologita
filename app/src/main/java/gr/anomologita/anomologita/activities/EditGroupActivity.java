package gr.anomologita.anomologita.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.signature.StringSignature;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.extras.Keys;
import gr.anomologita.anomologita.extras.Keys.ImageEditComplete;
import gr.anomologita.anomologita.extras.Keys.ImageSetComplete;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.network.AttemptLogin;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static android.util.Base64.DEFAULT;

public class EditGroupActivity extends ActionBarActivity implements LoginMode, ImageEditComplete, ImageSetComplete, Keys.CheckGroupComplete {

    private static final int SELECT_PICTURE = 1;
    private String currentGroupName, currentHashtag, image, newHashtag, newGroupName, groupID;
    private ImageView picture;
    private EditText groupNameET, hashtagET;
    private RelativeLayout layout;
    private Boolean imageChanged = false;
    private ProgressWheel wheel;

    public static String encodeToBase64(Bitmap image) {
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

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentHashtag = extras.getString("hashtag");
        }

        currentGroupName = Anomologita.getCurrentGroupName();
        groupID = Anomologita.getCurrentGroupID();

        Toolbar toolbar = (Toolbar) findViewById(R.id.createGroupToolbar);
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
        picture = (ImageView) findViewById(R.id.default_group_image);
        groupNameET = (EditText) findViewById(R.id.groupName);
        hashtagET = (EditText) findViewById(R.id.groupHashtagName);
        wheel = (ProgressWheel) findViewById(R.id.wheel);
        wheel.stopSpinning();

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
                if (Anomologita.isConnected())
                    new AttemptLogin(EDIT_IMAGE, selectedImageUri, this).execute();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.editGroupComplete) {
            newHashtag = hashtagET.getText().toString();
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
                return checkChanges();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkChanges() {
        if (Anomologita.isConnected()) {
            if (imageChanged)
                new AttemptLogin(SET_IMAGE, image, groupID, this).execute();
            if (!newHashtag.equals(currentHashtag))
                new AttemptLogin(SET_HASHTAG, groupID, newHashtag).execute();
            if (!newGroupName.equals(currentGroupName)) {
                new AttemptLogin(CHECK_GROUP, newGroupName, this).execute();
            }
        } else {
            YoYo.with(Techniques.Tada).duration(700).playOn(layout);
            Toast.makeText(Anomologita.getAppContext(), "ΔΕΝ ΥΠΑΡΧΕΙ ΣΘΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
        }
        onBackPressed();
        return true;
    }

    @Override
    public void onImageEditComplete(Bitmap imageBitmap) {
        image = encodeToBase64(imageBitmap);
    }

    @Override
    public void onImageSetComplete() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    public void onCheckGroupComplete(Boolean exists) {
        if (exists) {
            YoYo.with(Techniques.Tada).duration(700).playOn(groupNameET);
            Toast.makeText(Anomologita.getAppContext(), "Δυστυχως το όνομα υπάρχει", Toast.LENGTH_SHORT).show();
        } else {
            if (Anomologita.isConnected()) {
                new AttemptLogin(SET_GROUP_NAME, newGroupName, groupID).execute();
                Anomologita.setCurrentGroupName(newGroupName);
            }
        }
    }
}
