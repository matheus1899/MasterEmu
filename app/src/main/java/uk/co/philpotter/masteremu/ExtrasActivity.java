/* MasterEmu extras screen source code file
   copyright Phil Potter, 2019 */

package uk.co.philpotter.masteremu;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class acts as the extras screen of the app.
 */
public class ExtrasActivity extends Activity {
    // define instance variables
    private ControllerSelection selectionObj;
    private long timeSinceLastAnaloguePress = 0;
    private Button btn_WebSite;
    private Button btn_GitHub_SourceCode;

    /**
     * This method creates the extras screen.
     */
    @SuppressWarnings("deprecation") @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extras_activity);
        setButtonsBehaviors();

        StringBuilder extrasText = new StringBuilder();
        String line = new String();
        BufferedReader bufferedStream = null;

        try {
            bufferedStream = new BufferedReader(new InputStreamReader(getAssets().open("extras.txt")));

            while ((line = bufferedStream.readLine()) != null) {
                extrasText.append(line);
                extrasText.append('\n');
            }
        }
        catch (IOException e) {
            Log.e("ExtrasActivity:", "Encountered error reading extras file: " + e.toString());
        }
        finally {
            try {
                bufferedStream.close();
            }
            catch (IOException e) {
                Log.e("ExtrasActivity:", "Encountered error closing extras file: " + e.toString());
            }
        }

        TextView extrasView = findViewById(R.id.extras_view);
        extrasView.setText(extrasText);
    }

    /**
     * This method currently just keeps the orientation locked if necessary.
     */
    @Override protected void onStart() {
        super.onStart();
        // make sure screen orientation is set here if locked
        if (OptionStore.orientation_lock) {
            if (OptionStore.orientation.equals("portrait")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (OptionStore.orientation.equals("landscape")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    private void setButtonsBehaviors(){
        btn_WebSite = findViewById(R.id.extras_btn_phil_website);
        btn_GitHub_SourceCode = findViewById(R.id.extras_btn_github_sourcecode);

        btn_WebSite.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View view) {
                Intent pottersoft_intent = new Intent(Intent.ACTION_VIEW);
                String url = getResources().getString(R.string.pottersoft_url);
                pottersoft_intent.setData(Uri.parse(url));
                startActivity(pottersoft_intent);
            }
        });

        btn_GitHub_SourceCode.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View view) {
                Intent github_intent = new Intent(Intent.ACTION_VIEW);
                String url = getResources().getString(R.string.github_url);
                github_intent.setData(Uri.parse(url));
                startActivity(github_intent);
            }
        });
    }
    /**
     * This method helps us to detect gamepad events and do the right thing with them.
     */
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        int action, keycode;
        boolean returnVal = false;
        action = event.getAction();
        keycode = event.getKeyCode();
        if (action == KeyEvent.ACTION_DOWN) {
            if (keycode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (selectionObj != null)
                    selectionObj.moveDown();
                returnVal = true;
            } else if (keycode == KeyEvent.KEYCODE_DPAD_UP) {
                if (selectionObj != null)
                    selectionObj.moveUp();
                returnVal = true;
            } else if (keycode == KeyEvent.KEYCODE_BUTTON_A) {
                if (selectionObj != null)
                    selectionObj.activate();
                returnVal = true;
            } else if (keycode == KeyEvent.KEYCODE_BUTTON_B) {
                finish();
                returnVal = true;
            }
        }

        if (returnVal)
            return returnVal;

        return super.dispatchKeyEvent(event);
    }

    /**
     * This method deals with input from the analogue stick and some d-pads.
     */
    public boolean motionEvent(MotionEvent event) {
        boolean returnVal = true;

        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            InputDevice dev = event.getDevice();
            if ((event.getSource() & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) {
                float y = event.getAxisValue(MotionEvent.AXIS_HAT_Y);

                if (y == -1.0f) {
                    if (selectionObj != null)
                        selectionObj.moveUp();
                } else if (y == 1.0f) {
                    if (selectionObj != null)
                        selectionObj.moveDown();
                }
            } else if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
                InputDevice.MotionRange yRange = dev.getMotionRange(MotionEvent.AXIS_Y);
                float yHat = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
                if (yRange == null)
                    return true;
                else {
                    if (event.getEventTime() - timeSinceLastAnaloguePress > 85) {
                        timeSinceLastAnaloguePress = event.getEventTime();
                        float yVal = event.getAxisValue(MotionEvent.AXIS_Y);
                        if (Math.abs(yVal) <= yRange.getMax() / 2)
                            yVal = 0;

                        if (yVal < 0 || yHat < 0) {
                            if (selectionObj != null)
                                selectionObj.moveUp();
                        } else if (yVal > 0 || yHat > 0) {
                            if (selectionObj != null)
                                selectionObj.moveDown();
                        }
                    }
                }
            }
        }


        return returnVal;
    }
}
