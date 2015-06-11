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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

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

        AdView ad = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        ad.loadAd(adRequest);

        TextView title = (TextView) findViewById(R.id.title);
        NotificationDBHandler db = new NotificationDBHandler(this);
        if (db.getAllNotifications().size() == 0)
            title.setText(getResources().getString(R.string.noNotifications));
        else
            title.setText("");
        db.close();

        NotificationsAdapter notificationsAdapter = new NotificationsAdapter(this);
        notificationsAdapter.setMainData(notifications);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(notificationsAdapter);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .margin(Anomologita.convert(10))
                        .color(getResources().getColor(R.color.primaryColor))
                        .build());

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(100);
        animator.setRemoveDuration(100);
        recyclerView.setItemAnimator(animator);
    }

    public void groupClick(Notification notification) {
        HidingGroupProfileListener.mGroupProfileOffset = 0;
        Anomologita.setCurrentGroupID(String.valueOf(notification.getId()));
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public void postClick(Notification notification) {
        ok = true;
        PostsDBHandler db = new PostsDBHandler(this);
        Post post = db.getPost(Integer.parseInt(notification.getId()));
        db.close();
        Intent i = new Intent(this, CommentActivity.class);
        post.setLiked(new LikesDBHandler(this).exists(post.getPost_id()));
        post.setUser_id(Anomologita.USER_ID);
        startActivityForResult(i, 1);
        Anomologita.currentPost = post;
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
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
