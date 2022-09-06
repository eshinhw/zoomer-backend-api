package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCursor;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Driver extends Endpoint {

    /**
     * GET /trip/driver/:uid
     * @param uid
     * @return 200, 400, 404
     * Get all trips driver with the given uid has.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // TODO
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }
        String uid = params[3];
        MongoCursor<Document> result = this.dao.getDriverTrips(uid);

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
