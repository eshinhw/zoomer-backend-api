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

import com.mongodb.client.MongoCursor;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Drivetime extends Endpoint {

    /**
     * GET /trip/driverTime/:_id
     * @param _id
     * @return 200, 400, 404, 500
     * Get time taken to get from driver to passenger on the trip with
     * the given _id. Time should be obtained from navigation endpoint
     * in location microservice.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // TODO

        String[] params = r.getRequestURI().toString().split("/");

        if (params.length < 4 || params[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }



        String tripId = params[3];


        try {
            MongoCursor<Document> result = this.dao.getTripById(tripId);

            // No trip return 404
            if (!result.hasNext()) {
                this.sendStatus(r, 404);
                return;
            }

            Document doc = result.next();

            String driverUid = doc.getString("driver");
            String passengerUid = doc.getString("passenger");

            String locationUrl =
                    "http://locationmicroservice:8000/location/navigation/%s?passengerUid=%s";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            String.format(locationUrl, driverUid, passengerUid)))
                    .method("GET", HttpRequest.BodyPublishers.ofString(""))
                    .build();
            HttpResponse<String> response;
            try {
                response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
                return;
            }

            JSONObject responseBody = new JSONObject(response.body());

            if (responseBody.getString("status").equals("BAD REQUEST")) {
                this.sendStatus(r, 400);
            } else if (responseBody.getString("status").equals("NOT FOUND")) {
                this.sendStatus(r, 404);
            } else if (responseBody.getString("status").equals("INTERNAL SERVER ERROR")) {
                this.sendStatus(r, 500);
            } else {
                // STATUS CODE 200
                int arrivalTime = responseBody.getJSONObject("data").getInt("total_time");

                JSONObject resp = new JSONObject();
                JSONObject data = new JSONObject();

                data.put("arrival_time", arrivalTime);
                resp.put("data", data);

                this.sendResponse(r, resp, 200);
            }

        }
        catch (Exception e) {
            this.sendStatus(r, 500);
//            return;
        }

    }
}
