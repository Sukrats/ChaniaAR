package tuc.christos.chaniacitywalk2.locationService;

import android.location.Location;
/**
 * Created by Christos on 13-Mar-17.
 *
 */

interface LocationCallback {

    void handleNewLocation(Location location);
}
