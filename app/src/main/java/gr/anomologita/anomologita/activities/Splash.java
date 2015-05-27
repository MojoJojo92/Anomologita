package gr.anomologita.anomologita.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import gr.anomologita.anomologita.R;

public class Splash extends Activity {

    private final int SPLASH_DISPLAY_LENGTH = 5000;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_screen_layout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}