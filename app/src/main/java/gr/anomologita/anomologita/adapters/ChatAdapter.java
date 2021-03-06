package gr.anomologita.anomologita.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import gr.anomologita.anomologita.Anomologita;
import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.objects.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater inflater;
    private List<ChatMessage> messages = new ArrayList<>();

    public ChatAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setMainData(List<ChatMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessage chatMessage) {
        messages.add(chatMessage);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new MeConversationsHolder(inflater.inflate(R.layout.chatbubbleright_layout, parent, false));
        else
            return new OthersConversationsHolder(inflater.inflate(R.layout.chatbubbleleft_layout, parent, false));
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
            meMessagesHolder.meTime.setText(Anomologita.getTime(messages.get(position).getTime(),0));
        } else {
            OthersConversationsHolder othersConversationsHolder = (OthersConversationsHolder) holder;
            othersConversationsHolder.othersTxtMessage.setText(messages.get(position).getMessage());
            othersConversationsHolder.anonymousTime.setText(Anomologita.getTime(messages.get(position).getTime(),0));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MeConversationsHolder extends RecyclerView.ViewHolder {

        final TextView meTxtMessage;
        final TextView meTime;

        public MeConversationsHolder(View itemView) {
            super(itemView);
            meTxtMessage = (TextView) itemView.findViewById(R.id.meTxtId);
            meTime = (TextView) itemView.findViewById(R.id.meTime);
        }
    }

    class OthersConversationsHolder extends RecyclerView.ViewHolder {

        final TextView othersTxtMessage;
        final TextView anonymousTime;

        public OthersConversationsHolder(View itemView) {
            super(itemView);
            othersTxtMessage = (TextView) itemView.findViewById(R.id.othersTxtId);
            anonymousTime = (TextView) itemView.findViewById(R.id.anonymousTime);
        }
    }
}
