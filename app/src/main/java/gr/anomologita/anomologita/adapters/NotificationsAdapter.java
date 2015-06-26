package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.NotificationActivity;
import gr.anomologita.anomologita.databases.NotificationDBHandler;
import gr.anomologita.anomologita.objects.Notification;
import me.grantland.widget.AutofitHelper;

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater inflater;
    private final Context context;
    private List<Notification> notifications = new ArrayList<>();
    private NotificationDBHandler db;

    public NotificationsAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        db = new NotificationDBHandler(context);
        notifications = db.getAllNotifications();
        Collections.reverse(notifications);
        db.close();
    }

    public void setMainData(List<Notification> notifications) {
        db = new NotificationDBHandler(context);
        for (int i = 0; i < notifications.size(); i++)
            db.createNotification(notifications.get(i));
        this.notifications = db.getAllNotifications();
        Collections.reverse(this.notifications);
        notifyItemRangeChanged(0, this.notifications.size());
        db.close();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NotificationHolder(inflater.inflate(R.layout.notification_row_layout, parent, false));
    }

    public int getItemViewType(int position) {
        if(position != notifications.size()){
            if (notifications.get(position).getType().equals("like"))
                return 0;
            else if (notifications.get(position).getType().equals("comment"))
                return 1;
            else if (notifications.get(position).getType().equals("subscribe"))
                return 2;
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NotificationHolder notificationHolder = (NotificationHolder) holder;
        final int currentPosition = position;
        if (position == notifications.size()) {
            notificationHolder.time.setVisibility(View.INVISIBLE);
            notificationHolder.text.setVisibility(View.INVISIBLE);
            notificationHolder.delete.setVisibility(View.INVISIBLE);
            notificationHolder.image.setVisibility(View.INVISIBLE);
        } else {
            final Notification currentNotification = notifications.get(position);
            notificationHolder.time.setVisibility(View.VISIBLE);
            notificationHolder.text.setVisibility(View.VISIBLE);
            notificationHolder.delete.setVisibility(View.VISIBLE);
            notificationHolder.image.setVisibility(View.VISIBLE);
            notificationHolder.time.setText(Anomologita.getTime(currentNotification.getTime(), 0));
            AutofitHelper.create(notificationHolder.text);
            notificationHolder.text.setText(currentNotification.getText());
            notificationHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteData(currentPosition);
                }
            });
            if (getItemViewType(position) == 0) {
                notificationHolder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_fire_big));
                notificationHolder.text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((NotificationActivity) context).postClick(currentNotification);
                    }
                });
            }else if (getItemViewType(position) == 1) {
                notificationHolder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_comment_big));
                notificationHolder.text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((NotificationActivity) context).postClick(currentNotification);
                    }
                });
            }else if (getItemViewType(position) == 2) {
                notificationHolder.text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((NotificationActivity) context).groupClick(currentNotification);
                    }
                });
                Glide.with(context).load("http://anomologita.gr/img/" + currentNotification.getId() + ".png")
                        .asBitmap()
                        .signature(new StringSignature(UUID.randomUUID().toString()))
                        .fitCenter()
                        .into(notificationHolder.image);
            }
        }
    }

    private void deleteData(int position) {
        NotificationDBHandler db = new NotificationDBHandler(context);
        db.deleteNotification(notifications.get(position));
        db.close();
        if (notifications.size() != 0) {
            notifications.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(0, this.notifications.size());
        } else {
            notifications = new ArrayList<>();
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size() + 1;
    }

    class NotificationHolder extends RecyclerView.ViewHolder {

        final TextView text;
        final TextView time;
        final ImageView image;
        final ImageView delete;

        public NotificationHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.notificationText);
            time = (TextView) itemView.findViewById(R.id.time);
            image = (ImageView) itemView.findViewById(R.id.image);
            delete = (ImageView) itemView.findViewById(R.id.delete);
        }
    }
}
