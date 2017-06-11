package tuc.christos.chaniacitywalk2.mInterfaces;

import android.location.Location;

/**
 * Created by Christos on 08-Jun-17.
 *
 */

public interface IServiceListener {

    void regionChanged(String region_name,String country);

    void userEnteredArea(String areaID);

    void userLeftArea(String areaID);

    void handleNewLocation(Location location);
}
