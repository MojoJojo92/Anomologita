package gr.anomologita.anomologita.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.MainActivity;
import gr.anomologita.anomologita.adapters.NavAdapter;
import gr.anomologita.anomologita.adapters.SearchAdapter;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.EndpointGroups;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.extras.Keys.SearchComplete;
import gr.anomologita.anomologita.objects.GroupSearch;

public class NavFragment extends Fragment implements NavAdapter.ClickListener, View.OnClickListener, LoginMode {

    private RecyclerView recyclerView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout drawerLayout;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private NavAdapter navAdapter;

    public NavFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = Boolean.valueOf(readFromPreferences(getActivity(), EndpointGroups.KEY_USER_LEARNED_DRAWER, "true"));
        if (savedInstanceState != null)
            mFromSavedInstanceState = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_nav_layout, container, false);
        navAdapter = new NavAdapter(getActivity(), this, drawerLayout);
        navAdapter.setMainData();
        navAdapter.setClickListener(this);

        recyclerView = (RecyclerView) layout.findViewById(R.id.drawerList);
        recyclerView.setAdapter(navAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return layout;
    }

    public void setUp(int fragmentId, final DrawerLayout drawerLayout, Toolbar toolbar) {
        final View containerView = getActivity().findViewById(fragmentId);
        containerView.isInEditMode();
        this.drawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!mUserLearnedDrawer) {
                    saveToPreferences(getActivity(), EndpointGroups.KEY_USER_LEARNED_DRAWER, true + "");
                }
            }

            // @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                ((MainActivity) getActivity()).onDrawerSlide(slideOffset);
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            drawerLayout.openDrawer(containerView);
        }
        drawerLayout.setDrawerListener(mDrawerToggle);
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });
    }

    public static void saveToPreferences(Context context, String preferenceName, String preferenceValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(EndpointGroups.PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String readFromPreferences(Context context, String preferenceName, String preferenceValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(EndpointGroups.PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, preferenceValue);
    }

    @Override
    public void itemClicked(View view, int position) {
        LinearLayout mGroupProfileContainer = (LinearLayout) getActivity().findViewById(R.id.groupProfileContainer);
        mGroupProfileContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
        HidingGroupProfileListener.mGroupProfileOffset = 0;
        Toast.makeText(this.getActivity(), "Position " + position, Toast.LENGTH_SHORT).show();
        Anomologita.setCurrentGroupID(String.valueOf(navAdapter.getData(position - 1).getId()));
        Anomologita.setCurrentGroupName(navAdapter.getData(position - 1).get_name());
        ((MainActivity) getActivity()).setGroup();
    }

    public void updateDrawer() {
        navAdapter = new NavAdapter(getActivity(), this, drawerLayout);
        navAdapter.setMainData();
        navAdapter.setClickListener(this);
        recyclerView.setAdapter(navAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onClick(View v) {

    }
}
