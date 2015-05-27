package gr.anomologita.anomologita.objects;

public class Comment {
    private int postID;
    private String comment;

    public void setCommentID(int commentID) {
        int commentID1 = commentID;
    }

    public int getPostID() {
        return postID;
    }

    public void setPostID(int postID) {
        this.postID = postID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
