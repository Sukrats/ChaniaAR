package tuc.christos.chaniacitywalk2.model;

import android.util.SparseArray;

import java.util.ArrayList;

/**
 * Created by Christos on 24/1/2017.
 *
 */

public class Player {

    private static Player INSTANCE = null;
    private boolean instantiated = false;

    private long id;          //used only in coherence with the server db
    private String email;
    private String password;
    private String username;

    private Long placesUnlocked;
    private Long placesVisited;
    private Scene isAtScene;
    private boolean completedRoute;

    private SparseArray<Scene> visited = new SparseArray<>();

    private Player(){}

    public Player(long id, String email, String password, String username) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
    }

    /**
     * Player Instance
     * @return
     * Always check for null return to know if it is instantiated
     */

    public static Player getInstance(){
        return INSTANCE;
    }

    public void initPlayer(long id, String email, String password, String username ){
        INSTANCE = new Player(id,email,password,username);
        instantiated = true;
    }

    public void resetPlayer(){
        INSTANCE = null;
        instantiated = false;
    }

    public boolean isInstantiated(){return instantiated;}

    private boolean checkPlayerData(){
        return true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getPlacesUnlocked() {
        return placesUnlocked;
    }

    public void setPlacesUnlocked(Long placesUnlocked) {
        this.placesUnlocked = placesUnlocked;
    }

    public Long getPlacesVisited() {
        return placesVisited;
    }

    public void setPlacesVisited(Long placesVisited) {
        this.placesVisited = placesVisited;
    }

    public Scene getIsAtScene() {
        return isAtScene;
    }

    public void setIsAtScene(Scene isAtScene) {
        this.isAtScene = isAtScene;
    }

    public SparseArray<Scene> getVisited() {
        return visited;
    }

    public void setVisited(SparseArray<Scene> visited) {
        this.visited = visited;
    }


    public boolean isCompletedRoute() {
        return completedRoute;
    }

    public void setCompletedRoute(boolean completedRoute) {
        this.completedRoute = completedRoute;
    }
}
