package gr.anomologita.anomologita.extras;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import gr.anomologita.anomologita.R;
import gr.anomologita.anomologita.objects.BadgeDrawable;

public class Utils {

    // give your server registration url here
    public static final String APP_SERVER_URL = "http://anomologita.gr/GCM.php";

    // Google Project Number
    public static final String GOOGLE_PROJECT_ID = "1079412359695";
    public static final String MESSAGE_KEY = "message";

    public static void setBadgeCount(Context context, LayerDrawable icon, int count) {

        BadgeDrawable badge;

        // Reuse drawable if possible
        Drawable reuse = icon.findDrawableByLayerId(R.id.ic_badge);
        if (reuse != null && reuse instanceof BadgeDrawable) {
            badge = (BadgeDrawable) reuse;
        } else {
            badge = new BadgeDrawable(context);
        }

        badge.setCount(count);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_badge, badge);
    }
}
