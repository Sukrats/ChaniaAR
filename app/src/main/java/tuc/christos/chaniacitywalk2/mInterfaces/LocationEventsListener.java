package tuc.christos.chaniacitywalk2.mInterfaces;

import android.location.Location;

/**
 * Created by Christos on 16-Mar-17.
 *
 */

public interface LocationEventsListener {

    void userEnteredArea(String areaID);

    void userLeftArea(String areaID);

}
