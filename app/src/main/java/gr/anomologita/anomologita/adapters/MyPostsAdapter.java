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

import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.CommentActivity;
import gr.anomologita.anomologita.fragments.MyPostsFragment;
import gr.anomologita.anomologita.objects.Post;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MyPostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Post> myPosts = Collections.emptyList();
    private LayoutInflater layoutInflater;
    private Context context;
    private MyPostsFragment fragmentMePosts;

    public MyPostsAdapter(Context context, MyPostsFragment fragmentMePosts) {
        layoutInflater = LayoutInflater.from(context);
        this.fragmentMePosts = fragmentMePosts;
        this.context = context;
    }

    public void setPosts(List<Post> posts) {
        this.myPosts = posts;
        notifyItemRangeChanged(0, this.myPosts.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyPostsHolder(layoutInflater.inflate(R.layout.me_posts_row_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        MyPostsHolder myPostsHolder = (MyPostsHolder) holder;
        final Post currentPost = myPosts.get(position);
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
        myPostsHolder.time.setText(currentPost.getTimestamp());
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
        myPosts.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, this.myPosts.size());
    }

    private String getTime(String postTimeStamp){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Timestamp t2 = new Timestamp(System.currentTimeMillis());
            Date postDate = dateFormat.parse(postTimeStamp);
            Date currentDate = dateFormat.parse(String.valueOf(t2));
            int days = currentDate.getDay() - postDate.getDay();
            int hours = currentDate.getHours() - postDate.getHours();
            int minutes = currentDate.getMinutes() - postDate.getMinutes() + 13;
            if(days > 0){
                if(days== 1)
                    return ("Χθές");
                else
                    return (""+postDate);
            }else if(hours > 0){
                if(hours== 1)
                    return ("1 hr");
                else
                    return (hours+" hrs");
            }else if(minutes > 0){
                return (minutes+" min");
            }else {
                return "τώρα";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "τώρα";
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return myPosts.size();
    }

    class MyPostsHolder extends RecyclerView.ViewHolder {
        private TextView mePostTxt;
        private TextView mePostLikes;
        private TextView mePostComments;
        private TextView time;
        private ImageView deletePost;
        private TextView group_name;

        public MyPostsHolder(View itemView) {
            super(itemView);
            mePostTxt = (TextView) itemView.findViewById(R.id.my_posts_txt);
            time = (TextView) itemView.findViewById(R.id.time);
            mePostLikes = (TextView) itemView.findViewById(R.id.me_posts_likes);
            mePostComments = (TextView) itemView.findViewById(R.id.me_posts_comments);
            deletePost = (ImageView) itemView.findViewById(R.id.deletePost);
            group_name = (TextView) itemView.findViewById(R.id.mePost_groupName);

        }
    }


}
