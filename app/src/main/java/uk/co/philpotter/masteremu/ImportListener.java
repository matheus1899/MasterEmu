package uk.co.philpotter.masteremu;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

/**
 * This is the listener which handles importing of states.
 */
public class ImportListener implements DialogInterface.OnClickListener {
    private FileBrowserActivity activity;
    public ImportListener(FileBrowserActivity c){
        activity = c;
    }

    @Override public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                // import files with StateIO class
                StateIO manager = new StateIO();
                boolean result = manager.importFromZip(activity.getFilesDir().getAbsolutePath(), activity.importPath);

                // check success/failure
                if (result) {
                    Toast success = Toast.makeText(activity, "Imported states successfully", Toast.LENGTH_SHORT);
                    success.show();
                    activity.finish();
                } else {
                    Toast failure = Toast.makeText(activity, "Couldn't find states in this file", Toast.LENGTH_SHORT);
                    failure.show();
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                Toast negative = Toast.makeText(activity, "Import was cancelled", Toast.LENGTH_SHORT);
                negative.show();
                break;
        }
    }
}
