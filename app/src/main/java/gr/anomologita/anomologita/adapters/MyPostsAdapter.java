package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.fragments.MyPostsFragment;
import gr.anomologita.anomologita.objects.Post;

public class MyPostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private final MyPostsFragment fragmentMePosts;
    private List<Post> posts = Collections.emptyList();

    public MyPostsAdapter(Context context, MyPostsFragment fragmentMePosts) {
        layoutInflater = LayoutInflater.from(context);
        this.fragmentMePosts = fragmentMePosts;
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
        if (position == posts.size()) {
            myPostsHolder.postsLayout.setVisibility(View.INVISIBLE);
        } else {
            myPostsHolder.postsLayout.setVisibility(View.VISIBLE);
            final Post currentPost = posts.get(position);
            myPostsHolder.mePostLikes.setText(String.valueOf(currentPost.getLikes()));
            myPostsHolder.group_name.setText(currentPost.getGroup_name());
            myPostsHolder.time.setText(Anomologita.getTime(currentPost.getTimestamp()));
            myPostsHolder.mePostComments.setText(String.valueOf(currentPost.getComments()));
            myPostsHolder.mePostTxt.setText(currentPost.getPost_txt());
            myPostsHolder.mePostTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragmentMePosts.comment(currentPost);
                }
            });
            myPostsHolder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragmentMePosts.edit(currentPost);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return posts.size() + 1;
    }

    class MyPostsHolder extends RecyclerView.ViewHolder {
        private final TextView mePostTxt;
        private final TextView mePostLikes;
        private final TextView mePostComments;
        private final TextView time;
        private final ImageView edit;
        private final TextView group_name;
        private final RelativeLayout postsLayout;

        public MyPostsHolder(View itemView) {
            super(itemView);
            mePostTxt = (TextView) itemView.findViewById(R.id.post);
            time = (TextView) itemView.findViewById(R.id.time);
            mePostLikes = (TextView) itemView.findViewById(R.id.likeCount);
            mePostComments = (TextView) itemView.findViewById(R.id.commentCount);
            edit = (ImageView) itemView.findViewById(R.id.editPost);
            group_name = (TextView) itemView.findViewById(R.id.groupName);
            postsLayout = (RelativeLayout) itemView.findViewById(R.id.myPostsRowLayout);
        }
    }
}
