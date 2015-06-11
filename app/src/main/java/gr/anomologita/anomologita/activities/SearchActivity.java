package gr.anomologita.anomologita.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.adapters.SearchAdapter;
import gr.anomologita.anomologita.databases.PostsDBHandler;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.extras.Keys.SearchComplete;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.Favorite;
import me.grantland.widget.AutofitHelper;

public class SearchActivity extends ActionBarActivity implements LoginMode, SearchComplete, SearchAdapter.ClickListener {

    private final List<Favorite> searches = new ArrayList<>();
    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private PostsDBHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar_recycler_view_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).margin(Anomologita.convert(10))
                .color(getResources().getColor(R.color.primaryColor)).build());

        search(" ");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        SearchView.SearchAutoComplete theTextArea = (SearchView.SearchAutoComplete)searchView.findViewById(R.id.search_src_text);
        theTextArea.setTextSize(15);
        searchView.setActivated(true);
        searchView.setQueryHint("Ψάξε για σχολές, περιοχές, άλλα");
        searchView.setIconified(false);
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
        if (Anomologita.isConnected()){
            AttemptLogin getSearch = new AttemptLogin();
            getSearch.getSearch(search, this);
            getSearch.execute();
        }
    }

    @Override
    public void onSearchCompleted(List<Favorite> groupSearches) {
        adapter.setMainData(groupSearches);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void itemClicked(int position) {
        HidingGroupProfileListener.mGroupProfileOffset = 0;
        Anomologita.setCurrentGroupID(String.valueOf(adapter.getData(position).getId()));
        Anomologita.setCurrentGroupName(adapter.getData(position).get_name());
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        db.close();
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
        setResult(Activity.RESULT_CANCELED, intent);
        db.close();
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
