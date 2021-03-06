package tuc.christos.chaniacitywalk2.model;

import android.util.SparseArray;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christos on 24/1/2017.
 *
 */

public class Player {
    private String email;
    private String username;
    private String password;
    private String newPassword;
    private String firstname;
    private String lastname;
    private Date created;
    private Date recentActivity;
    private Long numOfPlaces;
    private Long numOfVisits;
    private Long score;

    private Scene isAtScene;
    private boolean completedRoute;

    private Map<String, String> links = new HashMap<>();

    private HashMap<String,Place> places = new HashMap<>();
    private HashMap<String,Visit> visited = new HashMap<>();


    public Player() {
    }

    public Player(String email, String username, String password, String firstname, String lastname){
        this.email = email;
        this.password = password;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
    }
    /**
     * Player Instance
     *
     * @return Always check for null return to know if it is instantiated
     */

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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String password) {
        this.newPassword = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getNumOfPlaces() {
        return numOfPlaces;
    }

    public void setNumOfPlaces(Long numOfPlaces) {
        this.numOfPlaces = numOfPlaces;
    }

    public Long getNumOfVisits() {
        return numOfVisits;
    }

    public void setNumOfVisits(Long numOfVisits) {
        this.numOfVisits = numOfVisits;
    }

    public Scene getIsAtScene() {
        return isAtScene;
    }

    public void setIsAtScene(Scene isAtScene) {
        this.isAtScene = isAtScene;
    }



    public boolean isCompletedRoute() {
        return completedRoute;
    }

    public void setCompletedRoute(boolean completedRoute) {
        this.completedRoute = completedRoute;
    }


    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    public void addLink(String rel, String url) {
        this.links.put(rel, url);
    }


    public Date getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(Date recent_activity) {
        this.recentActivity = recent_activity;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public void updateScore(boolean success){
        if(success)
            this.score +=(this.score - 501)*15/100; //this.score += 250;
        else
            this.score -=(this.score - 501)*15/100;// -= 250;
    }
    public void updateBaseScore(){
        this.score += 250;

    }


    public HashMap<String,Place> getPlaces() {
        return places;
    }

    public void setPlaces(HashMap<String,Place> places) {
        this.places = places;
    }

    public void addPlace(Place place){
        this.places.put(String.valueOf(place.getScene_id()),place);
    }
    public void removePlace(long id){
        this.places.remove(String.valueOf(id));
    }
    public boolean hasPlaced(long scene_id){
        return this.places.containsKey(String.valueOf(scene_id));
    }
    public Place getPlace(long scene_id){
        return this.places.get(String.valueOf(scene_id));
    }


    public HashMap<String,Visit> getVisited() {
        return visited;
    }

    public void setVisited(HashMap<String,Visit> visited) {
        this.visited = visited;
    }

    public void addVisit(Visit visit){
        this.visited.put(String.valueOf(visit.getScene_id()),visit);
    }
    public boolean hasVisited(long scene_id){
        return this.visited.containsKey(String.valueOf(scene_id));
    }
    public Visit getVisit(long scene_id){
        return this.visited.get(String.valueOf(scene_id));
    }
}
