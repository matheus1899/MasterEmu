/* MasterEmu ControllerMapped interface file
   copyright Phil Potter, 2019 */

package uk.co.philpotter.masteremu;

import android.graphics.drawable.Drawable;
import android.content.Intent;

/**
 * This interface allows us to treat all objects equally in terms of highlighting them etc.
 */
public interface ControllerMapped {
    void highlight();
    void unHighlight();
    void activate();
    void setActiveDrawable(Drawable d);
    void setInactiveDrawable(Drawable d);
}
