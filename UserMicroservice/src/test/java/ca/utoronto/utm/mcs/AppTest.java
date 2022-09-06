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

/**
 * Please write your tests in this class. 
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest {

    final static String API_URL = "http://0.0.0.0:8000/user";

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws IOException, InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @Order(1)
    public void registerUserPass() throws JSONException, IOException, InterruptedException {
        JSONObject request = new JSONObject()
                .put("name", "Eddie")
                .put("email", "eddie@gmail.com")
                .put("password", "loveCS");

        String url = "/register";

        HttpResponse<String> response = sendRequest(url, "POST",
                request.toString());
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
    }

    @Test
    @Order(2)
    public void registerUserFail400() throws JSONException, IOException, InterruptedException {
        // 400 Missing Parameters
        JSONObject request = new JSONObject()
                .put("name", "Eddie");

        String url = "/register";

        HttpResponse<String> response = sendRequest(url, "POST",
                request.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(3)
    public void registerUserFail409() throws JSONException, IOException, InterruptedException {
        // User already exists
        JSONObject request = new JSONObject()
                .put("name", "Eddie")
                .put("email", "eddie@gmail.com")
                .put("password", "loveCS");

        String url = "/register";

        HttpResponse<String> response = sendRequest(url, "POST",
                request.toString());
        assertEquals(HttpURLConnection.HTTP_CONFLICT, response.statusCode());
    }

    @Test
    @Order(4)
    public void loginUserPass() throws JSONException, IOException, InterruptedException {
        JSONObject request = new JSONObject()
                .put("email", "eddie@gmail.com")
                .put("password", "loveCS");

        String url = "/login";

        HttpResponse<String> response = sendRequest(url, "POST",
                request.toString());
        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
    }

    @Test
    @Order(5)
    public void loginUserFail400() throws JSONException, IOException, InterruptedException {
        // 400 BAD REQUEST MISSING PARAMETERS
        JSONObject request = new JSONObject()
                .put("name", "Eddie");

        String url = "/login";

        HttpResponse<String> response = sendRequest(url, "POST",
                request.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    @Order(6)
    public void loginUserFail404() throws JSONException, IOException, InterruptedException {
        // 404 NOT FOUND USER DOESN'T EXIST

        JSONObject request = new JSONObject()
                .put("email", "something@new.com")
                .put("password", "doesn't_exist");

        String url = "/login";

        HttpResponse<String> response = sendRequest(url, "POST",
                request.toString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    @Order(7)
    public void loginUserFail401() throws JSONException, IOException, InterruptedException {
        // 403 WRONG PASSWORD FORBIDDEN

        JSONObject request = new JSONObject()
                .put("email", "eddie@gmail.com")
                .put("password", "wrong_password");

        String url = "/login";

        HttpResponse<String> response = sendRequest(url, "POST",
                request.toString());
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.statusCode());
    }


}
