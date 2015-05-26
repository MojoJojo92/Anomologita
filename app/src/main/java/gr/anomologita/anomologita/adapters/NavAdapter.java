package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.databases.FavotitesDBHandler;
import gr.anomologita.anomologita.fragments.NavFragment;
import gr.anomologita.anomologita.objects.Favorite;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NavAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater inflater;
    private List<Favorite> data = Collections.emptyList();
    private List<String> titles = Collections.emptyList();
    private Context context;
    private ClickListener clickListener;
    private NavFragment fragmentNav;
    private Favorite currentFavorite;
    private DrawerLayout drawerLayout;
    private View view;

    public NavAdapter(Context context, NavFragment fragmentNav, DrawerLayout drawerLayout) {
        this.context = context;
        this.fragmentNav = fragmentNav;
        inflater = LayoutInflater.from(context);
        this.drawerLayout = drawerLayout;
        this.view = view;
    }

    public void setMainData() {
        FavotitesDBHandler db = new FavotitesDBHandler(context);
        this.data = db.getAllFavorites();
        db.close();
    }

    public  Favorite getData(int position){
        return data.get(position);
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new TitleHolder(inflater.inflate(R.layout.nav_drawer_title_layout, parent, false));
        else
            return new FavoritesHolder(inflater.inflate(R.layout.group_nav_layout, parent, false));
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
            TitleHolder titleHolder = (TitleHolder) holder;
            titleHolder.title.setText(titles.get(0));
        } else {
            FavoritesHolder favoritesHolder = (FavoritesHolder) holder;
            currentFavorite = data.get(position - 1);
            favoritesHolder.title.setText(currentFavorite.get_name());
            Glide.with(context).load("http://anomologita.gr/img/" + data.get(position - 1).getId() + ".png")
                    .asBitmap()
                    .signature(new StringSignature(UUID.randomUUID().toString()))
                    .fitCenter()
                    .into(favoritesHolder.icon);
        }
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return data.size() + titles.size();
    }

    class FavoritesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        ImageView icon;

        public FavoritesHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.groupNameNav);
        //    title.setOnClickListener(this);
            icon = (ImageView) itemView.findViewById(R.id.groupIcon);
          //  icon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.itemClicked(v, getPosition());
            }
        }
    }

    class TitleHolder extends RecyclerView.ViewHolder {
        TextView title;
        public TitleHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.textView5);
        }
    }

    public interface ClickListener {
        public void itemClicked(View view, int position);
    }
}
