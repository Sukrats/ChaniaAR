package tuc.christos.chaniacitywalk2.utils;

/**
 * Created by Christos on 11-Apr-17.
 *
 */

public class Constants {
    public static final String WIKITUDE_SDK_KEY = "QYDTeJGF0oFY9UYUmVYRMrp6d8+fjb8KI6dtqtTmaDYkpfMsJABSeGFGZvcFK+svTr4si4NRNUhCEDm7VwdawQfGC4+gPwa1BKZjhvcUOV4QRjtbN7yesg13shpeSXuRboCK42miQzSl9S7e8tDIADlUvmjg9ytGFkNho9sFhL1TYWx0ZWRfXyH1S3AAIMH2pypCdkD4VPBhuMPVafLlolTJiFjC7HstY7tCUsYg+jreq5yDUR+pdEHzQvT0O9WaAbV9TanxxqtyyRUeYwt1vVO+WKJ9ulwE2foBBghQ4or5j5SlrX0aaN4Hs9XLWXq3LBQWY8SasFiF9w++JO+cMGjaW5VcfUErmmRHa3rgNpKT2eoSSc/X7DkETsaNvF7I33lv+Pz5HYn46D4L8GLYTUYjvfKOKBQzWTTPoFzgghVw0nkLb0cLHPvi1GhY0Wqbxrg4+PC72AWWwzPlD35tt80qo8J82qCpYNGqDfQYN6jLHshdi810RSWj8Xk7OQQRHurk7sPJers80hzNENNY8tfcs5b0FuUbw459Le/FBEr46jfkGDj+8Z7Z0tzvXeQi4SYvYuXtePZiVvwkFx6WU3vimRkjI3dDJiIO0iCfuKSm65YF1w7kMXvvW4N/gnqx+zmP4CWMrCJNyoDR31GUKjplLTqcA/5u+gqEdaV2vAibzlOaSgnYmjg6wrDm+EHY";
    public static final String WIKITUDE_SDK_KEY_51 = "JEsKfp9mCJzu6kRmgZys/BIvC8N+bMunN3V1IenCeGXkR8aHIZzw83b3UYfFXYUo3jnAQExtX83E1Q/KEBW+rQbZnr8Z06FRfdEmpyhA/NLAl8m4yfs1FkNAonxjEV88ruNfIHNuWRSLelQ5LBdsXxeQAaI2TzHebw5AFHhsIf5TYWx0ZWRfX1l2aZ4iwV9ofrydNwWT/TWCnbzBf7YS/FvOlRouMnWNlCeW2zrTZFcNKjwHPCxvgeItkQuHT/Q6svpQW6nTB+d95B4gZ3ET773bCq3k8FF8amMH7MjKs5XHW0N7TmEkML4N6s0VU0Mq1BQpNLQDLHNbTOEkw6+gnSigATv5yC9OCJ4pgeaOKxGqDJUlDYB/aEP46XZGuw6wPA3qe7kQ8jCnTK+h39GF2w2JhByeKVuXBL/LDg98M9GtzJ0UAFyFsFU1JLdZXcynAr9E1RiNftu05IiXWLzVNlsvquboRlkoGrwxVVaFU3fU0Tk2rWEo1k5GDrmnWwFflGLdAAsae6KH8dVLCjOy0APWTd512rnlm888HKE2KvUyg50QnIi3nk/vJv1BDS0DwR6NEf3WKUeZzTn/RrQKFjp+TRjvghCI4uIZ21iadHqGLf5UB31SDCdDS/JGtvDPTRXynbGvtO55IgTBeqad9iWAwtqdaiXu8wCLb09Z9Pnfza3vhvyDdjHwzWS+wIeK";

    private static final String URL_APP = "http://citywalk.duckdns.org:8080/citywalk/arapp/";
    private static final String URL_APP_LOCAL = "http://192.168.1.11:8080/citywalk/arapp/";

    public static final String URL_SCENES = URL_APP + "scenes";
    public static final String URL_SCENES_SYNC = URL_SCENES + "/sync";

    public static final String URL_PERIODS = URL_APP + "periods";
    public static final String URL_PERIODS_SYNC = URL_PERIODS + "/sync";

    public static final String URL_PUT_USER = URL_APP + "users/secure/";
    public static final String URL_USER = URL_APP + "users/secure/";
    public static final String URL_REGISTER_USER = URL_APP + "users/";
    public static final String URL_LOGIN_USER = URL_APP + "users/secure/login";
    public static final String URL_USERS = URL_APP + "users/admin";

    public static final String PLAYERS_TABLE = "Player";
    public static final String SCENES_TABLE = "Scenes";
    public static final String PERIODS_TABLE = "Periods";
    public static final String MODIFICATIONS_TABLE = "Modifications";


    public static final String ARCHITECT_WORLD_KEY = "WorldToLoad";
    public static final String ARCHITECT_MODEL_AT_GEOLOCATION_KEY = "ModelAtGeoLocation";
    public static final String ARCHITECT_AR_NAVIGATION_KEY = "ArNavigation";
    public static final String ARCHITECT_INSTANT_TRACKING_KEY = "Instant";
    public static final String ARCHITECT_AR_SCENE_KEY = "scene";
    public static final String ARCHITECT_ORIGIN = "origin";
    public static final String ARCHITECT_QUESTION_SCENE_KEY = "question";

    public static final int PERMA_NOTIFICATION_ID = -20;
    public static final String ACTION_STOP = "STOP";
    public static final String ACTION_START = "START";
    public static final String ACTION_SETTINGS = "SETTINGS";
}
