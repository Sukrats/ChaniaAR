package tuc.christos.chaniacitywalk2;

import android.location.Location;

/**
 * Created by Christos on 16-Mar-17.
 *
 */

public interface LocationEventsListener {

    void userEnteredArea(int areaID);

    void userLeftArea(int areaID);

}
