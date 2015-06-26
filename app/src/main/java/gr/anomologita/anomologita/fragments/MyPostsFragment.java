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

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.CommentActivity;
import gr.anomologita.anomologita.activities.EditPostActivity;
import gr.anomologita.anomologita.adapters.MyPostsAdapter;
import gr.anomologita.anomologita.databases.LikesDBHandler;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.extras.Keys.MyPostsComplete;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.Post;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class MyPostsFragment extends Fragment implements MyPostsComplete, LoginMode {


    private final List<Post> myPosts = new ArrayList<>();
    private MyPostsAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    public static MyPostsFragment newInstance() {
        return new MyPostsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view_layout, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity())
                        .margin(Anomologita.convert(10))
                        .size(Anomologita.convert(0.5f))
                        .color(getResources().getColor(R.color.primaryColor))
                        .build());

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(100);
        animator.setRemoveDuration(100);
        recyclerView.setItemAnimator(animator);

      /*  MMAdView adLayout = (MMAdView) view.findViewById(R.id.adView);
        MMRequest request = new MMRequest();
        request.setAge("25");
        request.setEthnicity(MMRequest.ETHNICITY_WHITE);
        request.setEducation(MMRequest.EDUCATION_BACHELORS);
        adLayout.setMMRequest(request);
        adLayout.getAd(); */

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MyPostsAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.setPosts(myPosts);

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
        if (Anomologita.isConnected()){
            AttemptLogin getUserPosts = new AttemptLogin();
            getUserPosts.getUserPosts(this);
            getUserPosts.execute();
        }else {
            Toast.makeText(getActivity(), R.string.noInternet, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGetUserPostsCompleted(List<Post> userPosts) {
        adapter.setPosts(userPosts);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDeleteUserPostCompleted() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void edit(Post post) {
        Intent i = new Intent(getActivity(), EditPostActivity.class);
        i.putExtra("post", post.getPost_txt());
        i.putExtra("location", post.getLocation());
        i.putExtra("postID", String.valueOf(post.getPost_id()));
        startActivityForResult(i,1);
        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void comment(Post post) {
        Intent i = new Intent(getActivity(), CommentActivity.class);
        post.setLiked(new LikesDBHandler(getActivity()).exists(post.getPost_id()));
        post.setUser_id(Anomologita.userID);
        startActivityForResult(i, 1);
        Anomologita.currentPost = post;
        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

}
