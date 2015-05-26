package gr.anomologita.anomologita.logging;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Tzoleto on 31/3/2015.
 */
public class L {
    public static void m(String size, String message) {
        Log.d("M2", "" + message);
    }

    public static void t (Context context, String message) {
        Toast.makeText(context, message + "",Toast.LENGTH_SHORT).show();
    }

    public static void T (Context context, String message) {
        Toast.makeText(context, message + "",Toast.LENGTH_LONG).show();
    }
}
