package tuc.christos.chaniacitywalk2.mInterfaces;

import android.location.Location;

/**
 * Created by Christos on 16-Mar-17.
 *
 */

public interface LocationEventsListener {

    void drawGeoFences(long[] fences);

    void userEnteredArea(long areaID);

    void userLeftArea(long areaID);

}
