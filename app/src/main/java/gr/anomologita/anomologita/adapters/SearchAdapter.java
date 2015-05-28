package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.objects.GroupSearch;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater inflater;
    private final Context context;
    private List<GroupSearch> groupSearches = new ArrayList<>();
    private ClickListener clickListener;

    public SearchAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public void setMainData(List<GroupSearch> groupSearches) {
        this.groupSearches = groupSearches;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SearchHolder(inflater.inflate(R.layout.group_nav_layout, parent, false));
    }

    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        SearchHolder searchHolder = (SearchHolder) holder;
        searchHolder.title.setText(groupSearches.get(position).getTitle());
        BitmapPool pool = Glide.get(context).getBitmapPool();
        Glide.with(context)
                .load("http://anomologita.gr/img/"+ groupSearches.get(position).getGroupID()+".png").asBitmap()
                .transform(new CropCircleTransformation(pool))
                .into(searchHolder.icon);
    }

    public GroupSearch  getData(int position){
        return groupSearches.get(position);
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return groupSearches.size();
    }

    class SearchHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView title;
        final ImageView icon;

        public SearchHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.groupNameNav);
            title.setOnClickListener(this);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            icon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
           if (clickListener != null) {
               clickListener.itemClicked(getPosition());
           }
        }
    }

    public interface ClickListener {
        public void itemClicked(int position);
    }
}
