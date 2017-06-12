package tuc.christos.chaniacitywalk2.mInterfaces;

import android.location.Location;

/**
 * Created by Christos on 08-Jun-17.
 *
 */

public interface IServiceListener {

    void drawGeoFences(long[] areaIDs);

    void regionChanged(String region_name,String country);

    void userEnteredArea(long areaID);

    void userLeftArea(long areaID);

    void handleNewLocation(Location location);
}
