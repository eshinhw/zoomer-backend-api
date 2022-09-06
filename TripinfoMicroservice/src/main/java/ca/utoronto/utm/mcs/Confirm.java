package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Confirm extends Endpoint {

    /**
     * POST /trip/confirm
     *
     * @return 200, 400
     * Adds trip info into the database after trip has been requested.
     * @body driver, passenger, startTime
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // TODO

        try {
            JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));

            String fields[] = {"driver", "passenger", "startTime"};
            Class<?> fieldClasses[] = {String.class, String.class, Integer.class};
            if (!validateFields(body, fields, fieldClasses)) {
                this.sendStatus(r, 400);
                return;
            }

            String driverUid, passengerUid;
            int startTime;

            if (body.length() == 3 && body.has("driver") && body.has("passenger") && body.has("startTime")) {
                driverUid = body.getString("driver");
                passengerUid = body.getString("passenger");
                startTime = body.getInt("startTime");
            } else {
                // wrong input body
                this.sendStatus(r, 400);
                return;
            }

            String oid = this.dao.confirmTrip(driverUid, passengerUid, startTime);

            JSONObject resp = new JSONObject();
            JSONObject data = new JSONObject();
            JSONObject _id = new JSONObject();

            _id.put("$oid", oid);
            data.put("_id", _id);
            resp.put("data", data);

            this.sendResponse(r, resp, 200);
            return;

        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }

    }
}
