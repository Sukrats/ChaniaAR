package tuc.christos.chaniacitywalk2;

import android.location.Location;

/**
 * Created by Christos on 13-Mar-17.
 */

public interface LocationCallback {
    void handleNewLocation(Location location);
}
