package gr.anomologita.anomologita.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.CommentActivity;
import gr.anomologita.anomologita.fragments.MyPostsFragment;
import gr.anomologita.anomologita.objects.Post;

public class MyPostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private final Context context;
    private final MyPostsFragment fragmentMePosts;
    private List<Post> posts = Collections.emptyList();

    public MyPostsAdapter(Context context, MyPostsFragment fragmentMePosts) {
        layoutInflater = LayoutInflater.from(context);
        this.fragmentMePosts = fragmentMePosts;
        this.context = context;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyItemRangeChanged(0, this.posts.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyPostsHolder(layoutInflater.inflate(R.layout.myposts_row_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        MyPostsHolder myPostsHolder = (MyPostsHolder) holder;
        final Post currentPost = posts.get(position);
        myPostsHolder.mePostTxt.setText(currentPost.getPost_txt());
        myPostsHolder.mePostTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, CommentActivity.class);
                i.putExtra("postID", currentPost.getPost_id());
                i.putExtra("post_name", currentPost.getPost_txt());
                i.putExtra("hashtag", currentPost.getHashtagName());
                i.putExtra("where", currentPost.getLocation());
                i.putExtra("numberOfLikes", currentPost.getLikes());
                i.putExtra("numberOfComments", currentPost.getComments());
                context.startActivity(i);
            }
        });
        myPostsHolder.mePostLikes.setText(String.valueOf(currentPost.getLikes()));
        myPostsHolder.group_name.setText(currentPost.getGroup_name());
        myPostsHolder.time.setText(Anomologita.getTime(currentPost.getTimestamp()));
        myPostsHolder.mePostComments.setText(String.valueOf(currentPost.getComments()));
        myPostsHolder.deletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                fragmentMePosts.deletePost(String.valueOf(currentPost.getPost_id()), position);
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
        });
    }

    public void deleteData(int position) {
        if (posts.size() != 0) {
            posts.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(0, this.posts.size());
        } else {
            posts = new ArrayList<>();
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class MyPostsHolder extends RecyclerView.ViewHolder {
        private final TextView mePostTxt;
        private final TextView mePostLikes;
        private final TextView mePostComments;
        private final TextView time;
        private final ImageView deletePost;
        private final TextView group_name;

        public MyPostsHolder(View itemView) {
            super(itemView);
            mePostTxt = (TextView) itemView.findViewById(R.id.post);
            time = (TextView) itemView.findViewById(R.id.time);
            mePostLikes = (TextView) itemView.findViewById(R.id.likeCount);
            mePostComments = (TextView) itemView.findViewById(R.id.commentCount);
            deletePost = (ImageView) itemView.findViewById(R.id.deletePost);
            group_name = (TextView) itemView.findViewById(R.id.groupName);
        }
    }
}
