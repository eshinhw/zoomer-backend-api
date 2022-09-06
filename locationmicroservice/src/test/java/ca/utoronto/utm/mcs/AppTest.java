package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

/**
 * Please write your tests in this class.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest {

    final static String API_URL = "http://0.0.0.0:8000/location";

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws IOException, InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void updateDummyUsers() throws JSONException, IOException, InterruptedException {
        ArrayList<String> uids = new ArrayList<>();

        uids.add("7001");
        uids.add("7002");
        uids.add("7003");
        uids.add("7004");
        uids.add("7005");

        JSONObject passengerLoc = new JSONObject();
        JSONObject driver1Loc = new JSONObject();
        JSONObject driver2Loc = new JSONObject();
        JSONObject driver3Loc = new JSONObject();
        JSONObject driver4Loc = new JSONObject();

        passengerLoc.put("longitude", -79.41395693614706);
        passengerLoc.put("latitude", 43.76974409999627);
        passengerLoc.put("street", "Gibson");


        driver1Loc.put("longitude", -79.41525232200692);
        driver1Loc.put("latitude", 43.780016543768205);
        driver1Loc.put("street", "Finch");

        driver2Loc.put("longitude", -79.41045347659814);
        driver2Loc.put("latitude", 43.76192611847282);
        driver2Loc.put("street", "Sheppard");

        driver3Loc.put("longitude",-79.18623868352148);
        driver3Loc.put("latitude", 43.78406214773374);
        driver3Loc.put("street", "UTSC");

        driver4Loc.put("longitude", -79.61621149513054);
        driver4Loc.put("latitude", 43.68313333617516);
        driver4Loc.put("street", "Airport");

        ArrayList<JSONObject> locations = new ArrayList<>();

        locations.add(passengerLoc);
        locations.add(driver1Loc);
        locations.add(driver2Loc);
        locations.add(driver3Loc);
        locations.add(driver4Loc);

        for (int i=0; i<5; i++) {
            try {
                HttpResponse<String> response = sendRequest("/" + uids.get(i), "PATCH", locations.get(i).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static void addDummyRoads() throws JSONException, IOException, InterruptedException {
        JSONObject Gibson1 = new JSONObject();
        JSONObject Finch2 = new JSONObject();
        JSONObject Sheppard3 = new JSONObject();
        JSONObject UTSC4 = new JSONObject();
        JSONObject Airport5 = new JSONObject();

        Gibson1.put("roadName", "Gibson");
        Gibson1.put("hasTraffic", false);
        Finch2.put("roadName", "Finch");
        Finch2.put("hasTraffic", false);
        Sheppard3.put("roadName", "Sheppard");
        Sheppard3.put("hasTraffic", true);
        UTSC4.put("roadName", "UTSC");
        UTSC4.put("hasTraffic", true);
        Airport5.put("roadName", "Airport");
        Airport5.put("hasTraffic", true);

        ArrayList<JSONObject> roads = new ArrayList<>();

        roads.add(Gibson1);
        roads.add(Finch2);
        roads.add(Sheppard3);
        roads.add(UTSC4);
        roads.add(Airport5);

        for (JSONObject obj : roads) {
            try {
                HttpResponse<String> response = sendRequest("/road", "PUT", obj.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static void addDummyRoutes() throws JSONException, IOException, InterruptedException {
        ArrayList<JSONObject> routes = new ArrayList<>();

        JSONObject route1 = new JSONObject();
        JSONObject route2 = new JSONObject();
        JSONObject route3 = new JSONObject();
        JSONObject route4 = new JSONObject();
        JSONObject route5 = new JSONObject();

        route1.put("roadName1", "Finch");
        route1.put("roadName2", "Gibson");
        route1.put("hasTraffic", false);
        route1.put("time", 20);

        route2.put("roadName1", "Gibson");
        route2.put("roadName2", "Airport");
        route2.put("hasTraffic", true);
        route2.put("time", 50);

        route3.put("roadName1", "Gibson");
        route3.put("roadName2", "Sheppard");
        route3.put("hasTraffic", true);
        route3.put("time", 40);

        route4.put("roadName1", "Sheppard");
        route4.put("roadName2", "UTSC");
        route4.put("hasTraffic", true);
        route4.put("time", 100);

        route5.put("roadName1", "Finch");
        route5.put("roadName2", "UTSC");
        route5.put("hasTraffic", true);
        route5.put("time", 500);

        routes.add(route1);
        routes.add(route2);
        routes.add(route3);
        routes.add(route4);
        routes.add(route5);

        for (JSONObject obj : routes) {
            try {
                HttpResponse<String> response = sendRequest("/hasRoute", "POST", obj.toString());
            } catch(Exception e) {
                e.printStackTrace();
            }
        }



    }

    private static void initializeDummyUsers() throws JSONException, IOException, InterruptedException {
        ArrayList<JSONObject> users = new ArrayList<>();
        JSONObject passenger = new JSONObject();
        JSONObject driver1 = new JSONObject();
        JSONObject driver2 = new JSONObject();
        JSONObject driver3 = new JSONObject();
        JSONObject driver4 = new JSONObject();

        passenger.put("uid", "7001");
        passenger.put("is_driver", false);
        driver1.put("uid", "7002");
        driver1.put("is_driver", true);
        driver2.put("uid", "7003");
        driver2.put("is_driver", true);
        driver3.put("uid", "7004");
        driver3.put("is_driver", true);
        driver4.put("uid", "7005");
        driver4.put("is_driver", true);

        users.add(passenger);
        users.add(driver1);
        users.add(driver2);
        users.add(driver3);
        users.add(driver4);

        for (JSONObject obj : users) {
            try {
                HttpResponse<String> response = sendRequest("/user", "PUT", obj.toString());
            } catch(Exception e) {
                e.printStackTrace();
            }

        }

    }

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException, JSONException {
        initializeDummyUsers();
        addDummyRoads();
        addDummyRoutes();
        updateDummyUsers();

    }

    @Test
    @Order(1)
    public void nearbyDriversPass() throws JSONException, IOException, InterruptedException {
        // STATUS 200
        String uid = "7001";
        int radius = 100;

        String url = String.format("/nearbyDriver/%s?radius=%d", uid, radius);

        HttpResponse<String> response = sendRequest(url, "GET", "");
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
    }

    @Test
    @Order(2)
    public void nearbyDriversFail400A() throws JSONException, IOException, InterruptedException {
        // STATUS 400 BAD REQUEST - RADIUS MISSING
        String uid = "2";
        String url = String.format("/nearbyDriver/%s", uid);

        HttpResponse<String> response = sendRequest(url, "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(3)
    public void nearbyDriversFail400B() throws JSONException, IOException, InterruptedException {
        // STATUS 400 BAD REQUEST - UID MISSING

        int radius = 5;

        String url = String.format("/nearbyDriver?radius=%d", radius);

        HttpResponse<String> response = sendRequest(url, "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(4)
    public void nearbyDriversFail400C() throws JSONException, IOException, InterruptedException {
        // STATUS 400 BAD REQUEST - NEGATIVE RADIUS
        String uid = "2";
        int radius = -4;

        String url = String.format("/nearbyDriver/%s?radius=%d", uid, radius);

        HttpResponse<String> response = sendRequest(url, "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(5)
    public void navigationPass() throws JSONException, IOException, InterruptedException {
        String driverUid = "7004";
        String passengerUid = "7001";

        String url = String.format("/navigation/%s?passengerUid=%s", driverUid, passengerUid);

        HttpResponse<String> response = sendRequest(url, "GET", "");
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
    }

    @Test
    @Order(6)
    public void navigationFail400A() throws JSONException, IOException, InterruptedException {
        // STATUS 400 BAD REQUEST driverUid Missing

        String passengerUid = "";

        String url = String.format("/navigation?passengerUid=%s", passengerUid);

        HttpResponse<String> response = sendRequest(url, "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(7)
    public void navigationFail400B() throws JSONException, IOException, InterruptedException {
        // STATUS 400 BAD REQUEST passengerUid Missing
        String driverUid = "";

        String url = String.format("/navigation/%s", driverUid);

        HttpResponse<String> response = sendRequest(url, "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }
}
