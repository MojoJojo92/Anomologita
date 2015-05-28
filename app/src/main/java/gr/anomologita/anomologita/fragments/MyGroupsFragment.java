package gr.anomologita.anomologita.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

        recyclerView.setItemAnimator(animator);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MyGroupsAdapter(getActivity(), this, view);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity())
                        .margin(Anomologita.convert(10))
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
        if (Anomologita.isConnected())
            new AttemptLogin(GET_USER_GROUPS, this).execute();
    }

    @Override
    public void onGetUserGroupsCompleted(List<GroupProfile> userGroups) {
        adapter.setGroups(userGroups);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void show(GroupProfile groupProfile) {
        Anomologita.setCurrentGroupName(groupProfile.getGroupName());
        Anomologita.setCurrentGroupID(String.valueOf(groupProfile.getGroup_id()));
        getActivity().onBackPressed();
    }

    public void deleteGroup(final int position, final GroupProfile groupProfile) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Διαγραφή Γκροθπ")
                .setMessage("Are you sure you want to delete this group?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new AttemptLogin(DELETE_GROUP, String.valueOf(groupProfile.getGroup_id())).execute();
                        adapter.deleteData(position);
                        Toast.makeText(getActivity(), "Το γκρουπ " + groupProfile.getGroupName() + " έχει διαγραφεί", Toast.LENGTH_SHORT).show();
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
}
