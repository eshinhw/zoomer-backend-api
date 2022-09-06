package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.util.ArrayList;

import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;

public class Navigation extends Endpoint {

    /**
     * GET /location/navigation/:driverUid?passengerUid=:passengerUid
     *
     * @param driverUid, passengerUid
     * @return 200, 400, 404, 500
     * Get the shortest path from a driver to passenger weighted by the
     * travel_time attribute on the ROUTE_TO relationship.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // TODO

        /*
        If any parameter is missing or
        if any value is of the wrong type or
        if the URI has extra arguments,
        status code should be 400
         */
        String[] params = r.getRequestURI().toString().split("/");

        if (params.length != 4
                || !params[3].contains("?passengerUid=")
                || params[3].split("\\?").length != 2) {
            this.sendStatus(r, 400);
            return;
        }

        String driverUid = params[3].split("\\?")[0];
        String passengerUid = params[3].split("=")[1];

        try {

            /*
            STATUS CODE 404 NOT FOUND
            If either driverUid or passengerUid is not in the database or
            if driverUid is not a driver or
            if a path between driver and passenger doesn't exist,
            status code should be 404
             */

            JSONObject inputTests = new JSONObject();

            Result driverResult = this.dao.getUserByUid(driverUid);
            Result passengerResult = this.dao.getUserByUid(passengerUid);

            if (!driverResult.hasNext() || !passengerResult.hasNext()) {
                // either driverUid or passengerUid not in DB
                this.sendStatus(r, 404);
                return;
            }

            Record driverRecord = driverResult.next();
            Record passengerRecord = passengerResult.next();

            if (!driverRecord.get(0).get("is_driver").asBoolean()) {
                // driverUid is not a driver
                this.sendStatus(r, 404);
                return;
            }

            String driverStreet = driverRecord.get(0).get("street").asString();
            String passengerStreet = passengerRecord.get(0).get("street").asString();

            Result path = this.dao.shortestPath(passengerStreet, driverStreet);
            this.dao.removeGraph();

            if (!path.hasNext()) {
                // a path between driver and passenger doesn't exist
                this.sendStatus(r, 404);
                return;
            }

            Record pathData = path.next();

            int total_time = pathData.get("totalCost").asInt();

            ArrayList<JSONObject> route = new ArrayList<>();

            for (int i=0; i < pathData.get("path").size(); i++) {
                JSONObject nodeProperties = new JSONObject();

                Node currNode = pathData.get("path").get(i).asNode();
                int cost = pathData.get("costs").get(i).asInt();

                nodeProperties.put("street", currNode.get("name").asString());
                nodeProperties.put("has_traffic", currNode.get("has_traffic").asBoolean());
                nodeProperties.put("time", cost);

                route.add(nodeProperties);
            }

            JSONObject resp = new JSONObject();
            JSONObject data = new JSONObject();

            data.put("total_time", total_time);
            data.put("route", route);
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
