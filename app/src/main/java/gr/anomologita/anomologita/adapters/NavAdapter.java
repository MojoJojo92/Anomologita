package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import gr.anomologita.anomologita.databases.FavotitesDBHandler;
import gr.anomologita.anomologita.fragments.NavFragment;
import gr.anomologita.anomologita.objects.Favorite;

public class NavAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater inflater;
    private List<Favorite> data = new ArrayList<>();
    private List<Favorite> favotites = new ArrayList<>();
    private List<Favorite> myGroups = new ArrayList<>();
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
        for (int i = 0; i < data.size(); i++) {
         //   data.get(i).getSubs();
            if (data.get(i).getUserID().equals(Anomologita.userID))
                myGroups.add(data.get(i));
            else
                favotites.add(data.get(i));
        }
        db.close();
    }

    public Favorite getData(int position) {
        return data.get(position);
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
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
           // Log.e("subbs",currentFavorite.getSubs()+"");
            favoritesHolder.subCount.setText(createSubs(0));
            currentFavorite = favotites.get(position - myGroups.size() - 3);
            favoritesHolder.title.setText(currentFavorite.get_name());
            Glide.with(context).load("http://anomologita.gr/img/" + currentFavorite.getId() + ".png")
                    .asBitmap()
                    .signature(new StringSignature(UUID.randomUUID().toString()))
                    .fitCenter()
                    .into(favoritesHolder.icon);
        }
    }

    private String createSubs(int subs){
        if(subs < 1000){
            return ""+subs;
        }else if(subs < 10000){
            String s = ""+(float)subs/1000;
            s = s.substring(0,1);
            return s+"k";
        }
        return "0";
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return data.size() + 3;
    }

    class FavoritesHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subCount;
        ImageView icon;
        ImageView backgroundIcon;
        LinearLayout subs;

        public FavoritesHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.groupNameNav);
            subCount = (TextView) itemView.findViewById(R.id.subCount);
            icon = (ImageView) itemView.findViewById(R.id.groupIcon);
            backgroundIcon = (ImageView) itemView.findViewById(R.id.circle);
            subs = (LinearLayout) itemView.findViewById(R.id.subs);
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
