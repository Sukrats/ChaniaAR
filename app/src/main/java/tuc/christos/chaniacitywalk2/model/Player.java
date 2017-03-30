package tuc.christos.chaniacitywalk2.model;

import java.util.ArrayList;

/**
 * Created by Christos on 24/1/2017.
 *
 */

public class Player {

    private String id;          //used only in coherence with the server db
    private String email;
    private String password;
    private String username;
    private Long placesUnlocked;
    private Long placesVisited;
    private Scene isAtScene;
    private ArrayList<Scene> hasCompleted = new ArrayList<>();
    private ArrayList<Scene> hasSeen = new ArrayList<>();
    private boolean completedRoute;

    private static Player player = new Player();

    private Player(){}

    public Player getInstance() {
        if (player == null){
            //Check if Player table exists in Database
            if(checkPlayerData()){
                //if it exists
                //Create Player instance from table
                //return player
                return player;
            }else{
                //if not( First the Applications is executed)
                //Create new Player with given credentials
                //return player instance
                return player;
            }
        }else
         return player;
    }

    private boolean checkPlayerData(){
        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public ArrayList<Scene> getHasCompleted() {
        return hasCompleted;
    }

    public void setHasCompleted(ArrayList<Scene> hasCompleted) {
        this.hasCompleted = hasCompleted;
    }

    public ArrayList<Scene> getHasSeen() {
        return hasSeen;
    }

    public void setHasSeen(ArrayList<Scene> hasSeen) {
        this.hasSeen = hasSeen;
    }

    public boolean isCompletedRoute() {
        return completedRoute;
    }

    public void setCompletedRoute(boolean completedRoute) {
        this.completedRoute = completedRoute;
    }
}
