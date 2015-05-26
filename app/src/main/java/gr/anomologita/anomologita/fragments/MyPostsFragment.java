package gr.anomologita.anomologita.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.adapters.MyPostsAdapter;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.extras.Keys.MyPostsComplete;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.Post;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class MyPostsFragment extends Fragment implements MyPostsComplete, LoginMode {


    private List<Post> myPosts = new ArrayList<>();
    private MyPostsAdapter fragmentMePostsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    public static MyPostsFragment newInstance() {
        return new MyPostsFragment();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public MyPostsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view_layout, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity())
                        .margin(50)
                        .color(getResources().getColor(R.color.primaryColor))
                        .build());

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(100);
        animator.setRemoveDuration(100);
        recyclerView.setItemAnimator(animator);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        fragmentMePostsAdapter = new MyPostsAdapter(getActivity(), this);
        recyclerView.setAdapter(fragmentMePostsAdapter);
        fragmentMePostsAdapter.setPosts(myPosts);

        getPosts();
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPosts();
            }
        });

        return view;
    }

    private void getPosts(){
        if (Anomologita.isConnected())
            new AttemptLogin(GET_USER_POSTS, this).execute();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void onGetUserPostsCompleted(List<Post> userPosts) {
        fragmentMePostsAdapter.setPosts(userPosts);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDeleteUserPostCompleted() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void deletePost(String postID, int position) {
        if (Anomologita.isConnected()) {
            new AttemptLogin(DELETE_POST, postID, this).execute();
            fragmentMePostsAdapter.deleteData(position);
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }
}
