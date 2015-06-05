package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.signature.StringSignature;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.fragments.MyGroupsFragment;
import gr.anomologita.anomologita.objects.GroupProfile;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import me.grantland.widget.AutofitHelper;

public class MyGroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private final MyGroupsFragment fragmentMeGroups;
    private final View view;
    private final Context context;
    private List<GroupProfile> groups = Collections.emptyList();

    public MyGroupsAdapter(Context context, MyGroupsFragment fragmentMeGroups, View view) {
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.fragmentMeGroups = fragmentMeGroups;
        this.view = view;
    }

    public void setGroups(List<GroupProfile> groups) {
        this.groups = groups;
        notifyItemRangeChanged(0, this.groups.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyGroupsHolder(layoutInflater.inflate(R.layout.mygroups_row_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final MyGroupsHolder groupsHolder = (MyGroupsHolder) holder;
        if (position == groups.size()) {
            groupsHolder.groupsLayout.setVisibility(View.INVISIBLE);
        } else {
            groupsHolder.groupsLayout.setVisibility(View.VISIBLE);
            final GroupProfile currentGroup = groups.get(position);
            AutofitHelper.create(groupsHolder.group_name);
            groupsHolder.group_name.setText(currentGroup.getGroupName());
            groupsHolder.subscribers.setText(String.valueOf(currentGroup.getSubscribers()));
            groupsHolder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragmentMeGroups.edit(currentGroup);
                }
            });
            BitmapPool pool = Glide.get(context).getBitmapPool();
            Glide.clear(view);
            Glide.with(context)
                    .load("http://anomologita.gr/img/" + String.valueOf(groups.get(position).getGroup_id()) + ".png").asBitmap()
                    .signature(new StringSignature(UUID.randomUUID().toString()))
                    .transform(new CropCircleTransformation(pool))
                    .into(groupsHolder.icon);
            groupsHolder.group_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragmentMeGroups.show(currentGroup);
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
        return groups.size() + 1;
    }

    class MyGroupsHolder extends RecyclerView.ViewHolder {

        final ImageView icon;
        final TextView group_name;
        final TextView subscribers;
        final ImageView edit;
        final RelativeLayout groupsLayout;


        public MyGroupsHolder(View itemView) {
            super(itemView);
            group_name = (TextView) itemView.findViewById(R.id.myGroupNameProfile);
            subscribers = (TextView) itemView.findViewById(R.id.subscribers);
            groupsLayout = (RelativeLayout) itemView.findViewById(R.id.myGroupsRowLayout);
            edit = (ImageView) itemView.findViewById(R.id.edit);
            icon = (ImageView) itemView.findViewById(R.id.groupIcon);
        }
    }
}