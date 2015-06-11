package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.activities.ConversationsActivity;
import gr.anomologita.anomologita.databases.PostsDBHandler;
import gr.anomologita.anomologita.objects.Conversation;
import me.grantland.widget.AutofitHelper;

public class ConversationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater inflater;
    private final Context context;
    private List<Conversation> conversations = new ArrayList<>();

    public ConversationsAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public void setMainData(List<Conversation> conversations) {
        this.conversations = conversations;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConversationsHolder(inflater.inflate(R.layout.conversation_row_layout, parent, false));
    }

    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ConversationsHolder conversationsHolder = (ConversationsHolder) holder;
        final int currentPosition = position;
        if(position == conversations.size()){
            conversationsHolder.layout.setVisibility(View.INVISIBLE);
        }else {
            conversationsHolder.layout.setVisibility(View.VISIBLE);
            final Conversation currentCon = conversations.get(position);
            AutofitHelper.create(conversationsHolder.senderName);
            PostsDBHandler db = new PostsDBHandler(context);
            if (db.exists(Integer.parseInt(currentCon.getPostID())))
                conversationsHolder.senderName.setText("Με " + currentCon.getName() + " στο " + currentCon.getHashtag());
            else
                conversationsHolder.senderName.setText("Με Ανώνυμο στο " + currentCon.getHashtag());
            Log.e("test", currentCon.getLastSenderID()+ " "+ Anomologita.userID);
            if ((String.valueOf(currentCon.getLastSenderID()).equals(Anomologita.userID))) {
                conversationsHolder.lastSenderName.setText("Εγώ: ");
            }else {
                if (db.exists(Integer.parseInt(currentCon.getPostID())))
                    conversationsHolder.lastSenderName.setText(currentCon.getName() + ": ");
                else
                    conversationsHolder.lastSenderName.setText("Ανώνυμος: ");
            }
            AutofitHelper.create(conversationsHolder.txtMessage);
            if (currentCon.getLastMessage().length() < 30)
                conversationsHolder.txtMessage.setText(currentCon.getLastMessage());
            else
                conversationsHolder.txtMessage.setText(currentCon.getLastMessage().substring(0, 30) + "...");
            conversationsHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ConversationsActivity)context).delete(currentCon.getConversationID(), currentPosition);
                }
            });
            conversationsHolder.senderName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ConversationsActivity)context).selected(currentCon);
                }
            });
            conversationsHolder.conLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ConversationsActivity)context).selected(currentCon);
                }
            });
            if (currentCon.getSeen().equals("no")) {
                conversationsHolder.lastSenderName.setTextColor(context.getResources().getColor(R.color.primaryColor));
                conversationsHolder.txtMessage.setTextColor(context.getResources().getColor(R.color.primaryColor));
                conversationsHolder.senderName.setTextColor(context.getResources().getColor(R.color.primaryColor));
            }else {
                conversationsHolder.lastSenderName.setTextColor(context.getResources().getColor(R.color.secondaryTextColor));
                conversationsHolder.txtMessage.setTextColor(context.getResources().getColor(R.color.secondaryTextColor));
                conversationsHolder.senderName.setTextColor(context.getResources().getColor(R.color.secondaryTextColor));
            }
            conversationsHolder.time.setText(Anomologita.getTime(currentCon.getTime(), 0));
        }
    }

    @Override
    public int getItemCount() {
        return conversations.size() + 1;
    }

    public void deleteData(int position) {
        if (conversations.size() != 0) {
            conversations.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(0, this.conversations.size());
        } else {
            conversations = new ArrayList<>();
            notifyDataSetChanged();
        }
    }

    class ConversationsHolder extends RecyclerView.ViewHolder {
        final TextView lastSenderName;
        final TextView senderName;
        final TextView txtMessage;
        final TextView time;
        final ImageView delete;
        final RelativeLayout layout;
        final LinearLayout conLayout;

        public ConversationsHolder(View itemView) {
            super(itemView);
            lastSenderName = (TextView) itemView.findViewById(R.id.lastSenderName);
            senderName = (TextView) itemView.findViewById(R.id.conversationTitle);
            txtMessage = (TextView) itemView.findViewById(R.id.txtMessageName);
            time = (TextView) itemView.findViewById(R.id.time);
            delete = (ImageView) itemView.findViewById(R.id.delete);
            layout = (RelativeLayout) itemView.findViewById(R.id.conRowLayout);
            conLayout = (LinearLayout) itemView.findViewById(R.id.conLayout);
        }
    }
}
