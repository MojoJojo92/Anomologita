package gr.anomologita.anomologita.adapters;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMRequest;

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
        else if (viewType == 1)
            return new AdHolder(layoutInflater.inflate(R.layout.ad_layout, parent, false));
        else
            return new PostHolder(layoutInflater.inflate(R.layout.post_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 1) {
            AdHolder adHolder = (AdHolder) holder;
            MMRequest request = new MMRequest();
            request.setAge("25");
            request.setEthnicity(MMRequest.ETHNICITY_WHITE);
            request.setEducation(MMRequest.EDUCATION_BACHELORS);
            adHolder.adRelativeLayout.setMMRequest(request);
            adHolder.adRelativeLayout.getAd();
            if (position > previousPosition)
                animateAd(adHolder, true);
            else
                animateAd(adHolder, false);
            previousPosition = position;
        } else if (getItemViewType(position) == 2) {
            int adCount;
            if (position % 10 >= 5)
                adCount = position / 10 + 1;
            else
                adCount = position / 10;
            final int currentPosition = position - 1 - adCount;
            final PostHolder postHolder = (PostHolder) holder;
            final Post currentPost = posts.get(currentPosition);
            postHolder.post.setText(currentPost.getPost_txt());
            if (currentPost.getUser_id().equals(Anomologita.getCurrentGroupUserID()))
                postHolder.admin.setVisibility(View.VISIBLE);
            else
                postHolder.admin.setVisibility(View.INVISIBLE);
            postHolder.hashtag.setText(currentPost.getHashtagName());
            postHolder.location.setText("(" + currentPost.getLocation() + ")");
            postHolder.postTime.setText(Anomologita.getTime(currentPost.getTimestamp(), 16));
            if (currentPost.isLiked())
                postHolder.like.setImageResource(R.drawable.ic_fire_red);
            else
                postHolder.like.setImageResource(R.drawable.ic_fire_grey);
            postHolder.like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Anomologita.isConnected()){
                        if (currentPost.isLiked()) {
                            LikesDBHandler db = new LikesDBHandler(Anomologita.getAppContext());
                            db.deleteLike(currentPost.getPost_id());
                            posts.get(currentPosition).setLiked(false);
                            currentPost.setLiked(false);
                            postHolder.like.setImageResource(R.drawable.ic_fire_grey);
                            mainFragment.setLike("-1", currentPost);
                            currentPost.setLikes(currentPost.getLikes() - 1);
                            postHolder.numberOfLikes.setText(String.valueOf(currentPost.getLikes()));
                            db.close();
                        } else {
                            LikesDBHandler db = new LikesDBHandler(Anomologita.getAppContext());
                            db.createLikes(currentPost.getPost_id());
                            posts.get(currentPosition).setLiked(true);
                            currentPost.setLiked(true);
                            postHolder.like.setImageResource(R.drawable.ic_fire_red);
                            mainFragment.setLike("1", currentPost);
                            currentPost.setLikes(currentPost.getLikes() + 1);
                            postHolder.numberOfLikes.setText(String.valueOf(currentPost.getLikes()));
                            db.close();
                        }
                        Anomologita.refresh = true;
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
            postHolder.adminIcon.setVisibility(View.VISIBLE);
            if (Anomologita.userID.equals(Anomologita.getCurrentGroupUserID()) && !String.valueOf(currentPost.getUser_id()).equals(Anomologita.userID)) {
                postHolder.adminIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainFragment.adminDialog(currentPost, postHolder.postRowLayout, 1);
                    }
                });
            } else if (String.valueOf(currentPost.getUser_id()).equals(Anomologita.userID)) {
                postHolder.adminIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainFragment.adminDialog(currentPost, postHolder.postRowLayout, 2);
                    }
                });
            } else {
                postHolder.adminIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainFragment.adminDialog(currentPost, postHolder.postRowLayout, 3);
                    }
                });
            }
            if (posts.size() - 1 == position - adCount)
                mainFragment.loadMore(position + 1 - adCount);
            if (position != 1) {
                if (position > previousPosition)
                    animatePost(postHolder, true);
                else
                    animatePost(postHolder, false);
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
        else if (position % 10 == 5)
            return 1;
        else
            return 2;
    }

    @Override
    public int getItemCount() {
        if ((posts.size() + 1) % 10 >= 5)
            return (posts.size() + 1 + (posts.size() + 1) / 10 + 1);
        else
            return (posts.size() + 1 + (posts.size() + 1) / 10);
    }

    private void animatePost(PostHolder postHolder, Boolean down) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(postHolder.postRowLayout, "translationY", down ? offset : -offset, 0);
        objectAnimator.setDuration(1000);
        objectAnimator.start();
    }

    private void animateAd(AdHolder adHolder, Boolean down) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(adHolder.adRelativeLayout, "translationY", down ? offset : -offset, 0);
        objectAnimator.setDuration(1000);
        objectAnimator.start();
    }

    class PostHolder extends RecyclerView.ViewHolder {
        private final TextView post;
        private final TextView hashtag;
        private final TextView postTime;
        private final TextView location;
        private final ImageView comments_word;
        private final ImageView like;
        private final ImageView adminIcon;
        private final TextView numberOfLikes;
        private final TextView numberOfComments;
        private final RelativeLayout postRowLayout;
        private final TextView admin;

        public PostHolder(View itemView) {
            super(itemView);
            post = (TextView) itemView.findViewById(R.id.post);
            post.setMovementMethod(LinkMovementMethod.getInstance());
            hashtag = (TextView) itemView.findViewById(R.id.hashTag);
            postTime = (TextView) itemView.findViewById(R.id.time);
            location = (TextView) itemView.findViewById(R.id.location);
            comments_word = (ImageView) itemView.findViewById(R.id.comment);
            like = (ImageView) itemView.findViewById(R.id.like);
            adminIcon = (ImageView) itemView.findViewById(R.id.adminIcon);
            numberOfLikes = (TextView) itemView.findViewById(R.id.likeCount);
            numberOfComments = (TextView) itemView.findViewById(R.id.commentCount);
            postRowLayout = (RelativeLayout) itemView.findViewById(R.id.postLayout);
            admin = (TextView) itemView.findViewById(R.id.admin);
        }
    }

    class AdHolder extends RecyclerView.ViewHolder {
        private final MMAdView adRelativeLayout;

        public AdHolder(View itemView) {
            super(itemView);
            adRelativeLayout = (MMAdView) itemView.findViewById(R.id.adView);
        }
    }

    class SpaceHolder extends RecyclerView.ViewHolder {
        public SpaceHolder(View itemView) {
            super(itemView);
        }
    }
}
