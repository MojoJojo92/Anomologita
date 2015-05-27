package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.objects.ChatMessage;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater inflater;
    private List<ChatMessage> messages = new ArrayList<>();
   // private Context context;

    public ChatAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setMainData(List<ChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
        //notifyItemRangeChanged(0, messages.size());
    }

    public  void addMessage(ChatMessage chatMessage){
        messages.add(chatMessage);
        notifyItemInserted(messages.size()-1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new MeConversationsHolder(inflater.inflate(R.layout.chatbubbleleft_layout, parent, false));
        else
            return new OthersConversationsHolder(inflater.inflate(R.layout.chatbubbleright_layout, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        if (String.valueOf(messages.get(position).getSenderID()).equals(Anomologita.userID))
            return 0;
        else
            return 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            MeConversationsHolder meMessagesHolder = (MeConversationsHolder) holder;
            meMessagesHolder.meTxtMessage.setText(messages.get(position).getMessage());
            meMessagesHolder.meTime.setText(getTime(messages.get(position).getTime()));
        } else {
            OthersConversationsHolder othersConversationsHolder = (OthersConversationsHolder) holder;
            othersConversationsHolder.othersTxtMessage.setText(messages.get(position).getMessage());
            othersConversationsHolder.anonymousTime.setText(getTime(messages.get(position).getTime()));
        }
    }

    private String getTime(String postTimeStamp){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        try {
            Timestamp t2 = new Timestamp(System.currentTimeMillis());
            Date postDate = dateFormat.parse(postTimeStamp);
            Date currentDate = dateFormat.parse(String.valueOf(t2));
            int days = currentDate.getDay() - postDate.getDay();
            int hours = currentDate.getHours() - postDate.getHours();
            int minutes = currentDate.getMinutes() - postDate.getMinutes();
            if(days > 0){
                if(days== 1)
                    return ("Χθές");
                else
                    return (""+postDate);
            }else if(hours > 0){
                if(hours== 1)
                    return ("1 hr");
                else
                    return (hours+" hrs");
            }else if(minutes > 0){
                    return (minutes+" min");
            }else {
                return "τώρα";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "τώρα";
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MeConversationsHolder extends RecyclerView.ViewHolder {

        TextView meTxtMessage;
        TextView meTime;

        public MeConversationsHolder(View itemView) {
            super(itemView);
            meTxtMessage = (TextView) itemView.findViewById(R.id.meTxtId);
            meTime = (TextView) itemView.findViewById(R.id.meTime);
        }
    }

    class OthersConversationsHolder extends RecyclerView.ViewHolder {

        TextView othersTxtMessage;
        TextView anonymousTime;

        public OthersConversationsHolder(View itemView) {
            super(itemView);
            othersTxtMessage = (TextView) itemView.findViewById(R.id.othersTxtId);
            anonymousTime = (TextView) itemView.findViewById(R.id.anonymousTime);
        }
    }
}
