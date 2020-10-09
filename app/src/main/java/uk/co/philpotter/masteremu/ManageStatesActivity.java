/* MasterEmu manage states screen source code file
   copyright Phil Potter, 2019 */

package uk.co.philpotter.masteremu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * This class acts as the manage states screen of the app.
 */
public class ManageStatesActivity extends Activity {

    // define instance variables
    private ControllerSelection selectionObj;
    private long timeSinceLastAnaloguePress = 0;
    private ControllerTextView managestates_wipe_states_button;

    /**
     * This method creates the manage states screen.
     */
    @SuppressWarnings("deprecation") @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.managestates_activity);
        setButtonBehaviors();
    }
    private void setButtonBehaviors(){
        Button import_states_button = findViewById(R.id.managestates_import_states_button);
        Button export_states_button = findViewById(R.id.managestates_export_states_button);
        Button wipe_states_button = findViewById(R.id.managestates_wipe_states_button);

        import_states_button.setOnClickListener(new ManageStatesButtonBehaviors());
        export_states_button.setOnClickListener(new ManageStatesButtonBehaviors());
        wipe_states_button.setOnClickListener(new ManageStatesButtonBehaviors());
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
                if (selectionObj != null) {
                    if (selectionObj.getSelected() != managestates_wipe_states_button) {
                        selectionObj.activate();
                    }
                    else {
                        // create dialogue
                        AlertDialog wipeMenu = new AlertDialog.Builder(ManageStatesActivity.this).create();
                        wipeMenu.setTitle("Wipe Prompt");
                        wipeMenu.setMessage("Are you sure you want to wipe all save states and saves?");
                        ManageStatesActivity.WipeListener wl = new ManageStatesActivity.WipeListener();
                        wipeMenu.setButton(DialogInterface.BUTTON_POSITIVE, "YES", wl);
                        wipeMenu.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", wl);
                        wipeMenu.show();
                    }
                }
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

    /**
     * This shows a message.
     * @param message
     */
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * This is the listener which handles wiping of all states.
     */
    private class WipeListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    StateIO manager = new StateIO();
                    boolean result = manager.deleteAllStates(getFilesDir().getAbsolutePath());

                    // check success/failure
                    if (result) {
                        Toast success = Toast.makeText(ManageStatesActivity.this, "Wiped states successfully", Toast.LENGTH_SHORT);
                        success.show();
                    } else {
                        Toast failure = Toast.makeText(ManageStatesActivity.this, "Couldn't wipe states", Toast.LENGTH_SHORT);
                        failure.show();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    ManageStatesActivity.this.showMessage("Wipe of states was cancelled");
                    break;
            }
        }
    }

    class ManageStatesButtonBehaviors implements View.OnClickListener{
        @Override public void onClick(View view) {

            switch (view.getId()){
                case R.id.managestates_import_states_button:
                    Intent import_states_intent = new Intent(ManageStatesActivity.this, FileBrowserActivity.class);
                    import_states_intent.putExtra("actionType", "import_states");
                    startActivity(import_states_intent);
                    break;
                case R.id.managestates_export_states_button:
                    Intent export_states_intent = new Intent(ManageStatesActivity.this, FileBrowserActivity.class);
                    export_states_intent.putExtra("actionType", "export_states");
                    startActivity(export_states_intent);
                    break;
                case R.id.managestates_wipe_states_button:
                    AlertDialog wipeMenu = new AlertDialog.Builder(ManageStatesActivity.this).create();
                    wipeMenu.setTitle("Wipe Prompt");
                    wipeMenu.setMessage("Are you sure you want to wipe all save states and saves?");
                    ManageStatesActivity.WipeListener wl = new ManageStatesActivity.WipeListener();
                    wipeMenu.setButton(DialogInterface.BUTTON_POSITIVE, "YES", wl);
                    wipeMenu.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", wl);
                    wipeMenu.show();
                    break;
                default:
                    finish();
            }
        }
    }
}
