package gr.anomologita.anomologita.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.EditGroupActivity;
import gr.anomologita.anomologita.activities.MeActivity;
import gr.anomologita.anomologita.adapters.MyGroupsAdapter;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.extras.Keys.MyGroupsComplete;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.GroupProfile;

public class MyGroupsFragment extends Fragment implements MyGroupsComplete, LoginMode {

    private final List<GroupProfile> groups = new ArrayList<>();
    private MyGroupsAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static MyGroupsFragment newInstance() {
        return new MyGroupsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.recycler_view_layout, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(100);
        animator.setRemoveDuration(100);

    /*    MMAdView adLayout = (MMAdView) view.findViewById(R.id.adView);
        MMRequest request = new MMRequest();
        request.setAge("25");
        request.setEthnicity(MMRequest.ETHNICITY_WHITE);
        request.setEducation(MMRequest.EDUCATION_BACHELORS);
        adLayout.setMMRequest(request);
        adLayout.getAd(); */

        recyclerView.setItemAnimator(animator);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MyGroupsAdapter(this, view);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity())
                        .margin(Anomologita.convert(10))
                        .size(Anomologita.convert(0.5f))
                        .color(getResources().getColor(R.color.primaryColor))
                        .build());
        adapter.setGroups(groups);

        getGroups();
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getGroups();
            }
        });
        return view;
    }

    private void getGroups() {
        if (Anomologita.isConnected()){
            AttemptLogin getUserGroups = new AttemptLogin();
            getUserGroups.getUserGroups(this);
            getUserGroups.execute();
        }else {
            Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGetUserGroupsCompleted(List<GroupProfile> userGroups) {
        adapter.setGroups(userGroups);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void show(GroupProfile groupProfile) {
        Anomologita.setCurrentGroupName(groupProfile.getGroupName());
        Anomologita.setCurrentGroupID(String.valueOf(groupProfile.getGroup_id()));
        ((MeActivity)getActivity()).returnResult();
    }

    public void edit(GroupProfile groupProfile) {
        if (Anomologita.isConnected()) {
            Intent i = new Intent(getActivity(), EditGroupActivity.class);
            i.putExtra("hashtag", groupProfile.getHashtag_name());
            i.putExtra("name", groupProfile.getGroupName());
            i.putExtra("id", String.valueOf(groupProfile.getGroup_id()));
            startActivityForResult(i, 1);
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }
    }
}
