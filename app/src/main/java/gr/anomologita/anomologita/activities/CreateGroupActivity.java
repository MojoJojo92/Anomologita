package gr.anomologita.anomologita.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
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
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.ByteArrayOutputStream;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.extras.Keys.CheckGroupComplete;
import gr.anomologita.anomologita.extras.Keys.CreateGroupComplete;
import gr.anomologita.anomologita.extras.Keys.ImageEditComplete;
import gr.anomologita.anomologita.extras.Keys.ImageSetComplete;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.network.AttemptLogin;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static android.util.Base64.DEFAULT;
import static android.util.Base64.encodeToString;

public class CreateGroupActivity extends ActionBarActivity implements LoginMode, ImageEditComplete, CreateGroupComplete, ImageSetComplete, CheckGroupComplete {

    private static final int SELECT_PICTURE = 1;
    private ImageView picture;
    private EditText groupNameET, hashtagET;
    private String image;
    private String groupName, hashtag, groupID;
    private RelativeLayout layout;
    private ProgressWheel wheel;

    public static String encodeTobase64(Bitmap image) {
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
                if (Anomologita.isConnected())
                    new AttemptLogin(EDIT_IMAGE, selectedImageUri, this).execute();
            }
        }
    }

    @Override
    public void onImageEditComplete(Bitmap imageBitmap) {
        image = encodeTobase64(imageBitmap);
    }

    @Override
    public void onCreateGroupComplete(String groupID) {
        this.groupID = groupID;
        if (image == null) {
            Bitmap bitmap = ((BitmapDrawable) picture.getDrawable()).getBitmap();
            image = encodeTobase64(bitmap);
        }
        if (Anomologita.isConnected())
            new AttemptLogin(SET_IMAGE, image, groupID, this).execute();
    }

    @Override
    public void onImageSetComplete() {
        wheel.stopSpinning();
        Anomologita.setCurrentGroupID(groupID);
        Anomologita.setCurrentGroupName(groupName);
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.createGroupComplete) {
            hashtag = hashtagET.getText().toString();
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
                    new AttemptLogin(CHECK_GROUP, groupName, this).execute();
                    wheel.spin();
                    layout.setAlpha((float) 0.3);
                } else {
                    YoYo.with(Techniques.Tada).duration(700).playOn(layout);
                    Toast.makeText(this, "ΔΕΝ ΥΠΑΡΧΕΙ ΣΘΝΔΕΣΗ", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckGroupComplete(Boolean exists) {
        if (exists) {
            YoYo.with(Techniques.Tada).duration(700).playOn(groupNameET);
            Toast.makeText(Anomologita.getAppContext(), "Δυστυχως το όνομα υπάρχει", Toast.LENGTH_SHORT).show();
        } else {
            if (Anomologita.isConnected())
                new AttemptLogin(CREATE_GROUP, hashtag, groupName, this, null).execute();
        }
    }
}
