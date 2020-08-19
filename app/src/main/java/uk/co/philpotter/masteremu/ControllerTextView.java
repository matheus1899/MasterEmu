/* MasterEmu ControllerTextView source code file
   copyright Phil Potter, 2019 */

package uk.co.philpotter.masteremu;

import android.os.Build;
import android.widget.TextView;
import android.content.Context;
import android.util.AttributeSet;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.app.Activity;

/**
 * Extends the normal TextView to allow controller support.
 */
public class ControllerTextView extends TextView implements ControllerMapped {

    // instance variables
    private Intent activity;
    private Drawable inactiveDrawable;
    private Drawable activeDrawable;
    private int highlightedText;
    private int unhighlightedText;
    private boolean isOptions = false;
    /**
     * Constructors to allow instantiation in the same way as parent class.
     */
    public ControllerTextView(Context context) {
        super(context);
        setDefaultAttributes();
    }
    public ControllerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDefaultAttributes();
    }
    public ControllerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDefaultAttributes();
    }
    private void setDefaultAttributes(){
        Drawable light, dark;
        int lightText, darkText;
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            lightText = getResources().getColor(R.color.ColorHighlight);
            darkText = getResources().getColor(R.color.ColorUnhighlight);
        } else {
            lightText = getResources().getColor(R.color.ColorHighlight, null);
            darkText = getResources().getColor(R.color.ColorUnhighlight, null);
        }
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            light = getResources().getDrawable(R.drawable.teste_bg_highlight);
            dark = getResources().getDrawable(R.drawable.teste_bg_unhighlight);
        } else {
            light = getResources().getDrawable(R.drawable.teste_bg_highlight, null);
            dark = getResources().getDrawable(R.drawable.teste_bg_unhighlight, null);
        }
        this.setActiveDrawable(light);
        this.setInactiveDrawable(dark);
        this.setHighlightedTextColour(lightText);
        this.setUnhighlightedTextColour(darkText);
    }
    public void activate() {
        if (isOptions) {
            OptionsActivity parent = (OptionsActivity)getContext();
            parent.applySettings();
        } else if (activity != null) {
            unHighlight();
            getContext().startActivity(activity);
        }
        else
            ((Activity)getContext()).finish();
    }
    public void highlight() {
        setBackground(activeDrawable);
        setTextColor(highlightedText);
    }
    public void unHighlight() {
        setBackground(inactiveDrawable);
        setTextColor(unhighlightedText);
    }
    public void setActiveDrawable(Drawable d) {
        activeDrawable = d;
    }
    public void setInactiveDrawable(Drawable d) {
        inactiveDrawable = d;
    }
    public void setIntent(Intent i) {
        activity = i;
    }
    public void setHighlightedTextColour(int colour) {
        highlightedText = colour;
    }
    public void setUnhighlightedTextColour(int colour) {
        unhighlightedText = colour;
    }
    public void isOptions() {
        isOptions = true;
    }
}
