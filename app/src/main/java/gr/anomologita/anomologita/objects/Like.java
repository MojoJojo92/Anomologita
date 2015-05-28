package gr.anomologita.anomologita.objects;

public class Like {

    private final int post_id;
    private final int liked;

    public Like(int post_id, int liked) {
        this.post_id = post_id;
        this.liked = liked;
    }

    public int getPost_id() {
        return post_id;
    }

    public int isLiked() {
        return liked;
    }
}
