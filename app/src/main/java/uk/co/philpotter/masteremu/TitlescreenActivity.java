/* MasterEmu titlescreen source code file
   copyright Phil Potter, 2019 */

package uk.co.philpotter.masteremu;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;

/**
 * This class acts as the titlescreen of the app.
 */
public class TitlescreenActivity extends Activity {

    // define instance variables
    //private ControllerSelection selectionObj;
    //private long timeSinceLastAnaloguePress = 0;

    /**
     * This loads settings when the app first starts.
     */
    @Override protected void onStart() {
        super.onStart();
        File f = new File(getFilesDir() + "/default_file");
        try {
            f.createNewFile();
        }
        catch (IOException e) {
            Log.e("TitlescreenActivity", "Couldn't create default_file: " + e);
        }
        OptionStore.updateOptionsFromFile(getFilesDir() + "/settings.ini");
        if (OptionStore.orientation_lock) {
            if (OptionStore.orientation.equals("portrait")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (OptionStore.orientation.equals("landscape")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    /**
     * This kills the app for us when back is pressed.
     */
    @Override public void onBackPressed() {
        finish();
    }

    /**
     * This method creates the titlescreen.
     */
    @SuppressWarnings("deprecation") @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Runtime.getRuntime().loadLibrary("SDL2");
        }
        catch (UnsatisfiedLinkError e) {
            Intent wrongCpu = new Intent(this, WrongCpuActivity.class);
            startActivity(wrongCpu);
            finish();
        }
        setContentView(R.layout.titlescreen_activity);
        SetButtonBehaviors();
    }
    private void SetButtonBehaviors(){
        ImageButton imgBtn_LoadROM = findViewById(R.id.title_imgBtn_LoadROM);
        ImageButton imgBtn_Options = findViewById(R.id.title_imgBtn_Options);
        ImageButton imgBtn_Help = findViewById(R.id.title_imgBtn_Help);
        ImageButton imgBtn_Credits = findViewById(R.id.title_imgBtn_Credits);
        ImageButton imgBtn_Extras = findViewById(R.id.title_imgBtn_Extras);
        ImageButton imgBtn_ManageStates = findViewById(R.id.title_imgBtn_ManageStates);
        ImageButton imgBtn_Exit = findViewById(R.id.title_imgBtn_Exit);

        TitleScreenButtonBehaviors listener = new TitleScreenButtonBehaviors();

        imgBtn_LoadROM.setOnClickListener(listener);
        imgBtn_Options.setOnClickListener(listener);
        imgBtn_Help.setOnClickListener(listener);
        imgBtn_Credits.setOnClickListener(listener);
        imgBtn_Extras.setOnClickListener(listener);
        imgBtn_ManageStates.setOnClickListener(listener);
        imgBtn_Exit.setOnClickListener(listener);
    }
    class TitleScreenButtonBehaviors implements View.OnClickListener{
        @Override public void onClick(View view) {
            switch (view.getId()){
                case R.id.title_imgBtn_LoadROM:
                    Intent i = new Intent(TitlescreenActivity.this, FileBrowserActivity.class);
                    i.putExtra("actionType", "load_rom");
                    startActivity(i);
                    break;
                case R.id.title_imgBtn_Options:
                    startActivity(new Intent(TitlescreenActivity.this, OptionsActivity.class));
                    break;
                case R.id.title_imgBtn_Help:
                    startActivity(new Intent(TitlescreenActivity.this, HelpActivity.class));
                    break;
                case R.id.title_imgBtn_Credits:
                    startActivity(new Intent(TitlescreenActivity.this, CreditsActivity.class));
                    break;
                case R.id.title_imgBtn_Extras:
                    startActivity(new Intent(TitlescreenActivity.this, ExtrasActivity.class));
                    break;
                case R.id.title_imgBtn_ManageStates:
                    startActivity(new Intent(TitlescreenActivity.this, ManageStatesActivity.class));
                    break;
                case R.id.title_imgBtn_Exit:
                    finish();
                    break;
            }
        }
    }
    /**
     * This method helps us to detect gamepad events and do the right thing with them.
     */
    /*
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action, keycode;
        action = event.getAction();
        keycode = event.getKeyCode();
        boolean returnVal = false;
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
                if (selectionObj != null)
                    finish();
                returnVal = true;
            }
        }

        if (returnVal)
            return returnVal;

        return super.dispatchKeyEvent(event);
    }*/

    /**
     * This method deals with input from the analogue stick and some d-pads.
     */
    /*
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
    */
    /**
     * This inner class allows us to support joysticks with a listener.
     */
    /*protected class MasterEmuMotionListener implements View.OnGenericMotionListener {
        @Override
        public boolean onGenericMotion(View v, MotionEvent event) {
            return TitlescreenActivity.this.motionEvent(event);
        }
    }*/

    /**
     * This class allows us to animate a button.
     */
    /*protected class ButtonColourListener implements View.OnTouchListener {*/

        /**
         * This is the actual implementation code.
         */
        /*@Override
        public boolean onTouch(View v, MotionEvent event) {
            ControllerTextView cv = (ControllerTextView)v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                cv.highlight();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                cv.unHighlight();
                cv.activate();
            }
            return true;
        }
    }*/
}
