/* MasterEmu start (and former ad consent) screen source code file
   copyright Phil Potter, 2019 */

package uk.co.philpotter.masteremu;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.view.View;

/**
 * This class acts as the start screen of the app, just passing through.
 */
public class StartActivity extends Activity {

    /**
     * This method just starts the rest of the app now.
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override public void run() {
                goToTitleScreen();
            }
        }, 2000);
    }
    @Override protected void onResume(){
        super.onResume();
        View decorView = getWindow().getDecorView();
        int opt = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LOW_PROFILE;
        decorView.setSystemUiVisibility(opt);
    }

    private void goToTitleScreen(){
        Intent titlescreenActivity = new Intent(this, TitlescreenActivity.class);
        titlescreenActivity.putExtra("startingApp", true);
        startActivity(titlescreenActivity);
        finish();
    }
}