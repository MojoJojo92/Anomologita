package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
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
    private final List<String> users = new ArrayList<>();
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
            postHolder.postTime.setText(Anomologita.getTime(post.getTimestamp(), 16));
            postHolder.numberOfLikes.setText(String.valueOf(post.getLikes()));
            postHolder.numberOfComments.setText(String.valueOf(post.getComments()));
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
            final Comment currentComment = comments.get(position - 1);
            commentHolder.comment_txt.setText(currentComment.getComment() + "   ");
            if (!currentComment.getUserID().isEmpty()) {
                if (currentComment.getUserID().equals(Anomologita.getCurrentGroupUserID())) {
                    SpannableStringBuilder sb = new SpannableStringBuilder("  Admin: " + currentComment.getComment() + "   ");
                    Drawable d = context.getResources().getDrawable(R.drawable.ic_me);
                    d.setBounds(0, 0, Anomologita.convert(15),  Anomologita.convert(15));
                    ImageSpan imagespan = new ImageSpan(d,ImageSpan.ALIGN_BASELINE);
                    sb.setSpan(imagespan,0,1,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    ForegroundColorSpan fcs = new ForegroundColorSpan(context.getResources().getColor(R.color.primaryColor));
                    sb.setSpan(fcs, 0, 9, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    commentHolder.comment_txt.setText(sb);
                } else if (currentComment.getUserID().equals(post.getUser_id())) {
                    SpannableStringBuilder sb = new SpannableStringBuilder("  Δημιουργός ποστ: " + currentComment.getComment() + "   ");
                    Drawable d = context.getResources().getDrawable(R.drawable.ic_me);
                    d.setBounds(0, 0, Anomologita.convert(15),  Anomologita.convert(15));
                    ImageSpan imagespan = new ImageSpan(d,ImageSpan.ALIGN_BASELINE);
                    sb.setSpan(imagespan,0,1,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    ForegroundColorSpan fcs = new ForegroundColorSpan(context.getResources().getColor(R.color.posterColor));
                    sb.setSpan(fcs, 0, 19, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    commentHolder.comment_txt.setText(sb);
                } else if (currentComment.getUserID().equals(Anomologita.userID)) {
                    SpannableStringBuilder sb = new SpannableStringBuilder("  Εγώ: " + currentComment.getComment()+ "   ");
                    Drawable d = context.getResources().getDrawable(R.drawable.ic_me);
                    d.setBounds(0, 0, Anomologita.convert(15),  Anomologita.convert(15));
                    ImageSpan imagespan = new ImageSpan(d,ImageSpan.ALIGN_BASELINE);
                    sb.setSpan(imagespan,0,1,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    ForegroundColorSpan fcs = new ForegroundColorSpan(context.getResources().getColor(R.color.accentColor));
                    sb.setSpan(fcs, 0, 7, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    commentHolder.comment_txt.setText(sb);
                } else {
                    if (!users.contains(currentComment.getUserID()))
                        users.add(currentComment.getUserID());
                    SpannableStringBuilder sb = new SpannableStringBuilder("  Χρήστης " + (users.indexOf(currentComment.getUserID())+1) + ": " + currentComment.getComment()+ "   ");
                    Drawable d = context.getResources().getDrawable(R.drawable.ic_me);
                    d.setBounds(0, 0, Anomologita.convert(15),  Anomologita.convert(15));
                    ImageSpan imagespan = new ImageSpan(d,ImageSpan.ALIGN_BASELINE);
                    sb.setSpan(imagespan,0,1,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    ForegroundColorSpan fcs = new ForegroundColorSpan(Color.BLACK);
                    sb.setSpan(fcs, 0, 13, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    commentHolder.comment_txt.setText(sb);
                }
            }
            if(Anomologita.userID.equals(Anomologita.getCurrentGroupUserID()) || currentComment.getUserID().equals(Anomologita.userID)){
                SpannableStringBuilder sb = new SpannableStringBuilder(commentHolder.comment_txt.getText());
                Drawable d = context.getResources().getDrawable(R.drawable.ic_edit);
                d.setBounds(0, 0, Anomologita.convert(15),  Anomologita.convert(15));
                ImageSpan imagespan = new ImageSpan(d,ImageSpan.ALIGN_BASELINE);
                sb.setSpan(imagespan,sb.length()-1,sb.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                commentHolder.comment_txt.setText(sb);
            }
            commentHolder.comment_txt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Anomologita.userID.equals(Anomologita.getCurrentGroupUserID())) {
                        ((CommentActivity) context).commentDialog(currentComment);
                    } else if (currentComment.getUserID().equals(Anomologita.userID)) {
                        ((CommentActivity) context).commentDialog(currentComment);
                    }
                }
            });
        }
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        notifyItemInserted(comments.size() - 1);
    }

    @Override
    public int getItemCount() {
        return comments.size() + 1;
    }

    public void setPost(String text) {
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
        private final ImageView like;
        private final TextView numberOfLikes;
        private final TextView numberOfComments;
        private final TextView location;

        public PostHolder(View itemView) {
            super(itemView);
            post = (TextView) itemView.findViewById(R.id.post);
            hashtag = (TextView) itemView.findViewById(R.id.hashTag);
            postTime = (TextView) itemView.findViewById(R.id.time);
            like = (ImageView) itemView.findViewById(R.id.like);
            numberOfLikes = (TextView) itemView.findViewById(R.id.likeCount);
            numberOfComments = (TextView) itemView.findViewById(R.id.commentCount);
            location = (TextView) itemView.findViewById(R.id.location);
        }
    }
}
