package ca.utoronto.utm.mcs;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Everything you need in order to send and recieve httprequests to 
 * the microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.OutputStream;    // Also given to you to send back your response
import java.util.HashMap;


public class RequestRouter implements HttpHandler {

    /**
     * You may add and/or initialize attributes here if you
     * need.
     */
    public HttpClient client;
    public HashMap<Integer, String> errorMap;

    public RequestRouter() {
        this.client = HttpClient.newHttpClient();
        errorMap = new HashMap<>();
        errorMap.put(200, "OK");
        errorMap.put(400, "BAD REQUEST");
        errorMap.put(401, "UNAUTHORIZED");
        errorMap.put(404, "NOT FOUND");
        errorMap.put(405, "METHOD NOT ALLOWED");
        errorMap.put(409, "CONFLICT");
        errorMap.put(500, "INTERNAL SERVER ERROR");
    }

    public void sendStatus(HttpExchange r, int statusCode) throws JSONException, IOException {
        JSONObject res = new JSONObject();
        res.put("status", errorMap.get(statusCode));
        String response = res.toString();
        r.sendResponseHeaders(statusCode, response.length());
        this.writeOutputStream(r, response);
    }

    public void sendResponse(HttpExchange r, JSONObject obj, int statusCode) throws JSONException, IOException {
        obj.put("status", errorMap.get(statusCode));
        String response = obj.toString();
        r.sendResponseHeaders(statusCode, response.length());
        this.writeOutputStream(r, response);
    }

    public void writeOutputStream(HttpExchange r, String response) throws IOException {
        OutputStream os = r.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public HttpRequest requestBuilder(String method, String url, String requestBody) {
        HttpRequest req =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .method(method, HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
        return req;
    }

    @Override
    public void handle(HttpExchange r) {
        // TODO

        String url = r.getRequestURI().toString();
        String[] params = url.split("/");
        String microservice = params[1];

        try {
            switch (microservice) {
                case "location":
                    this.locationMicroservice(r);
                    break;
                case "trip":
                    this.tripMicroservice(r);
                    break;
                case "user":
                    this.userMicroservice(r);
                    break;
                default:
                    this.sendStatus(r, 405);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
//            return;
        }
    }

    public void locationMicroservice(HttpExchange r) throws JSONException, IOException {
        String containerUrl = "http://locationmicroservice:8000/location/";
        try {
            if (r.getRequestMethod().equalsIgnoreCase("GET")) {
                // GET User Information
                String[] params = r.getRequestURI().toString().split("/");
                HttpResponse<String> response = null;

                if (params.length == 3) {
                    HttpRequest request = requestBuilder(
                            "GET",
                            containerUrl + params[2],
                            "");
                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }

                }
                else if (params.length == 3 && params[2].contains("?radius=")) {
                    HttpRequest request = requestBuilder(
                            "GET",
                            containerUrl + params[2],
                            "");
                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }
                }
                else if (params.length == 4 && (params[2].equals("nearbyDriver") || params[2].equals("navigation"))) {
                    HttpRequest request = requestBuilder(
                            "GET",
                            containerUrl + params[2] + "/" + params[3],
                            "");
                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }
                }

                else {
                    this.sendStatus(r, 405);
                    return;
                }

                // convert String of response.body() to JSONObject
                JSONObject responseBody = new JSONObject(response.body());

                if (responseBody.getString("status").equals("BAD REQUEST")) {
                    this.sendStatus(r, 400);
                    return;
                }
                else if (responseBody.getString("status").equals("NOT FOUND")) {
                    this.sendStatus(r, 404);
                    return;
                }
                else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                    this.sendStatus(r, 500);
                    return;
                }
                else {
                    // STATUS CODE 200
                    JSONObject resp = new JSONObject();
                    JSONObject data = responseBody.getJSONObject("data");
                    resp.put("data", data);
                    this.sendResponse(r, resp, 200);
                    return;
                }
            }
            else if (r.getRequestMethod().equalsIgnoreCase("PATCH")) {
                // PATCH
                String[] params = r.getRequestURI().toString().split("/");
                HttpRequest request = requestBuilder(
                        "PATCH",
                        containerUrl + params[2],
                        Utils.convert(r.getRequestBody()));

                HttpResponse<String> response;
                try {
                    response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (Exception e) {
                    e.printStackTrace();
                    this.sendStatus(r, 500);
                    return;
                }

                // convert String of response.body() to JSONObject
                JSONObject responseBody = new JSONObject(response.body());

                if (responseBody.getString("status").equals("BAD REQUEST")) {
                    this.sendStatus(r, 400);
                    return;
                }
                else if (responseBody.getString("status").equals("NOT FOUND")) {
                    this.sendStatus(r, 404);
                    return;
                }
                else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                    this.sendStatus(r, 500);
                    return;
                }
                else {
                    // STATUS CODE 200
                    this.sendStatus(r, 200);
                    return;
                }

            }
            else if (r.getRequestMethod().equalsIgnoreCase("PUT")) {
                String[] params = r.getRequestURI().toString().split("/");
                HttpResponse<String> response;

                if (params.length == 3 && params[2].equals("user")) {
                    // ADD USER'S INITIAL LOCATION INFO
                    try {
                        HttpRequest request = requestBuilder(
                                "PUT",
                                containerUrl + params[2],
                                Utils.convert(r.getRequestBody()));

                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }

                }
                else if (params.length == 3 && params[2].equals("road")) {
                    // ADD/UPDATE ROAD
                    try {
                        HttpRequest request = requestBuilder(
                                "PUT",
                                containerUrl + params[2],
                                Utils.convert(r.getRequestBody()));

                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }
                }
                else {
                    this.sendStatus(r, 405);
                    return;
                }

                // convert String of response.body() to JSONObject
                JSONObject responseBody = new JSONObject(response.body());

                if (responseBody.getString("status").equals("BAD REQUEST")) {
                    this.sendStatus(r, 400);
                    return;
                }
                else if (responseBody.getString("status").equals("NOT FOUND")) {
                    this.sendStatus(r, 404);
                    return;
                }
                else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                    this.sendStatus(r, 500);
                    return;
                }
                else {
                    // STATUS CODE 200
                    this.sendStatus(r, 200);
                    return;
                }


            }
            else if (r.getRequestMethod().equalsIgnoreCase("POST")) {
                // ADD ROUTE TO DATABASE
                String[] params = r.getRequestURI().toString().split("/");
                HttpResponse<String> response;
                if (params.length == 3 && params[2].equals("hasRoute")) {
                    // ADD USER'S INITIAL LOCATION INFO
                    HttpRequest request = requestBuilder(
                            "POST",
                            containerUrl + params[2],
                            Utils.convert(r.getRequestBody()));
                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }

                }
                else {
                    this.sendStatus(r, 405);
                    return;
                }

