package gr.anomologita.anomologita.objects;

import android.net.Uri;

public class Favorite {

    private int id;
    private Uri _imageURL;
    private String _name;

    public Favorite(int id, String _name,Uri _imageURL) {
        this._imageURL = _imageURL;
        this._name = _name;
        this.id = id;
    }

    public Uri get_imageURL() {
        return _imageURL;
    }

    public void set_imageURL(Uri _imageURL) {
        this._imageURL = _imageURL;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
