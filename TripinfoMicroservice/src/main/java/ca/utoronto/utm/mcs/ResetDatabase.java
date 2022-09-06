package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ResetDatabase extends Endpoint {

    /**
     * GET /trip/resetDatabase
     * @param uid
     * @return 200, 400, 404
     * Get all trips driver with the given uid has.
     */

    @Override
    public void handleDelete(HttpExchange r) throws IOException, JSONException {

        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 3 || !params[2].equals("resetDatabase")) {
            this.sendStatus(r, 400);
            return;
        }

        try {
            this.dao.resetDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
