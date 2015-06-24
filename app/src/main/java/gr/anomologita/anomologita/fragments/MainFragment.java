package gr.anomologita.anomologita.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.ByteArrayOutputStream;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.ChatActivity;
import gr.anomologita.anomologita.activities.CommentActivity;
import gr.anomologita.anomologita.activities.CreateGroupActivity;
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
    private RelativeLayout mainButtons;
    private MainAdapter adapter;
    private TextView title;
    private TextView favorite;
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

        final View view = inflater.inflate(R.layout.main_fragment_layout, container, false);
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

        mainButtons = (RelativeLayout) view.findViewById(R.id.mainButtons);
        TextView search = (TextView) view.findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Menu menu = ((MainActivity) getActivity()).getMenu();
                getActivity().onOptionsItemSelected(menu.findItem(R.id.search));
            }
        });
        TextView searchText = (TextView) view.findViewById(R.id.searchText);
        searchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Menu menu = ((MainActivity) getActivity()).getMenu();
                getActivity().onOptionsItemSelected(menu.findItem(R.id.search));
            }
        });
        TextView create = (TextView) view.findViewById(R.id.create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CreateGroupActivity.class);
                getActivity().startActivityForResult(i,2);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            }
        });
        TextView createText = (TextView) view.findViewById(R.id.createText);
        createText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CreateGroupActivity.class);
                getActivity().startActivityForResult(i,2);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            }
        });

        if (groupID == null) {
            mGroupProfileContainer.animate().translationY(-mGroupProfileHeight).setInterpolator(new AccelerateInterpolator(2)).start();
            name.setAlpha(1);
            mainButtons.setVisibility(View.VISIBLE);
        } else {
            mainButtons.setVisibility(View.INVISIBLE);
        }

        adapter = new MainAdapter(this);
        recyclerView.setItemAnimator(animator);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollListener(new HidingGroupProfileListener(mGroupProfileHeight) {
            @Override
            public void onMoved(int distance) {
                mGroupProfileContainer.setTranslationY(-distance);
                name.setAlpha((float) (-mGroupProfileHeight / 2 + mGroupProfileOffset) * (float) 1 / mGroupProfileHeight * 2);
                if (mGroupProfileOffset == mGroupProfileHeight) {
                    favorite.setVisibility(View.INVISIBLE);
                } else {
                    favorite.setVisibility(View.VISIBLE);
                }
                view.clearFocus();
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
        if (Anomologita.isConnected() && Anomologita.getCurrentGroupID() != null && !title.getText().equals(getResources().getString(R.string.deleted)) && !title.getText().equals(getResources().getString(R.string.groupName))) {
            AttemptLogin getPosts = new AttemptLogin();
            getPosts.getPosts(groupID, sort, "0", "20", this);
            getPosts.execute();
            mGroupProfileContainer.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            name.setAlpha(0);
            mainButtons.setVisibility(View.INVISIBLE);
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            mGroupProfileContainer.animate().translationY(-mGroupProfileHeight).setInterpolator(new AccelerateInterpolator(2)).start();
            name.setAlpha(1);
            adapter.removeAll();
            if (Anomologita.isConnected())
                mainButtons.setVisibility(View.VISIBLE);
        }
    }

    public void newComment(Post post) {
        Intent i = new Intent(getActivity(), CommentActivity.class);
        startActivityForResult(i, 1);
        Anomologita.currentPost = post;
        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    private void newMessage(Post post) {
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

    private void editPost(Post post) {
        Intent i = new Intent(getActivity(), EditPostActivity.class);
        i.putExtra("post", post.getPost_txt());
        i.putExtra("location", post.getLocation());
        i.putExtra("postID", String.valueOf(post.getPost_id()));
        startActivityForResult(i, 1);
        getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void adminDialog(final Post post, final View view, int type) {
        boolean wrapInScrollView = true;
        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(post.getHashtagName())
                .customView(R.layout.admin_dialog_layout, wrapInScrollView)
                .negativeText("ΑΚΥΡΟ")
                .negativeColorRes(R.color.primaryColor)
                .show();
        TextView message = (TextView) dialog.getView().findViewById(R.id.message);
        if (type == 1 || type == 3) {
            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newMessage(post);
                    dialog.cancel();
                }
            });
        } else {
            message.setVisibility(View.GONE);
        }
        TextView edit = (TextView) dialog.getView().findViewById(R.id.edit);
        if (type == 1 || type == 2) {
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editPost(post);
                    dialog.cancel();
                }
            });
        } else {
            edit.setVisibility(View.GONE);
        }
        final TextView share = (TextView) dialog.getView().findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share(view);
                dialog.cancel();
            }
        });
    }

    private void share(View v) {
        v.invalidate();
        v.setDrawingCacheEnabled(true);
        Bitmap bitmap = v.getDrawingCache();
        Bitmap bitmap12 = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.ic_launcher);
        Bitmap bitmap1 = overlay(bitmap, bitmap12);
        Uri imageUri = getImageUri(getActivity(), bitmap1);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Κοινοποίησε το ποστ"));
    }

    private static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth() + 10, bmp1.getHeight() + bmp2.getHeight() + 10, bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawColor(Color.RED);
        canvas.drawBitmap(bmp1, 5, bmp2.getHeight() + 5, null);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(Anomologita.convert(18));
        canvas.drawText("Ανομολόγητα Android",bmp2.getWidth()+5,bmp2.getHeight()/2 +10,paint);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }

    private static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(),inImage, "", "");
        return Uri.parse(path);
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