package gr.anomologita.anomologita.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.adapters.NotificationsAdapter;
import gr.anomologita.anomologita.databases.PostsDBHandler;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.objects.Notification;
import gr.anomologita.anomologita.objects.Post;

public class NotificationActivity extends ActionBarActivity implements LoginMode {

    private final List<Notification> notifications = new ArrayList<>();
    private PostsDBHandler db;

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

        db = new PostsDBHandler(this);
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
        db.close();
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public void postClick(Notification notification) {
        Post post = db.getPost(Integer.parseInt(notification.getId()));
        Intent i = new Intent(this, CommentActivity.class);
        startActivityForResult(i, 1);
        Log.e("okoko", post.getTimestamp());
        Anomologita.currentPost = post;
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // setGroup();
                    break;
                case Activity.RESULT_CANCELED:
                    //Write your code if there's no result
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        db.close();
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
