package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.objects.Comment;
import gr.anomologita.anomologita.objects.Post;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater inflater;
    private List<Comment> comments = new ArrayList<>();
    private Post post;
    private Context context;

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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == 0) {
            PostHolder postHolder = (PostHolder) holder;
            postHolder.post.setText(post.getPost_txt() + " (" + post.getLocation() + ") ");
            postHolder.hashtag.setText(post.getHashtagName());
            postHolder.postTime.setText(getTime(post.getTimestamp()));
            postHolder.numberOfLikes.setText(String.valueOf(post.getLikes()));
            postHolder.numberOfComments.setText(String.valueOf(post.getComments()));
            if (String.valueOf(post.getUser_id()).equals(Anomologita.userID)) {
                postHolder.editPost.setVisibility(View.VISIBLE);
                postHolder.send_personal_message.setVisibility(View.INVISIBLE);
            } else {
                postHolder.editPost.setVisibility(View.INVISIBLE);
                postHolder.send_personal_message.setVisibility(View.VISIBLE);
            }
            if (post.isLiked())
                postHolder.like.setImageResource(R.drawable.ic_action_fire_red);
            else
                postHolder.like.setImageResource(R.drawable.ic_action_fire_grey);
        } else {
            CommentHolder commentHolder = (CommentHolder) holder;
            commentHolder.comment_txt.setText(comments.get(position - 1).getComment());
        }
    }

    private String getTime(String postTimeStamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Timestamp t2 = new Timestamp(System.currentTimeMillis());
            Date postDate = dateFormat.parse(postTimeStamp);
            Date currentDate = dateFormat.parse(String.valueOf(t2));
            int days = currentDate.getDay() - postDate.getDay();
            int hours = currentDate.getHours() - postDate.getHours();
            int minutes = currentDate.getMinutes() - postDate.getMinutes() + 13;
            if (days > 0) {
                if (days == 1)
                    return ("Χθές");
                else
                    return ("" + postDate);
            } else if (hours > 0) {
                if (hours == 1)
                    return ("1 hr");
                else
                    return (hours + " hrs");
            } else if (minutes > 0) {
                return (minutes + " min");
            } else {
                return "τώρα";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "τώρα";
    }

    @Override
    public int getItemCount() {
        return comments.size() + 1;
    }

    class CommentHolder extends RecyclerView.ViewHolder {
        TextView comment_txt;

        public CommentHolder(View itemView) {
            super(itemView);
            comment_txt = (TextView) itemView.findViewById(R.id.txtComment);
        }
    }

    class PostHolder extends RecyclerView.ViewHolder {
        private TextView post;
        private TextView hashtag;
        private TextView postTime;
        private ImageView comments_word;
        private ImageView send_personal_message;
        private ImageView like;
        private ImageView editPost;
        private TextView numberOfLikes;
        private TextView numberOfComments;
        private LinearLayout postRowLayout;

        public PostHolder(View itemView) {
            super(itemView);
            post = (TextView) itemView.findViewById(R.id.post);
            hashtag = (TextView) itemView.findViewById(R.id.hashTag);
            postTime = (TextView) itemView.findViewById(R.id.time);
            comments_word = (ImageView) itemView.findViewById(R.id.comment);
            send_personal_message = (ImageView) itemView.findViewById(R.id.message);
            editPost = (ImageView) itemView.findViewById(R.id.edit);
            like = (ImageView) itemView.findViewById(R.id.like);
            numberOfLikes = (TextView) itemView.findViewById(R.id.likeCount);
            numberOfComments = (TextView) itemView.findViewById(R.id.commentCount);
            postRowLayout = (LinearLayout) itemView.findViewById(R.id.postLayout);
        }
    }
}
