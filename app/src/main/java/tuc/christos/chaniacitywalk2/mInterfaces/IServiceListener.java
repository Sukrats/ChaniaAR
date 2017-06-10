package tuc.christos.chaniacitywalk2.mInterfaces;

import android.location.Location;

/**
 * Created by Christos on 08-Jun-17.
 *
 */

public interface IServiceListener {

    void drawGeoFences(String[] areaIds, int radius);

    void userEnteredArea(String areaID);

    void userLeftArea(String areaID);

    void handleNewLocation(Location location);
}
