package ca.utoronto.utm.mcs;

/**
 * Everything you need in order to send and recieve httprequests to
 * other microservices is given here. Do not use anything else to send
 * and/or recieve http requests from other microservices. Any other
 * imports are fine.
 */

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import com.mongodb.util.JSON;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Request extends Endpoint {

    /**
     * POST /trip/request
     *
     * @return 200, 400, 404, 500
     * Returns a list of drivers within the specified radius
     * using location microservice. List should be obtained
     * from navigation endpoint in location microservice
     * @body uid, radius
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // TODO
        try {
            JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));

            // Type Checking for 400 Bad Request
            String fields[] = {"uid", "radius"};
            Class<?> fieldClasses[] = {String.class, Integer.class};
            if (!validateFields(body, fields, fieldClasses)) {
                this.sendStatus(r, 400);
                return;
            }

            String uid;
            int radius;

            if (body.length() == 2 && body.has("uid") && body.has("radius")) {
                uid = body.getString("uid");
                radius = body.getInt("radius");
                if (radius < 0) {
                    this.sendStatus(r, 400);
                    return;
                }
            } else {
                // wrong input body
                this.sendStatus(r, 400);
                return;
            }
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://locationmicroservice:8000/location/nearbyDriver/%s?radius=%d",
                            uid, radius)))
                    .method("GET", HttpRequest.BodyPublishers.ofString(""))
                    .build();
            HttpResponse<String> response = null;
            try {
                response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
                return;
            }

            ArrayList<String> driversUid = new ArrayList<>();
            JSONObject responseBody = new JSONObject(response.body());

            if (responseBody.getString("status").equals("BAD REQUEST")) {
                this.sendStatus(r, 400);
                return;
            } else if (responseBody.getString("status").equals("NOT FOUND")) {
                this.sendStatus(r, 404);
                return;
            } else {
                Iterator keys = responseBody.getJSONObject("data").keys();
                while (keys.hasNext()) {
                    String currKey = keys.next().toString();
                    driversUid.add(currKey);
                }
            }

            JSONObject resp = new JSONObject();
            resp.put("data", driversUid);

            this.sendResponse(r, resp, 200);
            return;

        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
