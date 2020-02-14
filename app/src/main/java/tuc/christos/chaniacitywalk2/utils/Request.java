package tuc.christos.chaniacitywalk2.utils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Christos on 26-Oct-17.
 *
 */

public class Request {

    private String operation;        //CRUD OPERATIONS
    private String URI;
    private String contentType;
    private JSONArray body;
    private String auth;

    public Request(String type, String URI, String contentType, JSONArray body) {
        this.operation = type;
        this.URI = URI;
        this.body = body;
        this.contentType = contentType;
    }

    public Request(String type, String URI, JSONArray body, String auth) {
        this.operation = type;
        this.URI = URI;
        this.body = body;
        this.auth = auth;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = URI;
    }

    public JSONArray getBody() {
        return body;
    }

    public void setBody(JSONArray body) {
        this.body = body;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
}
