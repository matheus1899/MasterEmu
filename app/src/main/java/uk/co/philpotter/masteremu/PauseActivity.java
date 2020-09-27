/* MasterEmu pause screen source code file
   copyright Phil Potter, 2019 */

package uk.co.philpotter.masteremu;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.view.KeyEvent;
import android.widget.Toast;
import java.util.Date;
import java.util.Calendar;
import android.view.InputDevice;
import android.util.Log;

/**
 * This class acts as the pause screen of the app.
 */
public class PauseActivity extends Activity {

    // define instance variables
    private ControllerSelection selectionObj;
    private long timeSinceLastAnaloguePress = 0;
    private static final int sleepTime = 300;

    // native methods to call
    public native void saveStateStub(long emulatorContainerPointer, String fileName);
    public native void quitStub(long emulatorContainerPointer);
    public native void resizeAndAudioStub(long emulatorContainerPointer);

    /**
     * This method sets the screen orientation when locked.
     */
    @Override protected void onStart() {
        super.onStart();
        if (OptionStore.orientation_lock) {
            if (OptionStore.orientation.equals("portrait")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (OptionStore.orientation.equals("landscape")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    /**
     * This returns to the title screen for us when back is pressed.
     */
    @Override public void onBackPressed() {
        quitStub(getContainerPointer());
        try {
            Thread.sleep(sleepTime);
        }
        catch (Exception e) {
            Log.e("PauseActivity", "Couldn't sleep thread");
        }
        finish();
    }

    /**
     * This method saves the extra bundle and allows us to handle screen
     * reorientations properly.
     */
    @Override public void onSaveInstanceState(Bundle savedInstanceState) {
        // transfer mappings
        Bundle extra = getIntent().getBundleExtra("MAIN_BUNDLE");
        savedInstanceState.putAll(extra);
    }

    /**
     * This method creates the pause screen.
     */
    @SuppressWarnings("deprecation")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pause_activity);
        setButtonBehaviors();
        // restore bundle if necessary
        if (savedInstanceState != null) {
            Bundle b = new Bundle();
            b.putAll(savedInstanceState);
            getIntent().putExtra("MAIN_BUNDLE", b);
        }
    }
    private long getContainerPointer(){
        return ((0xFFFFFFFF00000000L & (getHigherAddress() << 32)) | (0xFFFFFFFFL & getLowerAddress()));
    }
    private long getLowerAddress(){
        return getIntent().getBundleExtra("MAIN_BUNDLE").getInt("lowerAddress");
    }
    private long getHigherAddress(){
        return getIntent().getBundleExtra("MAIN_BUNDLE").getInt("higherAddress");
    }
    private void setButtonBehaviors(){
        Button pause_resume_button = findViewById(R.id.pause_resume_button);
        Button pause_loadstate_button = findViewById(R.id.pause_loadstate_button);
        Button pause_savestate_button = findViewById(R.id.pause_savestate_button);
        Button pause_exit_button = findViewById(R.id.pause_exit_button);
        PauseButtonBehaviors listener = new PauseButtonBehaviors();
        pause_resume_button.setOnClickListener(listener);
        pause_loadstate_button.setOnClickListener(listener);
        pause_savestate_button.setOnClickListener(listener);
        pause_exit_button.setOnClickListener(listener);
        pause_resume_button.requestFocus();
    }
    public void saveStateSucceeded() {
        Toast.makeText(PauseActivity.this, "Successfully saved state", Toast.LENGTH_SHORT).show();
    }
    public void saveStateFailed() {
        Toast.makeText(PauseActivity.this, "Failed to save state", Toast.LENGTH_SHORT).show();
    }
    public void handleButton(View view) {
        // This is where we deal with the button presses.
        long emulatorContainerPointer;
        switch(view.getId()){
            case R.id.pause_resume_button:
                finish();
                break;
            case R.id.pause_loadstate_button:
                Intent loadStateIntent = new Intent(this, StateBrowserActivity.class);
                Bundle b = getIntent().getBundleExtra("MAIN_BUNDLE");
                loadStateIntent.putExtra("MAIN_BUNDLE", b);
                startActivity(loadStateIntent);
                break;
            case R.id.pause_savestate_button:
                emulatorContainerPointer = getContainerPointer();
                String fileName = getStateFileName();
                saveStateStub(emulatorContainerPointer, fileName);
                break;
            default:
                quitStub(getContainerPointer());
                try {
                    Thread.sleep(sleepTime);
                }
                catch (Exception e) {
                    Log.e("PauseActivity", "Couldn't sleep thread");
                }
                finish();
                break;
        }
    }
    private String getStateFileName(){
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        StringBuilder saveStateFileName = new StringBuilder();
        saveStateFileName.append(calendar.get(Calendar.YEAR));
        saveStateFileName.append('-');
        saveStateFileName.append(calendar.get(Calendar.MONTH) + 1);
        saveStateFileName.append('-');
        saveStateFileName.append(calendar.get(Calendar.DAY_OF_MONTH));
        saveStateFileName.append('_');
        saveStateFileName.append(calendar.get(Calendar.HOUR_OF_DAY));
        saveStateFileName.append('-');
        saveStateFileName.append(calendar.get(Calendar.MINUTE));
        saveStateFileName.append('-');
        saveStateFileName.append(calendar.get(Calendar.SECOND));
        saveStateFileName.append(".mesav");
        return saveStateFileName.toString();
    }
    /**
     * This method helps us to detect gamepad events and do the right thing with them.
     */
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        int action, keycode;
        action = event.getAction();
        keycode = event.getKeyCode();
        boolean returnVal = false;

        if (action == KeyEvent.ACTION_DOWN) {
            if (keycode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (selectionObj != null) {
                    selectionObj.moveDown();
                }
                returnVal = true;
            }
            else if (keycode == KeyEvent.KEYCODE_DPAD_UP) {
                if (selectionObj != null){
                    selectionObj.moveUp();
                }
                returnVal = true;
            }
            else if (keycode == KeyEvent.KEYCODE_BUTTON_A) {
                if (selectionObj != null) {
                    ControllerTextView cv = (ControllerTextView)selectionObj.getSelected();
                    if (cv != null) {
                        cv.unHighlight();
                        handleButton(cv);
                    }
                }
                returnVal = true;
            }
            else if (keycode == KeyEvent.KEYCODE_BUTTON_B) {
                quitStub(getContainerPointer());
                try {
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    Log.e("PauseActivity", "Couldn't sleep thread");
                }
                finish();
                returnVal = true;
            }
        }

        if (returnVal) {
            return returnVal;
        }
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
    class PauseButtonBehaviors implements View.OnClickListener{
        @Override public void onClick(View view) {
            handleButton(view);
        }
    }
}
