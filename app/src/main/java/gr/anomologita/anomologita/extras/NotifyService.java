package gr.anomologita.anomologita.extras;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotifyService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public NotifyService() {
    }

}
