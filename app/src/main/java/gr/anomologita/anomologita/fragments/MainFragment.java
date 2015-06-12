package gr.anomologita.anomologita.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.ChatActivity;
import gr.anomologita.anomologita.activities.CommentActivity;
import gr.anomologita.anomologita.activities.EditPostActivity;
import gr.anomologita.anomologita.activities.MainActivity;
import gr.anomologita.anomologita.activities.MessageActivity;
import gr.anomologita.anomologita.adapters.MainAdapter;
import gr.anomologita.anomologita.databases.ConversationsDBHandler;
import gr.anomologita.anomologita.extras.HidingGroupProfileListener;
import gr.anomologita.anomologita.extras.Keys.GetPostsComplete;
import gr.anomologita.anomologita.extras.Keys.LoginMode;
import gr.anomologita.anomologita.network.AttemptLogin;
import gr.anomologita.anomologita.objects.Post;

public class MainFragment extends Fragment implements LoginMode, GetPostsComplete {

    private String sort, groupID = null;
    private int mGroupProfileHeight;
    private LinearLayout mGroupProfileContainer, name;
    private MainAdapter adapter;
    private TextView search, title, favorite;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static MainFragment newInstance(String sort) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString("sort", sort);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            sort = getArguments().getString("sort");
        groupID = Anomologita.getCurrentGroupID();
        mGroupProfileHeight = (int) (getResources().getDimension(R.dimen.groupProfileHeight) - (getResources().getDimension(R.dimen.titleSize)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_layout, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.listPostsNew);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        name = (LinearLayout) getActivity().findViewById(R.id.titleLayout);
        title = (TextView) getActivity().findViewById(R.id.title);
        title.setText(Anomologita.getCurrentGroupName());
        favorite = (TextView) getActivity().findViewById(R.id.favoritesButton);
        mGroupProfileContainer = (LinearLayout) getActivity().findViewById(R.id.groupProfileContainer);

        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setAddDuration(100);
        animator.setRemoveDuration(100);

        search = (TextView) view.findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Menu menu = ((MainActivity) getActivity()).getMenu();
                getActivity().onOptionsItemSelected(menu.findItem(R.id.search));
            }
        });

        if (groupID == null) {
            mGroupProfileContainer.animate().translationY(-mGroupProfileHeight).setInterpolator(new AccelerateInterpolator(2)).start();
            name.setAlpha(1);
            search.setVisibility(View.VISIBLE);
        }else {
            search.setVisibility(View.INVISIBLE);
        }

        adapter = new MainAdapter(this);
        recyclerView.setItemAnimator(animator);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollListener(new HidingGroupProfileListener(mGroupProfileHeight) {
            @Override
            public void onMoved(int distance) {
                mGroupProfileContainer.setTranslationY(-distance);
                name.setAlpha((float) (-mGroupProfileHeight / 2 + mGroupProfileOffset) * (float) 1 / mGroupProfileHeight * 2);
                if(mGroupProfileOffset == mGroupProfileHeight){
                    favorite.setVisibility(View.INVISIBLE);
                }else {
                    favorite.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onShow() {
                mGroupProfileContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                name.setAlpha(0);
                favorite.setVisibility(View.VISIBLE);
            }

            @Override
            public void onHide() {
                mGroupProfileContainer.animate().translationY(-mGroupProfileHeight).setInterpolator(new AccelerateInterpolator(2)).start();
                name.setAlpha(1);
                favorite.setVisibility(View.INVISIBLE);
            }
        });
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        TypedValue typed_value = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, -Anomologita.convert(80), getResources().getDimensionPixelSize(typed_value.resourceId) + Anomologita.convert(80));
        mSwipeRefreshLayout.setDistanceToTriggerSync(20);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPosts();
            }
        });

        getPosts();
        return view;
    }

    public void refresh() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
            getPosts();
        }
    }

    private void getPosts() {
        if (Anomologita.isConnected() && groupID != null && !title.getText().equals(getResources().getString(R.string.deleted)) && !title.getText().equals(getResources().getString(R.string.groupName))) {
            AttemptLogin getPosts = new AttemptLogin();
            getPosts.getPosts(groupID, sort, "0", "20", this);
            getPosts.execute();
            mGroupProfileContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            name.setAlpha(0);
            search.setVisibility(View.INVISIBLE);

        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            mGroupProfileContainer.animate().translationY(-mGroupProfileHeight).setInterpolator(new AccelerateInterpolator(2)).start();
            name.setAlpha(1);
            if(Anomologita.isConnected())
                search.setVisibility(View.VISIBLE);
        }
    }

    public void newComment(Post post) {
        Intent i = new Intent(getActivity(), CommentActivity.class);
        startActivityForResult(i, 1);
        Anomologita.currentPost = post;
        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void newMessage(Post post) {
        ConversationsDBHandler db = new ConversationsDBHandler(getActivity());
        String postID = String.valueOf(post.getPost_id());
        if (db.exists(postID, Anomologita.regID, post.getReg_id())) {
            Intent i = new Intent(getActivity(), ChatActivity.class);
            Anomologita.conversation = db.getConversation(postID, Anomologita.regID, post.getReg_id());
            startActivityForResult(i, 5);
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        } else {
            Intent i = new Intent(getActivity(), MessageActivity.class);
            i.putExtra("hashtag", post.getHashtagName());
            i.putExtra("userID", post.getUser_id());
            i.putExtra("regID", post.getReg_id());
            i.putExtra("postID", String.valueOf(post.getPost_id()));
            startActivityForResult(i, 1);
            getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }
    }

    public void editPost(Post post) {
        Intent i = new Intent(getActivity(), EditPostActivity.class);
        i.putExtra("post", post.getPost_txt());
        i.putExtra("location", post.getLocation());
        i.putExtra("postID", String.valueOf(post.getPost_id()));
        startActivityForResult(i, 1);
        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void setLike(String like, Post post) {
        if (Anomologita.isConnected()) {
            String text;
            AttemptLogin setLike = new AttemptLogin();
            setLike.setLike(String.valueOf(post.getPost_id()), like);
            setLike.execute();
            if (!post.getUser_id().equals(Anomologita.userID)) {
                int likes = post.getLikes() + Integer.parseInt(like);
                if (likes == 1)
                    text = "Το ανομολόγητο σου " + post.getHashtagName() + "\nπλέον  αρέσει σε " + likes + " άτομο";
                else
                    text = "Το ανομολόγητο σου " + post.getHashtagName() + "\nπλέον  αρέσει σε " + likes + " άτομα";
                AttemptLogin sendNotification = new AttemptLogin();
                sendNotification.sendNotification(text, "like", String.valueOf(post.getPost_id()), post.getReg_id());
                sendNotification.execute();
            }
        }
    }

    public void loadMore(int position) {
        if (Anomologita.isConnected() && groupID != null) {
            AttemptLogin getPost = new AttemptLogin();
            getPost.getPosts(groupID, sort, String.valueOf(position), String.valueOf(position + 20), this);
            getPost.execute();
        }
    }

    @Override
    public void onGetPostsComplete(List<Post> posts) {
        if (posts.size() > 0) {
            if (mSwipeRefreshLayout.isRefreshing()) {
                adapter.removeAll();
                adapter.setPostList(posts);
            } else {
                adapter.setPostList(posts);
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }
}