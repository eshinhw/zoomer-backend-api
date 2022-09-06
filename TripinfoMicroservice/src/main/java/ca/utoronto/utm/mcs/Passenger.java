package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCursor;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Passenger extends Endpoint {

    /**
     * GET /trip/passenger/:uid
     * @param uid
     * @return 200, 400, 404
     * Get all trips the passenger with the given uid has.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException,JSONException{
        // TODO
        // check if request url isn't malformed
        String[] splitUrl = r.getRequestURI().getPath().split("/");
        if (splitUrl.length != 4 || splitUrl[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        String uid = splitUrl[3];
        MongoCursor<Document> result;
        try {
            result = this.dao.getPassengerTrips(uid);
        } catch (Exception e) {
            this.sendStatus(r, 500);
            return;
        }
        // Uid not found or no trip 404
        if (!result.hasNext()) {
            this.sendStatus(r, 404);
            return;
        }

        JSONObject resp = new JSONObject();
        JSONObject data = new JSONObject();
        ArrayList<Document> tripList = new ArrayList<Document>();

        while (result.hasNext()) {
            Document curr = result.next();
            tripList.add(curr);
        }

        data.put("trips", tripList);
        resp.put("data", data);

        this.sendResponse(r, resp, 200);
        return;
    }
}
