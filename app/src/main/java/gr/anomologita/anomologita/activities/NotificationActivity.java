package gr.anomologita.anomologita.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.adapters.NotificationsAdapter;
import gr.anomologita.anomologita.databases.LikesDBHandler;
import gr.anomologita.anomologita.databases.NotificationDBHandler;
import gr.anomologita.anomologita.databases.PostsDBHandler;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.objects.Notification;
import gr.anomologita.anomologita.objects.Post;

public class NotificationActivity extends ActionBarActivity implements LoginMode {

    private final List<Notification> notifications = new ArrayList<>();
    private boolean ok = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar_recycler_view_layout);

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

      /*  MMAdView adLayout = (MMAdView) findViewById(R.id.adView);
        MMRequest request = new MMRequest();
        request.setAge("25");
        request.setEthnicity(MMRequest.ETHNICITY_WHITE);
        request.setEducation(MMRequest.EDUCATION_BACHELORS);
        adLayout.setMMRequest(request);
        adLayout.getAd(); */

        TextView titleTop = (TextView) findViewById(R.id.titleTop);
        TextView titleBottom = (TextView) findViewById(R.id.titleBottom);
        NotificationDBHandler db = new NotificationDBHandler(this);
        if (db.getAllNotifications().size() == 0) {
            titleTop.setText(getResources().getString(R.string.noNotificationsTop));
            titleBottom.setText(getResources().getString(R.string.noNotificationsBottom));
        }else {
            titleTop.setText("");
            titleBottom.setText("");
        }
        db.close();

        NotificationsAdapter notificationsAdapter = new NotificationsAdapter(this);
        notificationsAdapter.setMainData(notifications);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(notificationsAdapter);

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(100);
        animator.setRemoveDuration(100);
        recyclerView.setItemAnimator(animator);
    }

    public void groupClick(Notification notification) {
        if(Anomologita.isConnected()){
            HidingGroupProfileListener.mGroupProfileOffset = 0;
            Anomologita.setCurrentGroupID(String.valueOf(notification.getId()));
            Anomologita.setCurrentGroupUserID(Anomologita.userID);
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        }else {
            Toast.makeText(Anomologita.getAppContext(), R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
    }

    public void postClick(Notification notification) {
        if(Anomologita.isConnected()){
            ok = true;
            PostsDBHandler db = new PostsDBHandler(this);
            if(db.exists(Integer.parseInt(notification.getId()))){
                Anomologita.setCurrentGroupUserID(notification.getAdminID());
                Post post = db.getPost(Integer.parseInt(notification.getId()));
                db.close();
                Intent i = new Intent(this, CommentActivity.class);
                post.setLiked(new LikesDBHandler(this).exists(post.getPost_id()));
                post.setUser_id(Anomologita.USER_ID);
                startActivityForResult(i, 1);
                Anomologita.currentPost = post;
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }else {
                Toast.makeText(Anomologita.getAppContext(), "Το ανομολόγητο έχει διαγραφεί", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(Anomologita.getAppContext(), R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            switch (resultCode) {
                case Activity.RESULT_OK:
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
        Intent intent = new Intent();
        if (ok)
            setResult(Activity.RESULT_OK, intent);
        else
            setResult(Activity.RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
