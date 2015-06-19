package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.signature.StringSignature;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        return new SearchHolder(inflater.inflate(R.layout.group_nav_layout, parent, false));
    }

    public int getItemViewType(int position) {
        if (position == 0)
            return 0;
        else
            return 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SearchHolder searchHolder = (SearchHolder) holder;
        if(getItemViewType(position) == 0){
            searchHolder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_create_grey));
            searchHolder.layout.setVisibility(View.INVISIBLE);
            searchHolder.background.setVisibility(View.INVISIBLE);
            searchHolder.title.setText(Html.fromHtml("<b><u>Δημιούργησε Γκρούπ</u></b>"));
        }else {
            Favorite current = groupSearches.get(position -1);
            searchHolder.background.setVisibility(View.VISIBLE);
            searchHolder.layout.setVisibility(View.VISIBLE);
            searchHolder.title.setText(current.get_name());
            searchHolder.subsCount.setText(createSubs(current.getSubs()));
            BitmapPool pool = Glide.get(context).getBitmapPool();
            Glide.with(context)
                    .load("http://anomologita.gr/img/" + current.getId() + ".png").asBitmap()
                    .transform(new CropCircleTransformation(pool))
                    .signature(new StringSignature(UUID.randomUUID().toString()))
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
        private final LinearLayout layout;
        private final ImageView background;

        public SearchHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.groupNameNav);
            subsCount = (TextView) itemView.findViewById(R.id.subCount);
            layout = (LinearLayout) itemView.findViewById(R.id.subs);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            background = (ImageView) itemView.findViewById(R.id.circle);
            title.setTextColor(context.getResources().getColor(R.color.primaryColorDark));
            subsCount.setTextColor(context.getResources().getColor(R.color.accentColor));
            subsCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star, 0, 0, 0);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null)
                clickListener.itemClicked(getPosition() - 1);
        }
    }

    public interface ClickListener {
        public void itemClicked(int position);
    }
}
