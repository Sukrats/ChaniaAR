package tuc.christos.chaniacitywalk2.locationService;

import android.location.Location;

/**
 * Created by Christos on 16-Mar-17.
 *
 */

interface LocationEventsListener {

    void drawGeoFences(String[] areaIds, int radius);

    void userEnteredArea(String areaID);

    void userLeftArea(String areaID);

}
