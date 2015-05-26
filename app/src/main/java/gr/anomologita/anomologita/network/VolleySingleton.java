package gr.anomologita.anomologita.network;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import gr.anomologita.anomologita.Anomologita;

public class VolleySingleton {

    private static VolleySingleton sInstance = null;
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private VolleySingleton(){
        mRequestQueue = Volley.newRequestQueue(Anomologita.getAppContext());
        mImageLoader = new ImageLoader(mRequestQueue,new ImageLoader.ImageCache() {
            private LruCache<String,Bitmap> cache = new LruCache<>((int)(Runtime.getRuntime().maxMemory()/1024)/8);
            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url,bitmap);
            }
        });
    }

    public static VolleySingleton getsInstance(){
        if (sInstance == null){
            sInstance = new VolleySingleton();
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue(){
        return mRequestQueue;
    }

    public  ImageLoader getImageLoader(){
        return mImageLoader;
    }
}
