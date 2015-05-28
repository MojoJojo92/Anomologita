package gr.anomologita.anomologita.adapters;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.databases.LikesDBHandler;
import gr.anomologita.anomologita.fragments.MainFragment;
import gr.anomologita.anomologita.objects.Post;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Post> posts = new ArrayList<>();
    private final LayoutInflater layoutInflater;
    private final MainFragment mainFragment;
    private final int offset = Anomologita.convert(100);
    private int previousPosition;

    public MainAdapter(MainFragment fragmentNew) {
        layoutInflater = LayoutInflater.from(fragmentNew.getActivity());
        this.mainFragment = fragmentNew;
    }

    public void setPostList(List<Post> posts) {
        this.posts.addAll(posts);
        notifyItemRangeChanged(this.posts.size() - posts.size(), this.posts.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new SpaceHolder(layoutInflater.inflate(R.layout.space_layout, parent, false));
        else
            return new PostHolder(layoutInflater.inflate(R.layout.post_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) != 0) {
            final PostHolder postHolder = (PostHolder) holder;
            final Post currentPost = posts.get(position - 1);
            postHolder.post.setText(currentPost.getPost_txt());
            postHolder.location.setText("(" + currentPost.getLocation() + ")");
            postHolder.hashtag.setText(currentPost.getHashtagName());
            postHolder.postTime.setText(Anomologita.getTime(currentPost.getTimestamp()));
            if (currentPost.isLiked())
                postHolder.like.setImageResource(R.drawable.ic_fire_red);
            else
                postHolder.like.setImageResource(R.drawable.ic_fire_grey);
            postHolder.like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentPost.isLiked()) {
                        LikesDBHandler db = new LikesDBHandler(Anomologita.getAppContext());
                        db.deleteLike(currentPost.getPost_id());
                        posts.get(position - 1).setLiked(false);
                        currentPost.setLiked(false);
                        postHolder.like.setImageResource(R.drawable.ic_fire_grey);
                        mainFragment.setLike("-1", currentPost);
                        currentPost.setLikes(currentPost.getLikes() - 1);
                        postHolder.numberOfLikes.setText(String.valueOf(currentPost.getLikes()));
                        db.close();
                    } else {
                        LikesDBHandler db = new LikesDBHandler(Anomologita.getAppContext());
                        db.createLikes(currentPost.getPost_id());
                        posts.get(position - 1).setLiked(true);
                        currentPost.setLiked(true);
                        postHolder.like.setImageResource(R.drawable.ic_fire_red);
                        mainFragment.setLike("1", currentPost);
                        currentPost.setLikes(currentPost.getLikes() + 1);
                        postHolder.numberOfLikes.setText(String.valueOf(currentPost.getLikes()));
                        db.close();
                    }
                }
            });
            postHolder.numberOfLikes.setText(String.valueOf(currentPost.getLikes()));
            postHolder.comments_word.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainFragment.newComment(currentPost);
                }
            });
            postHolder.numberOfComments.setText(String.valueOf(currentPost.getComments()));
            postHolder.numberOfComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainFragment.newComment(currentPost);
                }
            });
            postHolder.send_personal_message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainFragment.newMessage(currentPost);
                }
            });
            postHolder.editPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainFragment.editPost(currentPost);
                }
            });
            if (String.valueOf(currentPost.getUser_id()).equals(Anomologita.userID)) {
                postHolder.editPost.setVisibility(View.VISIBLE);
                postHolder.send_personal_message.setVisibility(View.INVISIBLE);
            } else {
                postHolder.editPost.setVisibility(View.INVISIBLE);
                postHolder.send_personal_message.setVisibility(View.VISIBLE);
            }
            if (position == posts.size())
                mainFragment.loadMore(position);
            if (position != 1) {
                if (position > previousPosition)
                    animate(postHolder, true);
                else
                    animate(postHolder, false);
            }
            previousPosition = position;
        }
    }

    public void removeAll() {
        posts.removeAll(posts);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return 0;
        else
            return 1;
    }

    @Override
    public int getItemCount() {
        return (posts.size() + 1);
    }

    private void animate(PostHolder postHolder, Boolean down) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(postHolder.postRowLayout, "translationY", down ? offset : -offset, 0);
        objectAnimator.setDuration(1000);
        objectAnimator.start();
    }

    class PostHolder extends RecyclerView.ViewHolder {
        private final TextView post;
        private final TextView hashtag;
        private final TextView postTime;
        private final TextView location;
        private final ImageView comments_word;
        private final ImageView send_personal_message;
        private final ImageView like;
        private final ImageView editPost;
        private final TextView numberOfLikes;
        private final TextView numberOfComments;
        private final RelativeLayout postRowLayout;

        public PostHolder(View itemView) {
            super(itemView);
            post = (TextView) itemView.findViewById(R.id.post);
            hashtag = (TextView) itemView.findViewById(R.id.hashTag);
            postTime = (TextView) itemView.findViewById(R.id.time);
            location = (TextView) itemView.findViewById(R.id.location);
            comments_word = (ImageView) itemView.findViewById(R.id.comment);
            send_personal_message = (ImageView) itemView.findViewById(R.id.message);
            editPost = (ImageView) itemView.findViewById(R.id.edit);
            like = (ImageView) itemView.findViewById(R.id.like);
            numberOfLikes = (TextView) itemView.findViewById(R.id.likeCount);
            numberOfComments = (TextView) itemView.findViewById(R.id.commentCount);
            postRowLayout = (RelativeLayout) itemView.findViewById(R.id.postLayout);
        }
    }

    class SpaceHolder extends RecyclerView.ViewHolder {
        public SpaceHolder(View itemView) {
            super(itemView);
        }
    }
}
