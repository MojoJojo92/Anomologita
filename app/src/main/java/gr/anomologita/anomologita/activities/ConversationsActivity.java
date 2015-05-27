package gr.anomologita.anomologita.activities;

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
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.adapters.ConversationsAdapter;
import gr.anomologita.anomologita.databases.ConversationsDBHandler;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.objects.Conversation;

public class ConversationsActivity extends ActionBarActivity implements LoginMode {

    private ConversationsDBHandler db;
    private ConversationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar_recycler_view_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        db = new ConversationsDBHandler(this);
        adapter = new ConversationsAdapter(this, this);
        adapter.setMainData(db.getAllConversations());

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

    public void selected(Conversation conversation) {
        conversation.setSeen("yes");
        db.updateConversation(conversation);
        Anomologita.conversation = conversation;
        Intent i = new Intent(this, ChatActivity.class);
        startActivity(i);
        db.close();
        finish();
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

    }

    public void delete(final int conversationID, final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Διαγραφή Συνομιλίας")
                .setMessage("Σίγουρα θες να διαγράψεις αυτή την συνομιλία;")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.deleteData(position);
                        db.deleteConversation(conversationID);
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