                // convert String of response.body() to JSONObject
                JSONObject responseBody = new JSONObject(response.body());

                if (responseBody.getString("status").equals("BAD REQUEST")) {
                    this.sendStatus(r, 400);
                    return;
                }
                else if (responseBody.getString("status").equals("NOT FOUND")) {
                    this.sendStatus(r, 404);
                    return;
                }
                else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                    this.sendStatus(r, 500);
                    return;
                }
                else {
                    // STATUS CODE 200
                    this.sendStatus(r, 200);
                    return;
                }

            }
            else if (r.getRequestMethod().equalsIgnoreCase("DELETE")) {
                // DELETE USER FROM DB OR REMOVE A ROUTE
                String[] params = r.getRequestURI().toString().split("/");
                HttpResponse<String> response;
                if (params.length == 3 && params[2].equals("user")) {
                    // ADD USER'S INITIAL LOCATION INFO
                    HttpRequest request = requestBuilder(
                            "DELETE",
                            containerUrl + params[2],
                            Utils.convert(r.getRequestBody()));
                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }

                }
                else if (params.length == 3 && params[2].equals("route")) {
                    // ADD USER'S INITIAL LOCATION INFO
                    HttpRequest request = requestBuilder(
                            "DELETE",
                            containerUrl + params[2],
                            Utils.convert(r.getRequestBody()));
                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }

                }
                else {
                    this.sendStatus(r, 405);
                    return;
                }

                // convert String of response.body() to JSONObject
                JSONObject responseBody = new JSONObject(response.body());

                if (responseBody.getString("status").equals("BAD REQUEST")) {
                    this.sendStatus(r, 400);
                    return;
                }
                else if (responseBody.getString("status").equals("NOT FOUND")) {
                    this.sendStatus(r, 404);
                    return;
                }
                else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                    this.sendStatus(r, 500);
                    return;
                }
                else {
                    // STATUS CODE 200
                    this.sendStatus(r, 200);
                    return;
                }

            }
            else {
                // POST
                String[] params = r.getRequestURI().toString().split("/");
                HttpRequest request = requestBuilder(
                        "POST",
                        containerUrl + params[3],
                        r.getRequestBody().toString());

                HttpResponse<String> response;

                if (params[3].equals("register")) {
                    try {
                        response = this.client.send(request,
                                HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }
                    this.writeOutputStream(r, response.body());
                    return;
                } else if (params[3].equals("login")) {
                    try {
                        response = this.client.send(request,
                                HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }
                    this.writeOutputStream(r, response.body());
                    return;
                } else {
                    this.sendStatus(r, 405);
                    return;
                }

                }
        }
        catch(Exception e){
                e.printStackTrace();
                this.sendStatus(r, 405);
                return;
            }
    }

    public void tripMicroservice (HttpExchange r) throws JSONException, IOException {
        String containerUrl = "http://tripinfomicroservice:8000/trip/";
        try {
            if (r.getRequestMethod().equalsIgnoreCase("GET")) {
                // GET User Information
                String[] params = r.getRequestURI().toString().split("/");
                HttpResponse<String> response;

                if (params.length == 4
                        && (params[2].equals("passenger") ||
                            params[2].equals("driver") ||
                            params[2].equals("driverTime"))) {
                    HttpRequest request = requestBuilder(
                            "GET",
                            containerUrl + params[2] + "/" + params[3],
                            "");
                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }
                }
                else {
                    this.sendStatus(r, 405);
                    return;
                }

                // convert String of response.body() to JSONObject
                JSONObject responseBody = new JSONObject(response.body());

                if (responseBody.getString("status").equals("BAD REQUEST")) {
                    this.sendStatus(r, 400);
                    return;
                }
                else if (responseBody.getString("status").equals("NOT FOUND")) {
                    this.sendStatus(r, 404);
                    return;
                }
                else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                    this.sendStatus(r, 500);
                    return;
                }
                else {
                    // STATUS CODE 200
                    JSONObject resp = new JSONObject();
                    JSONObject data = responseBody.getJSONObject("data");
                    resp.put("data", data);
                    this.sendResponse(r, resp, 200);
                    return;
                }
            }
            else if (r.getRequestMethod().equalsIgnoreCase("PATCH")) {
                // PATCH
                String[] params = r.getRequestURI().toString().split("/");
                HttpRequest request = requestBuilder(
                        "PATCH",
                        containerUrl + params[2],
                        Utils.convert(r.getRequestBody()));

                HttpResponse<String> response;
                try {
                    response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (Exception e) {
                    e.printStackTrace();
                    this.sendStatus(r, 500);
                    return;
                }

                // convert String of response.body() to JSONObject
                JSONObject responseBody = new JSONObject(response.body());

                if (responseBody.getString("status").equals("BAD REQUEST")) {
                    this.sendStatus(r, 400);
                    return;
                }
                else if (responseBody.getString("status").equals("NOT FOUND")) {
                    this.sendStatus(r, 404);
                    return;
                }
                else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                    this.sendStatus(r, 500);
                    return;
                }
                else {
                    // STATUS CODE 200
                    this.sendStatus(r, 200);
                    return;
                }

            }
            else if (r.getRequestMethod().equalsIgnoreCase("POST")) {
                // REQUEST TRIP OR CONFIRM TRIP
                String[] params = r.getRequestURI().toString().split("/");
                HttpResponse<String> response = null;

                if (params.length == 3 && params[2].equals("request")) {
                    // ADD USER'S INITIAL LOCATION INFO
                    HttpRequest request = requestBuilder(
                            "POST",
                            containerUrl + params[2],
                            Utils.convert(r.getRequestBody()));
                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }

                    // convert String of response.body() to JSONObject
                    JSONObject responseBody = new JSONObject(response.body());

                    if (responseBody.getString("status").equals("BAD REQUEST")) {
                        this.sendStatus(r, 400);
                        return;
                    }
                    else if (responseBody.getString("status").equals("NOT FOUND")) {
                        this.sendStatus(r, 404);
                        return;
                    }
                    else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                        this.sendStatus(r, 500);
                        return;
                    }
                    else {
                        // STATUS CODE 200
                        JSONObject resp = new JSONObject();
                        JSONArray data = responseBody.getJSONArray("data");
                        resp.put("data", data);
                        this.sendResponse(r, resp, 200);
                        return;
                    }

                }
                if (params.length == 3 && params[2].equals("confirm")) {
                    // ADD USER'S INITIAL LOCATION INFO
                    HttpRequest request = requestBuilder(
                            "POST",
                            containerUrl + params[2],
                            Utils.convert(r.getRequestBody()));
                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }
                    // convert String of response.body() to JSONObject
                    JSONObject responseBody = new JSONObject(response.body());

                    if (responseBody.getString("status").equals("BAD REQUEST")) {
                        this.sendStatus(r, 400);
                        return;
                    }
                    else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                        this.sendStatus(r, 500);
                        return;
                    }
                    else {
                        // STATUS CODE 200
                        JSONObject resp = new JSONObject();
                        JSONObject data = responseBody.getJSONObject("data");
                        resp.put("data", data);
                        this.sendResponse(r, resp, 200);
                        return;
                    }

                }
                else {
                    this.sendStatus(r, 405);
                    return;
                }

            }
            else if (r.getRequestMethod().equalsIgnoreCase("DELETE")) {
                // REQUEST TRIP OR CONFIRM TRIP
                String[] params = r.getRequestURI().toString().split("/");
                HttpResponse<String> response = null;
                if (params.length == 3 && params[2].equals("resetDatabase")) {

                    HttpRequest request = requestBuilder(
                            "DELETE",
                            containerUrl + params[2],
                            Utils.convert(r.getRequestBody()));
                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }

                    // convert String of response.body() to JSONObject
                    JSONObject responseBody = new JSONObject(response.body());

                    if (responseBody.getString("status").equals("BAD REQUEST")) {
                        this.sendStatus(r, 400);
                        return;
                    }
                    else if (responseBody.getString("status").equals("NOT FOUND")) {
                        this.sendStatus(r, 404);
                        return;
                    }
                    else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                        this.sendStatus(r, 500);
                        return;
                    }
                    else {
                        // STATUS CODE 200
                        this.sendStatus(r, 200);
                        return;
                    }

                }
                else {
                    this.sendStatus(r, 405);
                    return;
                }

            }
            else {
                this.sendStatus(r, 405);
                return;

            }
        }
        catch(Exception e){
            e.printStackTrace();
            this.sendStatus(r, 405);
            return;
        }
    }

    public void userMicroservice (HttpExchange r) throws JSONException, IOException {
        String containerUrl = "http://usermicroservice:8000/user/";

        try {
            if (r.getRequestMethod().equalsIgnoreCase("GET")) {
                // GET User Information
                String[] params = r.getRequestURI().toString().split("/");
                if (params.length == 3 && params[1].equals("user")) {
                    HttpRequest request = requestBuilder(
                            "GET",
                            containerUrl + params[2],
                            Utils.convert(r.getRequestBody()));

                    HttpResponse<String> response = null;

                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }

                    // convert String of response.body() to JSONObject
                    JSONObject responseBody = new JSONObject(response.body());

                    if (responseBody.getString("status").equals("BAD REQUEST")) {
                        this.sendStatus(r, 400);
                        return;
                    }
                    else if (responseBody.getString("status").equals("NOT FOUND")) {
                        this.sendStatus(r, 404);
                        return;
                    }
                    else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                        this.sendStatus(r, 500);
                        return;
                    }
                    else {
                        // STATUS CODE 200
                        JSONObject resp = new JSONObject();
                        JSONObject data = responseBody.getJSONObject("data");
                        resp.put("data", data);
                        this.sendResponse(r, resp, 200);
                        return;
                    }
                }
                else {
                    this.sendStatus(r, 405);
                    return;
                }
            }
            else if (r.getRequestMethod().equalsIgnoreCase("PATCH")) {
                // PATCH
                String[] params = r.getRequestURI().toString().split("/");
                if (params.length == 3) {
                    HttpRequest request = requestBuilder(
                            "PATCH",
                            containerUrl + params[2],
                            Utils.convert(r.getRequestBody()));

                    HttpResponse<String> response = null;
                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }

                    // convert String of response.body() to JSONObject
                    JSONObject responseBody = new JSONObject(response.body());

                    if (responseBody.getString("status").equals("BAD REQUEST")) {
                        this.sendStatus(r, 400);
                        return;
                    }
                    else if (responseBody.getString("status").equals("NOT FOUND")) {
                        this.sendStatus(r, 404);
                        return;
                    }
                    else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                        this.sendStatus(r, 500);
                        return;
                    }
                    else {
                        // STATUS CODE 200
                        this.sendStatus(r, 200);
                        return;
                    }

                }


            }
            else if (r.getRequestMethod().equalsIgnoreCase("POST")) {

                String[] params = r.getRequestURI().toString().split("/");
                HttpResponse<String> response = null;
                if (params.length == 3 && params[2].equals("register")) {

                    HttpRequest request = requestBuilder(
                            "POST",
                            containerUrl + params[2],
                            Utils.convert(r.getRequestBody()));
                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }

                    // convert String of response.body() to JSONObject
                    JSONObject responseBody = new JSONObject(response.body());

                    if (responseBody.getString("status").equals("BAD REQUEST")) {
                        this.sendStatus(r, 400);
                        return;
                    }
                    else if (responseBody.getString("status").equals("NOT FOUND")) {
                        this.sendStatus(r, 404);
                        return;
                    }
                    else if (responseBody.getString("status").equals("CONFLICT")) {
                        this.sendStatus(r, 409);
                        return;
                    }
                    else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                        this.sendStatus(r, 500);
                        return;
                    }
                    else {
                        // STATUS CODE 200
                        JSONObject resp = new JSONObject();
                        String  data = responseBody.getString("uid");
                        resp.put("uid", data);
                        this.sendResponse(r, resp, 200);
                        return;
                    }

                }
                else if (params.length == 3 && params[2].equals("login")) {
                    // ADD USER'S INITIAL LOCATION INFO
                    HttpRequest request = requestBuilder(
                            "POST",
                            containerUrl + params[2],
                            Utils.convert(r.getRequestBody()));
                    try {
                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.sendStatus(r, 500);
                        return;
                    }
                    // convert String of response.body() to JSONObject
                    JSONObject responseBody = new JSONObject(response.body());

                    if (responseBody.getString("status").equals("BAD REQUEST")) {
                        this.sendStatus(r, 400);
                        return;
                    }
                    else if (responseBody.getString("status").equals("NOT FOUND")) {
                        this.sendStatus(r, 404);
                        return;
                    }
                    else if (responseBody.getString("status").equals("UNAUTHORIZED")) {
                        this.sendStatus(r, 401);
                        return;
                    }
                    else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                        this.sendStatus(r, 500);
                        return;
                    }
                    else {
                        // STATUS CODE 200
                        JSONObject resp = new JSONObject();
                        JSONObject data = responseBody.getJSONObject("uid");
                        resp.put("uid", data);
                        this.sendResponse(r, resp, 200);
                        return;
                    }

                }
                else {
                    this.sendStatus(r, 405);
                    return;
                }

            }
