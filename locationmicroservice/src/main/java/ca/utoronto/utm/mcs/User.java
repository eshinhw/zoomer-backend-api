package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.util.ArrayList;

import org.json.*;
import org.neo4j.driver.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;

public class User extends Endpoint {

    /**
     * PUT /location/user/
     * @body uid, is_driver
     * @return 200, 400, 404, 500 
     * Add a user into the database with attributes longitude and latitude 
     * initialized as 0, the “street” attribute must be initially set as 
     * an empty string.
     */

    @Override
    public void handlePut(HttpExchange r) throws IOException, JSONException {
        
        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
        String fields[] = {"uid", "is_driver"};
        Class<?> fieldClasses[] = {String.class, Boolean.class};
        if (!validateFields(body, fields, fieldClasses)) {
            this.sendStatus(r, 400);
            return;
        }

        String uid = body.getString("uid");
        boolean is_driver = body.getBoolean("is_driver");

        try {
            Result userCheck = this.dao.getUserByUid(uid);
            if (userCheck.hasNext()) {
                Result updateRes = this.dao.updateUserIsDriver(uid, is_driver);
                if (!updateRes.hasNext()) {
                    this.sendStatus(r, 500);
                    return;
                }
                this.sendResponse(r, new JSONObject(), 200);
                return;
            } else {
                Result addRes = this.dao.addUser(uid, is_driver);
                if (!addRes.hasNext()) {
                    this.sendStatus(r, 500);
                    return;
                }
                this.sendResponse(r, new JSONObject(), 200);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }

    /**
     * DELETE /location/user/
     * @body uid
     * @return 200, 400, 404, 500 
     * Delete a user in the database.
     */

    @Override
    public void handleDelete(HttpExchange r) throws IOException, JSONException {

        JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));
        String fields[] = {"uid"};
        Class<?> fieldClasses[] = {String.class};
        if (!validateFields(body, fields, fieldClasses)) {
            this.sendStatus(r, 400);
            return;
        }
        
        String uid = body.getString("uid");

        try {
            Result userCheck = this.dao.getUserByUid(uid);
            if (userCheck.hasNext()) {
                Result deleteRes = this.dao.deleteUser(uid);
                if (!deleteRes.hasNext()) {
                    this.sendStatus(r, 500);
                    return;
                }
                this.sendResponse(r, new JSONObject(), 200);
            } else {
                this.sendStatus(r, 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }

    /**
     * GET /location/user/
     * @body uid
     * @return 200, 400, 404, 500
     * Get all users by drivers and customers
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // TODO

        try {
            Result drivers = this.dao.getAllDrivers();
            Result passengers = this.dao.getAllPassengers();

            JSONObject resp = new JSONObject();
            ArrayList<Record> driversList = new ArrayList<Record>();
            ArrayList<Record> passengersList = new ArrayList<Record>();

            if (!drivers.hasNext() && !passengers.hasNext()) {
                // no passengers and drivers
                this.sendResponse(r, resp, 404);
                return;
            } else {
                while (drivers.hasNext()) {
                    driversList.add(drivers.next());
                }

                while (passengers.hasNext()) {
                    passengersList.add(passengers.next());
                }
            }

            resp.put("drivers", driversList);
            resp.put("passengers", passengersList);

            this.sendResponse(r, resp, 200);
            return;


        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
            return;
        }


    }
}
