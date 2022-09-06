package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCursor;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Trip extends Endpoint {

    /**
     * PATCH /trip/:_id
     *
     * @param _id
     * @return 200, 400, 404
     * Adds extra information to the trip with the given id when the
     * trip is done.
     * @body distance, endTime, timeElapsed, totalCost
     */

    @Override
    public void handlePatch(HttpExchange r) throws IOException, JSONException {
        // TODO

        String[] params = r.getRequestURI().toString().split("/");


        if (params.length != 3 || params[2].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        String tripId = params[2];

        try {
            JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));

            int distance, endTime, timeElapsed;
            String totalCost;

            String fields[] = {"distance", "endTime", "timeElapsed", "totalCost"};
            Class<?> fieldClasses[] = {Integer.class, Integer.class, Integer.class, String.class};
            if (!validateFields(body, fields, fieldClasses)) {
                this.sendStatus(r, 400);
                return;
            }

            if (body.length() == 4
                    && body.has("distance")
                    && body.has("endTime")
                    && body.has("timeElapsed")
                    && body.has("totalCost")) {
                distance = body.getInt("distance");
                endTime = body.getInt("endTime");
                timeElapsed = body.getInt("timeElapsed");
                totalCost = body.getString("totalCost");
            } else {
                // wrong input body
                this.sendStatus(r, 400);
                return;
            }

            MongoCursor<Document> result = this.dao.getTripById(tripId);

            // the trip ID is not in the DB
            if (!result.hasNext()) {
                this.sendStatus(r, 404);
                return;
            }

            this.dao.updateTrip(tripId, distance, endTime, timeElapsed, totalCost);
            this.sendStatus(r, 200);

        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
