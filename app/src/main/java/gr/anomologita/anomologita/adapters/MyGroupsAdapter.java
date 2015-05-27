package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.fragments.MyGroupsFragment;
import gr.anomologita.anomologita.objects.GroupProfile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.signature.StringSignature;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import me.grantland.widget.AutofitHelper;

public class MyGroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<GroupProfile> groups = Collections.emptyList();
    private LayoutInflater layoutInflater;
    private MyGroupsFragment fragmentMeGroups;
    private View view;
    private Context context;

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
        final GroupProfile currentGroup = groups.get(position);
        AutofitHelper.create(groupsHolder.group_name);
        groupsHolder.group_name.setText(currentGroup.getGroupName());
        groupsHolder.subscribers.setText(String.valueOf(currentGroup.getSubscribers()));
        groupsHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentMeGroups.deleteGroup(position, currentGroup);
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

    public void deleteData(int tempPosition) {
        groups.remove(tempPosition);
        notifyItemRemoved(tempPosition);
        Glide.clear(view);
        view.invalidate();
        view.postInvalidate();
        notifyItemRangeChanged(0, this.groups.size());
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    class MyGroupsHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView group_name;
        TextView subscribers;
        ImageView delete;


        public MyGroupsHolder(View itemView) {
            super(itemView);
            group_name = (TextView) itemView.findViewById(R.id.myGroupNameProfile);
            subscribers = (TextView) itemView.findViewById(R.id.subscribers);
            delete = (ImageView) itemView.findViewById(R.id.edit);
            icon = (ImageView) itemView.findViewById(R.id.groupIcon);
        }
    }
}