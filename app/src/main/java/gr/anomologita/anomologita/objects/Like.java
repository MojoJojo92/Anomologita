package gr.anomologita.anomologita.objects;

/**
 * Created by Fotis on 26-Apr-15.
 */
public class Like {

    private int post_id;
    private int liked;

    public Like(int post_id, int liked) {
        this.post_id = post_id;
        this.liked = liked;
    }

    public int getPost_id() {
        return post_id;
    }

    public void setPost_id(int post_id) {
        this.post_id = post_id;
    }

    public int isLiked() {
        return liked;
    }

    public void setLiked(int liked) {
        this.liked = liked;
    }
}
