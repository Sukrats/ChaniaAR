package tuc.christos.chaniacitywalk2;

/**
 * Created by Christos on 09-May-17.
 *
 */

public interface ContentListener {

    void downloadComplete(boolean success,int httpCode, String TAG, String msg);
}
