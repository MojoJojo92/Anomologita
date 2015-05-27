package gr.anomologita.anomologita.fragments;

import android.content.Intent;
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

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.CreateGroupActivity;
import gr.anomologita.anomologita.activities.MainActivity;
import gr.anomologita.anomologita.adapters.NavAdapter;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.LoginMode;

public class NavFragment extends Fragment implements NavAdapter.ClickListener, LoginMode {

    private RecyclerView recyclerView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout drawerLayout;
    private NavAdapter navAdapter;

    public NavFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
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
       // mDrawerToggle.setDrawerIndicatorEnabled(false);
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
                drawerLayout.openDrawer(Gravity.START);
            }
        });
    }

    @Override
    public void itemClicked(View view, int position, int viewType) {
        if (viewType == 1) {
            Intent i = new Intent(getActivity(), CreateGroupActivity.class);
            startActivity(i);
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            getActivity().finish();
        } else {
            LinearLayout mGroupProfileContainer = (LinearLayout) getActivity().findViewById(R.id.groupProfileContainer);
            mGroupProfileContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            HidingGroupProfileListener.mGroupProfileOffset = 0;
            Anomologita.setCurrentGroupID(String.valueOf(navAdapter.getData(position, viewType).getId()));
            Anomologita.setCurrentGroupName(navAdapter.getData(position, viewType).get_name());
            ((MainActivity) getActivity()).setGroup();
        }
    }

    public void updateDrawer() {
        navAdapter = new NavAdapter(getActivity(), this, drawerLayout);
        navAdapter.setMainData();
        navAdapter.setClickListener(this);
        recyclerView.setAdapter(navAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
}
