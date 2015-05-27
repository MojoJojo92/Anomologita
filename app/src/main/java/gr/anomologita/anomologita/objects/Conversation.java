package gr.anomologita.anomologita.objects;

public class Conversation {

    private int conversationID;
    private String senderRegID;
    private String receiverRegID;
    private String name;
    private String hashtag;
    private String postID;
    private String lastSenderID;
    private String seen;
    private String time;
    private String lastMessage;

    public Conversation(){

    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getLastSenderID() {
        return lastSenderID;
    }

    public void setLastSenderID(String lastSenderID) {
        this.lastSenderID = lastSenderID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public int getConversationID() {
        return conversationID;
    }

    public void setConversationID(int conversationID) {
        this.conversationID = conversationID;
    }

    public String getSenderRegID() {
        return senderRegID;
    }

    public void setSenderRegID(String senderRegID) {
        this.senderRegID = senderRegID;
    }

    public String getReceiverRegID() {
        return receiverRegID;
    }

    public void setReceiverRegID(String receiverRegID) {
        this.receiverRegID = receiverRegID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
