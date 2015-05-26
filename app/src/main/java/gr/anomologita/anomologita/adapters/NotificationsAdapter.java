package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.databases.NotificationDBHandler;
import gr.anomologita.anomologita.objects.Notification;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater inflater;
    private List<Notification> notifications = new ArrayList<>();
    private NotificationDBHandler db;
    private Context context;

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
        for (int i = 0; i < notifications.size(); i++) {
            db.createNotification(notifications.get(i));
        }
        this.notifications = db.getAllNotifications();
        Collections.reverse(this.notifications);
        notifyItemRangeChanged(0, this.notifications.size());
        db.close();
    }

    public Notification getData(int position) {
        return notifications.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NotificationHolder(inflater.inflate(R.layout.notifications_row_layout, parent, false));
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
        if (getItemViewType(position) == 0) {
            notificationHolder.text.setText(currentNotification.getText());
            notificationHolder.time.setText(getTime(currentNotification.getTime()));
            notificationHolder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_fire_red));
        } else if (getItemViewType(position) == 1) {
            notificationHolder.text.setText(currentNotification.getText());
            notificationHolder.time.setText(getTime(currentNotification.getTime()));
            notificationHolder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_comment));
        } else if (getItemViewType(position) == 2) {
            notificationHolder.text.setText(currentNotification.getText());
            notificationHolder.time.setText(getTime(currentNotification.getTime()));
            Glide.with(context).load("http://anomologita.gr/img/" + currentNotification.getId() + ".png")
                    .asBitmap()
                    .signature(new StringSignature(UUID.randomUUID().toString()))
                    .fitCenter()
                    .into(notificationHolder.image);
        }

    }

    private String getTime(String postTimeStamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Timestamp t2 = new Timestamp(System.currentTimeMillis());
            Date postDate = dateFormat.parse(postTimeStamp);
            Date currentDate = dateFormat.parse(String.valueOf(t2));
            int days = currentDate.getDay() - postDate.getDay();
            int hours = currentDate.getHours() - postDate.getHours();
            int minutes = currentDate.getMinutes() - postDate.getMinutes();
            if (days > 0) {
                if (days == 1)
                    return ("Χθές");
                else
                    return ("" + postDate);
            } else if (hours > 0) {
                if (hours == 1)
                    return ("1 hr");
                else
                    return (hours + " hrs");
            } else if (minutes > 0) {
                return (minutes + " min");
            } else {
                return "τώρα";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "τώρα";
    }


    @Override
    public int getItemCount() {
        return notifications.size();
    }


    class NotificationHolder extends RecyclerView.ViewHolder {

        TextView text;
        TextView time;
        ImageView image;

        public NotificationHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.notificationText);
            time = (TextView) itemView.findViewById(R.id.time);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}
