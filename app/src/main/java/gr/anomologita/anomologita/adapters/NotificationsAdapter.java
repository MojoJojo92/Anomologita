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
import gr.anomologita.anomologita.databases.NotificationDBHandler;
import gr.anomologita.anomologita.objects.Notification;

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
        if (notifications.get(position).getType().equals("like"))
            return 0;
        else if (notifications.get(position).getType().equals("comment"))
            return 1;
        else if (notifications.get(position).getType().equals("subscribe"))
            return 2;
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Notification currentNotification = notifications.get(position);
        NotificationHolder notificationHolder = (NotificationHolder) holder;
        notificationHolder.time.setText(Anomologita.getTime(currentNotification.getTime()));
        notificationHolder.text.setText(currentNotification.getText());
        if (getItemViewType(position) == 0)
            notificationHolder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_fire_big));
        else if (getItemViewType(position) == 1)
            notificationHolder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_comment));
        else if (getItemViewType(position) == 2)
            Glide.with(context).load("http://anomologita.gr/img/" + currentNotification.getId() + ".png")
                    .asBitmap()
                    .signature(new StringSignature(UUID.randomUUID().toString()))
                    .fitCenter()
                    .into(notificationHolder.image);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationHolder extends RecyclerView.ViewHolder {

        final TextView text;
        final TextView time;
        final ImageView image;

        public NotificationHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.notificationText);
            time = (TextView) itemView.findViewById(R.id.time);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}
