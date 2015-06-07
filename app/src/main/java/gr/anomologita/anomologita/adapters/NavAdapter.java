package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.databases.FavoritesDBHandler;
import gr.anomologita.anomologita.objects.Favorite;

public class NavAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater inflater;
    private final List<Favorite> favorites = new ArrayList<>();
    private final List<Favorite> myGroups = new ArrayList<>();
    private final Context context;
    private List<Favorite> data = new ArrayList<>();
    private ClickListener clickListener;

    public NavAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setMainData() {
        FavoritesDBHandler db = new FavoritesDBHandler(context);
        this.data = db.getAllFavorites();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getUserID().equals(Anomologita.userID))
                myGroups.add(data.get(i));
            else
                favorites.add(data.get(i));
        }
        db.close();
    }

    public Favorite getData(int position, int viewType) {
        if (viewType == 2)
            return myGroups.get(position - 2);
        else
            return favorites.get(position - myGroups.size() - 3);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0 || viewType == 3)
            return new TitleHolder(inflater.inflate(R.layout.nav_drawer_title_layout, parent, false));
        else
            return new FavoritesHolder(inflater.inflate(R.layout.group_nav_layout, parent, false));
    }

    public int getItemViewType(int position) {
        if (position == 0)
            return 0;
        else if (position == 1)
            return 1;
        else if (position <= myGroups.size() + 1)
            return 2;
        else if (position == myGroups.size() + 2)
            return 3;
        else
            return 4;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Favorite currentFavorite;
        if (getItemViewType(position) == 0) {
            TitleHolder titleHolder = (TitleHolder) holder;
            titleHolder.title.setText("Τα Γκρούπ Μου");
        } else if (getItemViewType(position) == 1) {
            FavoritesHolder favoritesHolder = (FavoritesHolder) holder;
            favoritesHolder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_create));
            favoritesHolder.backgroundIcon.setVisibility(View.INVISIBLE);
            favoritesHolder.subs.setVisibility(View.INVISIBLE);
            favoritesHolder.title.setText("Δημιούργησε Γκρούπ");
        } else if (getItemViewType(position) == 2) {
            FavoritesHolder favoritesHolder = (FavoritesHolder) holder;
            currentFavorite = myGroups.get(position - 2);
            favoritesHolder.title.setText(currentFavorite.get_name());
            favoritesHolder.subCount.setText(createSubs(currentFavorite.getSubs()));
            Glide.with(context).load("http://anomologita.gr/img/" + currentFavorite.getId() + ".png")
                    .asBitmap()
                    .signature(new StringSignature(UUID.randomUUID().toString()))
                    .fitCenter()
                    .into(favoritesHolder.icon);
        } else if (getItemViewType(position) == 3) {
            TitleHolder titleHolder = (TitleHolder) holder;
            titleHolder.title.setText("Αγαπημένα");
        } else {
            FavoritesHolder favoritesHolder = (FavoritesHolder) holder;
            currentFavorite = favorites.get(position - myGroups.size() - 3);
            favoritesHolder.subCount.setText(createSubs(currentFavorite.getSubs()));
            favoritesHolder.title.setText(currentFavorite.get_name());
            Glide.with(context).load("http://anomologita.gr/img/" + currentFavorite.getId() + ".png")
                    .asBitmap()
                    .signature(new StringSignature(UUID.randomUUID().toString()))
                    .fitCenter()
                    .into(favoritesHolder.icon);
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

    @Override
    public int getItemCount() {
        return data.size() + 3;
    }

    class FavoritesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView title;
        final TextView subCount;
        final ImageView icon;
        final ImageView backgroundIcon;
        final LinearLayout subs;

        public FavoritesHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.groupNameNav);
            subCount = (TextView) itemView.findViewById(R.id.subCount);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            backgroundIcon = (ImageView) itemView.findViewById(R.id.circle);
            subs = (LinearLayout) itemView.findViewById(R.id.subs);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.itemClicked(getPosition(), getItemViewType());
            }
        }
    }

    class TitleHolder extends RecyclerView.ViewHolder {
        final TextView title;

        public TitleHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        public void itemClicked(int position, int viewType);
    }
}
