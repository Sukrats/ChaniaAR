package tuc.christos.chaniacitywalk2;

/**
 * Created by Christos on 29-May-17.
 *
 */

public interface ClientListener {

    void onCompleted(boolean success, int httpCode, String msg);

    void onUpdate(int progress, String msg);
}
