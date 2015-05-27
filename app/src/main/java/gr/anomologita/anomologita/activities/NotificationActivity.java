package gr.anomologita.anomologita.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

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

public class NotificationActivity extends ActionBarActivity implements LoginMode {

    final Handler handler = new Handler();
    private List<Notification> notifications = new ArrayList<>();
    private RecyclerView recyclerView;
    private NotificationsAdapter notificationsAdapter;
    private PostsDBHandler db;


    public NotificationActivity() {

    }

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

        db = new PostsDBHandler(this);
        notificationsAdapter = new NotificationsAdapter(this);
        notificationsAdapter.setMainData(notifications);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(notificationsAdapter);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .margin(Anomologita.convert(10))
                        .color(getResources().getColor(R.color.primaryColor))
                        .build());

    }


    public void itemClicked(View view, int position) {
     /*   if (notificationsAdapter.getData(position).getType() != 2) {
            Post post = db.getPost(notificationsAdapter.getData(position).getPost_id());
            Intent i = new Intent(getApplicationContext(), CommentActivity.class);
            i.putExtra("postID", post.getPost_id());
            i.putExtra("post_name", post.getPost_txt());
            i.putExtra("hashtag", post.getHashtagName());
            i.putExtra("where", post.getLocation());
            i.putExtra("numberOfLikes", post.getLikes());
            i.putExtra("numberOfComments", post.getComments());
            db.close();
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            finish();
        } else {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            db.close();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            finish();
        } */
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        db.close();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        HidingGroupProfileListener.mGroupProfileOffset = 0;
        finish();
    }
}
