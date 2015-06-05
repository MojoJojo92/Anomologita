package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.objects.Favorite;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater inflater;
    private final Context context;
    private List<Favorite> groupSearches = new ArrayList<>();
    private ClickListener clickListener;

    public SearchAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public void setMainData(List<Favorite> groupSearches) {
        this.groupSearches = groupSearches;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1)
            return new AdHolder(inflater.inflate(R.layout.ad_layout, parent, false));
        else
            return new SearchHolder(inflater.inflate(R.layout.group_nav_layout, parent, false));
    }

    public int getItemViewType(int position) {
        if (position == groupSearches.size())
            return 1;
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == 1) {
            AdHolder adHolder = (AdHolder) holder;
            AdRequest adRequest = new AdRequest.Builder().build();
            adHolder.ad.loadAd(adRequest);
        } else {
            SearchHolder searchHolder = (SearchHolder) holder;
            Favorite current = groupSearches.get(position);
            searchHolder.title.setText(current.get_name());
            searchHolder.subsCount.setText(createSubs(current.getSubs()));
            BitmapPool pool = Glide.get(context).getBitmapPool();
            Glide.with(context)
                    .load("http://anomologita.gr/img/" + current.getId() + ".png").asBitmap()
                    .transform(new CropCircleTransformation(pool))
                    .into(searchHolder.icon);
        }
    }

    private String createSubs(int subs) {
        if (subs < 1000) {
            return "" + subs;
        } else if (subs < 10000) {
            String s = "" + (float) subs / 1000;
            s = s.substring(0, 1);
            return s + "k";
        }
        return "0";
    }


    public Favorite getData(int position) {
        return groupSearches.get(position);
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return groupSearches.size() + 1;
    }

    class SearchHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView title;
        private final TextView subsCount;
        private final ImageView icon;
        //  private final LinearLayout subsLayout;

        public SearchHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.groupNameNav);
            subsCount = (TextView) itemView.findViewById(R.id.subCount);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            //   subsLayout = (LinearLayout) itemView.findViewById(R.id.subs);

            title.setTextColor(context.getResources().getColor(R.color.primaryColorDark));
            subsCount.setTextColor(context.getResources().getColor(R.color.accentColor));
            subsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star, 0, 0, 0);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null)
                clickListener.itemClicked(getPosition());
        }
    }

    class AdHolder extends RecyclerView.ViewHolder {
        private final AdView ad;
      //  private final LinearLayout adLayout;

        public AdHolder(View itemView) {
            super(itemView);
            ad = (AdView) itemView.findViewById(R.id.adView);
       //     adLayout = (LinearLayout) itemView.findViewById((R.id.adLayout));
        }
    }

    public interface ClickListener {
        public void itemClicked(int position);
    }
}
