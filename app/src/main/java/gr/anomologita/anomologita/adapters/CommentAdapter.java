package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.CommentActivity;
import gr.anomologita.anomologita.databases.LikesDBHandler;
import gr.anomologita.anomologita.objects.Comment;
import gr.anomologita.anomologita.objects.Post;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater inflater;
    private final Context context;
    private List<Comment> comments = new ArrayList<>();
    private Post post;

    public CommentAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public void setPost(Post post) {
        this.post = post;
        notifyDataSetChanged();
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new PostHolder(inflater.inflate(R.layout.post_layout, parent, false));
        else
            return new CommentHolder(inflater.inflate(R.layout.comment_row_layout, parent, false));
    }

    public int getItemViewType(int position) {
        if (position == 0)
            return 0;
        else
            return 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            final PostHolder postHolder = (PostHolder) holder;
            postHolder.post.setText(post.getPost_txt());
            postHolder.hashtag.setText(post.getHashtagName());
            postHolder.location.setText("(" + post.getLocation() + ")");
            postHolder.postTime.setText(Anomologita.getTime(post.getTimestamp(),16));
            postHolder.numberOfLikes.setText(String.valueOf(post.getLikes()));
            postHolder.numberOfComments.setText(String.valueOf(post.getComments()));
            if (String.valueOf(post.getUser_id()).equals(Anomologita.userID)) {
                postHolder.editPost.setVisibility(View.VISIBLE);
                postHolder.send_personal_message.setVisibility(View.INVISIBLE);
            } else {
                postHolder.editPost.setVisibility(View.INVISIBLE);
                postHolder.send_personal_message.setVisibility(View.VISIBLE);
            }
            if (String.valueOf(post.getUser_id()).equals(Anomologita.userID)) {
                postHolder.editPost.setVisibility(View.INVISIBLE);
                postHolder.send_personal_message.setVisibility(View.INVISIBLE);
                postHolder.messageTextA.setVisibility(View.INVISIBLE);
                postHolder.messageTextB.setVisibility(View.INVISIBLE);
            } else {
                postHolder.editPost.setVisibility(View.INVISIBLE);
                postHolder.send_personal_message.setVisibility(View.VISIBLE);
                postHolder.messageTextA.setText("Προσωπικό");
                postHolder.messageTextB.setText("Μήνυμα");
            }
            postHolder.send_personal_message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CommentActivity) context).newMessage(post);
                }
            });
            postHolder.editPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CommentActivity) context).editPost(post);
                }
            });
            if (post.isLiked())
                postHolder.like.setImageResource(R.drawable.ic_fire_red);
            else
                postHolder.like.setImageResource(R.drawable.ic_fire_grey);
            postHolder.like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (post.isLiked()) {
                        LikesDBHandler db = new LikesDBHandler(Anomologita.getAppContext());
                        db.deleteLike(post.getPost_id());
                        post.setLiked(false);
                        postHolder.like.setImageResource(R.drawable.ic_fire_grey);
                        ((CommentActivity) context).setLike("-1", post);
                        post.setLikes(post.getLikes() - 1);
                        postHolder.numberOfLikes.setText(String.valueOf(post.getLikes()));
                        db.close();
                    } else {
                        LikesDBHandler db = new LikesDBHandler(Anomologita.getAppContext());
                        db.createLikes(post.getPost_id());
                        post.setLiked(true);
                        postHolder.like.setImageResource(R.drawable.ic_fire_red);
                        ((CommentActivity) context).setLike("1", post);
                        post.setLikes(post.getLikes() + 1);
                        postHolder.numberOfLikes.setText(String.valueOf(post.getLikes()));
                        db.close();
                    }
                    Anomologita.refresh = true;
                }
            });
        } else {
            CommentHolder commentHolder = (CommentHolder) holder;
            commentHolder.comment_txt.setText(comments.get(position - 1).getComment());
        }
    }

    @Override
    public int getItemCount() {
        return comments.size() + 1;
    }

    public void setPost(String text){
        post.setPost_txt(text);
        notifyItemChanged(0);
    }

    class CommentHolder extends RecyclerView.ViewHolder {
        final TextView comment_txt;

        public CommentHolder(View itemView) {
            super(itemView);
            comment_txt = (TextView) itemView.findViewById(R.id.txtComment);
        }
    }

    class PostHolder extends RecyclerView.ViewHolder {
        private final TextView post;
        private final TextView hashtag;
        private final TextView postTime;
        private final ImageView send_personal_message;
        private final ImageView like;
        private final ImageView editPost;
        private final TextView numberOfLikes;
        private final TextView numberOfComments;
        private final TextView messageTextA;
        private final TextView messageTextB;
        private final TextView location;

        public PostHolder(View itemView) {
            super(itemView);
            messageTextA = (TextView) itemView.findViewById(R.id.messageTextA);
            messageTextB = (TextView) itemView.findViewById(R.id.messageTextB);
            post = (TextView) itemView.findViewById(R.id.post);
            hashtag = (TextView) itemView.findViewById(R.id.hashTag);
            postTime = (TextView) itemView.findViewById(R.id.time);
            send_personal_message = (ImageView) itemView.findViewById(R.id.message);
            editPost = (ImageView) itemView.findViewById(R.id.edit);
            like = (ImageView) itemView.findViewById(R.id.like);
            numberOfLikes = (TextView) itemView.findViewById(R.id.likeCount);
            numberOfComments = (TextView) itemView.findViewById(R.id.commentCount);
            location = (TextView) itemView.findViewById(R.id.location);
        }
    }
}
