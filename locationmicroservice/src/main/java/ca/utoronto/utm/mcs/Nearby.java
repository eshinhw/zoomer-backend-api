package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.util.ArrayList;

import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

public class Nearby extends Endpoint {

    /**
     * GET /location/nearbyDriver/:uid?radius=:radius
     *
     * @param uid, radius
     * @return 200, 400, 404, 500
     * Get drivers that are within a certain radius around a user.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // TODO
        String[] params = r.getRequestURI().toString().split("/");
        JSONObject test = new JSONObject();

        if (params.length != 4
                || !params[3].contains("?radius=")
                || params[3].split("\\?").length != 2) {

            this.sendStatus(r, 400);
            return;
        }
        else {
            String uid = params[3].split("\\?")[0];
            String radiusStr = params[3].split("=")[1];
            double lat, longi;
            int radius = Integer.parseInt(radiusStr);

            if (radius < 0) {
                this.sendStatus(r, 400);
                return;
            }

            try {
                Result userCheck = this.dao.getUserByUid(uid);

                if (!userCheck.hasNext()) {
                    // uid doesn't exist
                    this.sendStatus(r, 404);
                    return;
                }
                // an user with uid exists
                // the current user is not a driver
                Result currLoc = this.dao.getUserLocationByUid(uid);
                if (!currLoc.hasNext()) {
                    this.sendStatus(r, 500);
                    return;
                }
                Record locRecord = currLoc.next();
                lat = locRecord.get("n.latitude").asDouble();
                longi = locRecord.get("n.longitude").asDouble();

                Result allDrivers = this.dao.getAllDrivers();
                ArrayList<Record> driversList = new ArrayList<Record>();

                JSONObject resp = new JSONObject();
                JSONObject data = new JSONObject();

                while (allDrivers.hasNext()) {
//                    driversList.add(allDrivers.next());
                    Record rec = allDrivers.next();
                    JSONObject currDriver = new JSONObject();

                    String driverUid = rec.get("n.uid").asString();
                    double currLat = rec.get("n.latitude").asDouble();
                    double currLongi = rec.get("n.longitude").asDouble();
                    String currStreet = rec.get("n.street").asString();

                    Result distanceData = this.dao.calculateDistance(lat, longi, currLat, currLongi);
                    double currDistance = distanceData.next().get("dist").asDouble();
                    double currDistanceKm = currDistance / 1000;

                    if (currDistanceKm < radius) {
                        currDriver.put("longitude", currLongi);
                        currDriver.put("latitude", currLat);
                        currDriver.put("street", currStreet);
                        data.put(driverUid, currDriver);
                    }
                }

                resp.put("data", data);

                this.sendResponse(r, resp, 200);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
                return;
            }

        }


    }
}
