package gr.anomologita.anomologita.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupProfile implements Parcelable{

    private String group_name;
    private int subscribers;
    private String hashtag_name;
    private int group_id;
    private int user_id;
    private String regID;

    public GroupProfile() {

    }

    private GroupProfile(Parcel input){
        group_name = input.readString();
        hashtag_name = input.readString();
    }

    public String getRegID() {
        return regID;
    }

    public void setRegID(String regID) {
        this.regID = regID;
    }

    public String getGroup_name() {
        return group_name;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getGroup_id() {
        return group_id;
    }

    public void setGroup_id(int group_id) {
        this.group_id = group_id;
    }

    public String getGroupName() {
        return group_name;
    }
    public void setGroupName(String name) {
        this.group_name = name;
    }
    public int getSubscribers() {
        return subscribers;
    }
    public void setSubscribers(int subscribers) {
        this.subscribers = subscribers;
    }
    public String getHashtag_name() {
        return hashtag_name;
    }
    public void setHashtag_name(String hashtag_name) {
        this.hashtag_name = hashtag_name;
    }

    @Override
    public String toString() {
        return  "Name: "+ group_name + "Members "+ subscribers + "Hashtag Name: "+ hashtag_name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(group_name);
        dest.writeString(hashtag_name);
    }

    public static final Creator<GroupProfile> CREATOR = new Creator<GroupProfile>(){
        public GroupProfile createFromParcel(Parcel in){
            return new GroupProfile(in);
        }
        public GroupProfile[] newArray(int size){
            return new GroupProfile[size];
        }
    };
}
