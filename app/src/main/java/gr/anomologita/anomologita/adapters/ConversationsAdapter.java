package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.ConversationsActivity;
import gr.anomologita.anomologita.objects.Conversation;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import me.grantland.widget.AutofitHelper;

public class ConversationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater inflater;
    private List<Conversation> Conversations = Collections.emptyList();
    private Context context;
    private ConversationsActivity conversationsActivity;

    public ConversationsAdapter(Context context, ConversationsActivity conversationsActivity) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.conversationsActivity = conversationsActivity;
    }

    public void setMainData(List<Conversation> conversations) {
        this.Conversations = conversations;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConversationsHolder(inflater.inflate(R.layout.conversations_row_layout, parent, false));
    }

    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ConversationsHolder conversationsHolder = (ConversationsHolder) holder;
        final Conversation currentCon = Conversations.get(position);
        if (!Conversations.get(position).getName().equals("Εγώ"))
            conversationsHolder.senderName.setText(currentCon.getName());
        if ((String.valueOf(currentCon.getLastSenderID()).equals(Anomologita.getCurrentGroupID())))
            conversationsHolder.lastSenderName.setText(currentCon.getName() + ": ");
        else
            conversationsHolder.lastSenderName.setText("Εγώ: ");
        AutofitHelper.create(conversationsHolder.postHashtag);
        conversationsHolder.postHashtag.setText(currentCon.getHashtag());
        AutofitHelper.create(conversationsHolder.txtMessage);
        if (currentCon.getLastMessage().length() < 30)
            conversationsHolder.txtMessage.setText(currentCon.getLastMessage());
        else
            conversationsHolder.txtMessage.setText(currentCon.getLastMessage().substring(0,30) + "...");
        conversationsHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conversationsActivity.selected(position, currentCon);
            }
        });
      /*  conversationsHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conversationsActivity.delete(currentCon.getConversation_id(), position);
            }
        });*/
        if (currentCon.getSeen().equals("yes")) {
            conversationsHolder.lastSenderName.setTextColor(context.getResources().getColor(R.color.primaryColor));
            conversationsHolder.txtMessage.setTextColor(context.getResources().getColor(R.color.primaryColor));
        }
        conversationsHolder.time.setText(getTime(currentCon.getTime()));
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
        return Conversations.size();
    }

    public void deleteData(int position) {
        Conversations.remove(position);
        notifyItemRemoved(position);
    }


    class ConversationsHolder extends RecyclerView.ViewHolder {
        TextView lastSenderName;
        TextView senderName;
        TextView postHashtag;
        TextView txtMessage;
        TextView time;
        ImageView delete;
        LinearLayout conLayout;
        RelativeLayout conRowLayout;
        LinearLayout chatClick;

        public ConversationsHolder(View itemView) {
            super(itemView);
            conLayout = (LinearLayout) itemView.findViewById(R.id.conLayout);
            conRowLayout = (RelativeLayout) itemView.findViewById(R.id.conRowLayout);
            chatClick = (LinearLayout) itemView.findViewById(R.id.chatClick);
            lastSenderName = (TextView) itemView.findViewById(R.id.lastSenderName);
            senderName = (TextView) itemView.findViewById(R.id.senderName);
            postHashtag = (TextView) itemView.findViewById(R.id.postHashtagName);
            txtMessage = (TextView) itemView.findViewById(R.id.txtMessageName);
            time = (TextView) itemView.findViewById(R.id.lastMessageTime);
            delete = (ImageView) itemView.findViewById(R.id.deleteConversation);
        }
    }
}
