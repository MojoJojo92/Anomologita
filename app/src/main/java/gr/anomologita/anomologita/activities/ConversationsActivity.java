package gr.anomologita.anomologita.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.adapters.ConversationsAdapter;
import gr.anomologita.anomologita.databases.ConversationsDBHandler;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.objects.Conversation;

public class ConversationsActivity extends ActionBarActivity implements LoginMode {

    private ConversationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar_recycler_view_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView title = (TextView) findViewById(R.id.title);
        ConversationsDBHandler db = new ConversationsDBHandler(this);
        if (db.getAllConversations().size() == 0)
            title.setText(getResources().getString(R.string.noConversations));
        else
            title.setText("");
        db.close();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        AdView ad = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        ad.loadAd(adRequest);

        adapter = new ConversationsAdapter(this);
        setData();

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(100);
        animator.setRemoveDuration(100);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setItemAnimator(animator);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this)
                .margin(Anomologita.convert(10)).color(getResources().getColor(R.color.primaryColor)).build());

    }

    private void setData() {
        ConversationsDBHandler db = new ConversationsDBHandler(this);
        adapter.setMainData(db.getAllConversations());
        db.close();
    }

    private void deleteConversation(int conID){
        ConversationsDBHandler db =  new ConversationsDBHandler(this);
        db.deleteConversation(conID);
        db.close();
    }

    public void selected(Conversation conversation) {
        conversation.setSeen("yes");
        ConversationsDBHandler db = new ConversationsDBHandler(this);
        db.updateConversation(conversation);
        db.close();
        Anomologita.conversation = conversation;
        Intent i = new Intent(this, ChatActivity.class);
        startActivityForResult(i, 3);
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void delete(final int conversationID, int position) {
        final int currentPosition = position;
        new AlertDialog.Builder(this)
                .setTitle("Διαγραφή Συνομιλίας")
                .setMessage("Σίγουρα θες να διαγράψεις αυτή την συνομιλία;")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.deleteData(currentPosition);
                        deleteConversation(conversationID);
                        Toast.makeText(Anomologita.getAppContext(), "Η συνομιλία έχει διαγραφεί", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 3) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    break;
                case Activity.RESULT_CANCELED:
                    setData();
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
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}