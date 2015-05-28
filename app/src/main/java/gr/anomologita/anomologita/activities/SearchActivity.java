package gr.anomologita.anomologita.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.adapters.SearchAdapter;
import gr.anomologita.anomologita.databases.PostsDBHandler;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.GroupSearch;

public class SearchActivity extends ActionBarActivity implements LoginMode, Keys.SearchComplete, SearchAdapter.ClickListener {

    private final List<GroupSearch> searches = new ArrayList<>();
    private RecyclerView recyclerView;
    private SearchAdapter adapter;
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

        db = new PostsDBHandler(this);
        adapter = new SearchAdapter(this);
        adapter.setMainData(searches);
        adapter.setClickListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .margin(50)
                        .color(getResources().getColor(R.color.primaryColor))
                        .build());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setActivated(true);
        searchView.setQueryHint("Αναζήτηση γκρουπ..");
        searchView.setIconified(false);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                recyclerView.setAdapter(adapter);
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0) {
                    search(newText);
                } else if (newText.length() == 0) {
                    recyclerView.setAdapter(adapter);
                }
                return false;
            }
        });

        return true;
    }

    private void search(String search) {
        if (Anomologita.isConnected())
            new AttemptLogin(SEARCH, search, this).execute();
    }

    @Override
    public void onSearchCompleted(List<GroupSearch> groupSearches) {
         adapter.setMainData(groupSearches);
         recyclerView.setAdapter(adapter);
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

    @Override
    public void itemClicked(int position) {
          Anomologita.setCurrentGroupID(String.valueOf(adapter.getData(position).getGroupID()));
          Anomologita.setCurrentGroupName(adapter.getData(position).getTitle());
          onBackPressed();
    }
}
