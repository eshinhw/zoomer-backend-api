package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.util.JSON;
import org.bson.types.ObjectId;
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

    final static String API_URL = "http://0.0.0.0:8000/trip";
    final static String SETUP_URL = "http://locationmicroservice:8000/location";
    static String tripIdCreated = "";

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> sendSetupRequest(String endpoint, String method, String reqBody) throws InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SETUP_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void resetDatabase() throws IOException, InterruptedException {
        try {
            HttpResponse<String> response = sendRequest("/resetDatabase", "DELETE", "");
        }
        catch(Exception e) {
            e.printStackTrace();
            return;
        }
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
                HttpResponse<String> response = sendSetupRequest("/" + uids.get(i), "PATCH", locations.get(i).toString());
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
                HttpResponse<String> response = sendSetupRequest("/road", "PUT", obj.toString());
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
                HttpResponse<String> response = sendSetupRequest("/hasRoute", "POST", obj.toString());
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
                HttpResponse<String> response = sendSetupRequest("/user", "PUT", obj.toString());
            } catch(Exception e) {
                e.printStackTrace();
            }

        }

    }

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException, JSONException {
//        resetDatabase();
        initializeDummyUsers();
        addDummyRoads();
        addDummyRoutes();
        updateDummyUsers();
    }

    @Test
    @Order(1)
    public void requestTripPass() throws JSONException, IOException, InterruptedException {
        JSONObject request = new JSONObject()
                .put("uid", "7001")
                .put("radius", 100);

        HttpResponse<String> response = sendRequest("/request", "POST", request.toString());
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());

    }

    @Test
    @Order(2)
    public void requestTripFail400A() throws JSONException, IOException, InterruptedException {
        // 400 BAD REQUEST PARAMETER MISSING
        JSONObject request = new JSONObject()
                .put("uid", "2");

        HttpResponse<String> response = sendRequest("/request", "POST", request.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }
    @Test
    @Order(3)
    public void requestTripFail400B() throws JSONException, IOException, InterruptedException {
        // 400 BAD REQUEST WRONG TYPE
        JSONObject request = new JSONObject()
                .put("uid", 2)
                .put("radius", "4");

        HttpResponse<String> response = sendRequest("/request", "POST", request.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }
    @Test
    @Order(4)
    public void requestTripFail400C() throws JSONException, IOException, InterruptedException {
        // 400 BAD REQUEST NEGATIVE RADIUS
        JSONObject request = new JSONObject()
                .put("uid", "2")
                .put("radius", -100);

        HttpResponse<String> response = sendRequest("/request", "POST", request.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(5)
    public void requestTripFail404() throws JSONException, IOException, InterruptedException {
        // 404 UID NOT FOUND
        JSONObject request = new JSONObject()
                .put("uid", "someRandomUidCantFind")
                .put("radius", 10);

        HttpResponse<String> response = sendRequest("/request", "POST", request.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    @Order(6)
    public void confirmTripPass() throws JSONException, IOException, InterruptedException {
        JSONObject request = new JSONObject()
                .put("driver", "7003")
                .put("passenger", "7001")
                .put("startTime", 12909841);

        HttpResponse<String> response = sendRequest("/confirm", "POST", request.toString());
        HttpResponse<String> responseIntermediate = sendRequest("/passenger/" + "7001", "GET", "");
        JSONObject data = new JSONObject(responseIntermediate.body());
        tripIdCreated = data.getJSONObject("data").getJSONArray("trips").getJSONObject(0).getString("_id");
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
    }

    @Test
    @Order(7)
    public void confirmTripFail400A() throws JSONException, IOException, InterruptedException {
        // 400 BAD REQUEST PARAMETER MISSING
        JSONObject request = new JSONObject()
                .put("driver", "1")
                .put("passenger", "2");

        HttpResponse<String> response = sendRequest("/confirm", "POST", request.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(8)
    public void confirmTripFail400B() throws JSONException, IOException, InterruptedException {
        // 400 BAD REQUEST WRONG TYPE
        JSONObject request = new JSONObject()
                .put("driver", 2)
                .put("passenger", 4)
                .put("startTime", "12303123");

        HttpResponse<String> response = sendRequest("/confirm", "POST", request.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(9)
    public void updateTripPass() throws JSONException, IOException, InterruptedException {
        // GET TRIP ID FOR 200 STATUS CODE?
        JSONObject request = new JSONObject()
                .put("distance", 150)
                .put("endTime", 1645919897)
                .put("timeElapsed", 40)
                .put("totalCost", "75.0");

        HttpResponse<String> response = sendRequest("/" + tripIdCreated, "PATCH", request.toString());
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
    }

    @Test
    @Order(10)
    public void updateTripFail404() throws JSONException, IOException, InterruptedException {
        // 404 Trip ID NOT FOUND
        String oid = new ObjectId().toString();
        JSONObject request = new JSONObject()
                .put("distance", 30)
                .put("endTime", 100)
                .put("timeElapsed", 250)
                .put("totalCost", "49.20");

        HttpResponse<String> response = sendRequest("/"+oid, "PATCH", request.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    @Order(11)
    public void updateTripFail400A() throws JSONException, IOException, InterruptedException {
        // 400 WRONG TYPE
        JSONObject request = new JSONObject()
                .put("distance", "30")
                .put("endTime", 100)
                .put("timeElapsed", 250)
                .put("totalCost", "49.20");

        HttpResponse<String> response = sendRequest("/validTripId", "PATCH", request.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(12)
    public void updateTripFail400B() throws JSONException, IOException, InterruptedException {
        // 400 Trip ID MISSING
        JSONObject request = new JSONObject()
                .put("distance", 30)
                .put("endTime", 100)
                .put("timeElapsed", 250)
                .put("totalCost", "49.20");

        HttpResponse<String> response = sendRequest("", "PATCH", request.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(13)
    public void getPassengerTripsPass() throws JSONException, IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("/passenger/" + "7001", "GET", "");
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
    }

    @Test
    @Order(14)
    public void getPassengerTripsFail400() throws JSONException, IOException, InterruptedException {
        // 400 Passenger Uid MISSING
        HttpResponse<String> response = sendRequest("/passenger", "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(15)
    public void getPassengerTripsFail404() throws JSONException, IOException, InterruptedException {
        // 404 Uid NOT FOUND
        String oid = new ObjectId().toString();
        HttpResponse<String> response = sendRequest("/passenger/" + oid, "GET", "");
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    @Order(16)
    public void getDriverTripsPass() throws JSONException, IOException, InterruptedException {

        HttpResponse<String> response = sendRequest("/driver/" + "7003", "GET", "");
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
    }

    @Test
    @Order(17)
    public void getDriverTripsFail400() throws JSONException, IOException, InterruptedException {
        // 400 Driver Uid MISSING
        HttpResponse<String> response = sendRequest("/driver", "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(18)
    public void getDriverTripsFail404() throws JSONException, IOException, InterruptedException {
        // 400 Driver Uid MISSING
        String oid = new ObjectId().toString();
        HttpResponse<String> response = sendRequest("/driver/" + oid, "GET", "");
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    @Order(19)
    public void getDriverTimePass() throws JSONException, IOException, InterruptedException {
        HttpResponse<String> responseIntermediate = sendRequest("/passenger/" + "7001", "GET", "");
        JSONObject data = new JSONObject(responseIntermediate.body());
        tripIdCreated = data.getJSONObject("data").getJSONArray("trips").getJSONObject(0).getString("_id");

        HttpResponse<String> response = sendRequest("/driverTime/" + tripIdCreated, "GET", "");
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
    }

    @Test
    @Order(20)
    public void getDriverTimeFail400() throws JSONException, IOException, InterruptedException {
        // 400 Trip ID MISSING
        HttpResponse<String> response = sendRequest("/driverTime", "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(21)
    public void getDriverTimeFail404() throws JSONException, IOException, InterruptedException {
        // 400 Trip ID MISSING
        String oid = new ObjectId().toString();
        HttpResponse<String> response = sendRequest("/driverTime/"+oid, "GET", "");
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }
}
