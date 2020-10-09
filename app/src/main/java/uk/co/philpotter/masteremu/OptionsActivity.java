/* MasterEmu options screen source code file
   copyright Phil Potter, 2019 */

package uk.co.philpotter.masteremu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.content.res.Configuration;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.FileWriter;
import android.widget.Toast;
import android.content.pm.ActivityInfo;
import android.view.InputDevice;

//This class acts as the options screen of the app.
public class OptionsActivity extends Activity {

    // define instance variables
    //private ControllerSelection selectionObj;
    //private long timeSinceLastAnaloguePress = 0;

    //This method creates the options screen.
    @SuppressWarnings("deprecation") @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options_activity);
        Button btn_apply = findViewById(R.id.options_btn_apply);
        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applySettings();
            }
        });
    }
    /**
     * This method restores the checkbox states amongst other things.
     */
    @Override protected void onStart(){
        super.onStart();
        if (OptionStore.orientation_lock) {
            CheckBox orientation_lock = findViewById(R.id.options_cb_lock_screen);
            orientation_lock.setChecked(true);
        }
        if (OptionStore.disable_sound) {
            CheckBox disable_sound = findViewById(R.id.options_cb_sound);
            disable_sound.setChecked(true);
        }
        if (OptionStore.larger_buttons) {
            CheckBox larger_buttons = findViewById(R.id.options_cb_larger_controllers);
            larger_buttons.setChecked(true);
        }
        if (OptionStore.no_buttons) {
            CheckBox no_buttons = findViewById(R.id.options_cb_touch_button_overlay);
            no_buttons.setChecked(true);
        }
        if (OptionStore.japanese_mode) {
            CheckBox japanese_mode = findViewById(R.id.options_cb_japanese_mode);
            japanese_mode.setChecked(true);
        }
        if (OptionStore.no_stretching) {
            CheckBox no_stretching = findViewById(R.id.options_cb_screen_stretching);
            no_stretching.setChecked(true);
        }
        if (OptionStore.game_genie) {
            CheckBox game_genie = findViewById(R.id.options_cb_enable_gg_action_replay);
            game_genie.setChecked(true);
        }
        // make sure screen orientation is set here if locked
        if (OptionStore.orientation_lock) {
            if (OptionStore.orientation.equals("portrait")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (OptionStore.orientation.equals("landscape")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }
    public void applySettings() {
        // define variables
        StringBuilder settings = new StringBuilder();
        CheckBox orientation_lock = findViewById(R.id.options_cb_lock_screen);
        CheckBox disable_sound = findViewById(R.id.options_cb_sound);
        CheckBox larger_buttons = findViewById(R.id.options_cb_larger_controllers);
        CheckBox no_buttons = findViewById(R.id.options_cb_touch_button_overlay);
        CheckBox japanese_mode = findViewById(R.id.options_cb_japanese_mode);
        CheckBox no_stretching = findViewById(R.id.options_cb_screen_stretching);
        CheckBox game_genie = findViewById(R.id.options_cb_enable_gg_action_replay);
        boolean errors = false;

        settings.append("orientation_lock=");
        if (orientation_lock.isChecked()) {
            settings.append("1\n");
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                settings.append("orientation=portrait\n");
            else
                settings.append("orientation=landscape\n");
        } else {
            settings.append("0\n");
        }
        settings.append("disable_sound=");
        if (disable_sound.isChecked())
            settings.append("1\n");
        else
            settings.append("0\n");
        settings.append("larger_buttons=");
        if (larger_buttons.isChecked())
            settings.append("1\n");
        else
            settings.append("0\n");
        settings.append("no_buttons=");
        if (no_buttons.isChecked())
            settings.append("1\n");
        else
            settings.append("0\n");
        settings.append("japanese_mode=");
        if (japanese_mode.isChecked())
            settings.append("1\n");
        else
            settings.append("0\n");
        settings.append("no_stretching=");
        if (no_stretching.isChecked())
            settings.append("1\n");
        else
            settings.append("0\n");
        if (OptionStore.default_path != null) {
            if (OptionStore.default_path.length() > 0) {
                settings.append("default_path=" + OptionStore.default_path + "\n");
            }
        }
        settings.append("game_genie=");
        if (game_genie.isChecked())
            settings.append("1\n");
        else
            settings.append("0\n");


        // define settings file
        File optionsFile = new File(OptionsActivity.this.getFilesDir() + "/settings.ini");

        // set length of file to zero
        RandomAccessFile r = null;
        try {
            r = new RandomAccessFile(optionsFile, "rw");
            r.setLength(0);
        }
        catch (Exception e) {
            Log.e("OptionsActivity", "Problem occurred with RandomAccessFile: " + e);
            errors = true;
        }
        finally {
            // close random acess file
            try {
                r.close();
            }
            catch (IOException e) {
                Log.e("OptionsActivity", "Couldn't close RandomAccessFile: " + e);
                errors = true;
            }
        }

        // write settings to file
        BufferedWriter settingsWriter = null;
        try {
            settingsWriter = new BufferedWriter(new FileWriter(optionsFile));
            settingsWriter.write(settings.toString(), 0, settings.length());
        }
        catch (Exception e) {
            Log.e("OptionsActivity", "Problem occurred writing to settings file: " + e);
            errors = true;
        }
        finally {
            // close settings file
            try {
                settingsWriter.close();
            }
            catch (IOException e) {
                Log.e("OptionsActivity", "Couldn't close BufferedWriter: " + e);
                errors = true;
            }
        }

        Toast status = null;
        if (errors) {
            status = Toast.makeText(OptionsActivity.this, "Failed to update settings file", Toast.LENGTH_SHORT);
            status.show();
        } else {
            status = Toast.makeText(OptionsActivity.this, "Successfully updated settings file", Toast.LENGTH_SHORT);
            OptionStore.updateOptionsFromFile(optionsFile.getAbsolutePath());
            status.show();
            finish();
        }
    }
    /**
     * This method helps us to detect gamepad events and do the right thing with them.
     */
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        /*int action, keycode;
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
        */
        return super.dispatchKeyEvent(event);
    }
    /**
     * This method deals with input from the analogue stick and some d-pads.
     */
    public boolean motionEvent(MotionEvent event) {
        boolean returnVal = true;

        /*if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
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
        }*/


        return returnVal;
    }
    /**
     * This inner class allows us to support joysticks with a listener.
     */
    protected class MasterEmuMotionListener implements View.OnGenericMotionListener {
        @Override public boolean onGenericMotion(View v, MotionEvent event) {
            return OptionsActivity.this.motionEvent(event);
        }
    }
    /**
     * This class allows us to animate a button.
     */
    protected class ButtonColourListener implements View.OnTouchListener {
        /**
         * This is the actual implementation code.
         */
        @Override public boolean onTouch(View v, MotionEvent event) {
            //ControllerMapped cv = (ControllerMapped) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //cv.unHighlight();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                //cv.highlight();
                // This is where we apply the settings
                if (v.getId() == R.id.options_btn_apply) {
                    applySettings();
                }
            }
            return true;
        }
    }
}
