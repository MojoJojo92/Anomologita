package gr.anomologita.anomologita.extras;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class NotifyService extends Service {

    private Context context;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public NotifyService(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void getNotification(){

    }
}
