package tuc.christos.chaniacitywalk2.model;

/**
 * Created by Christos on 30-May-17.
 *
 */

public class ArScene extends Scene {
    private String modelLocation;

    public ArScene(String modelLocation) {
        this.modelLocation = modelLocation;
    }

    public ArScene(double latitude, double longitude, int id, int periodid, String name, String description, String modelLocation) {
        super(latitude, longitude, id, periodid, name, description);
        this.modelLocation = modelLocation;
    }
}