//            else if (r.getRequestMethod().equalsIgnoreCase("DELETE")) {
//                String[] params = r.getRequestURI().toString().split("/");
//                HttpResponse<String> response = null;
//                if (params.length == 3 && params[2].equals("resetDatabase")) {
//
//                    HttpRequest request = requestBuilder(
//                            "DELETE",
//                            containerUrl + params[2],
//                            Utils.convert(r.getRequestBody()));
//                    try {
//                        response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        this.sendStatus(r, 500);
//                        return;
//                    }
//
//                    // convert String of response.body() to JSONObject
//                    JSONObject responseBody = new JSONObject(response.body());
//
//                    if (responseBody.getString("status").equals("BAD REQUEST")) {
//                        this.sendStatus(r, 400);
//                        return;
//                    }
//                    else if (responseBody.getString("status").equals("NOT FOUND")) {
//                        this.sendStatus(r, 404);
//                        return;
//                    }
//                    else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
//                        this.sendStatus(r, 500);
//                        return;
//                    }
//                    else {
//                        // STATUS CODE 200
//                        this.sendStatus(r, 200);
//                        return;
//                    }
//
//                }
//                else {
//                    this.sendStatus(r, 405);
//                    return;
//                }
//
//            }
            else {
                this.sendStatus(r, 405);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 405);
            return;
        }

    }


    }
