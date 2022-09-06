package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.util.PSQLException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Register extends Endpoint {

    public static String addSingleQuote(String s) {
        return new StringBuilder()
                .append('\'')
                .append(s)
                .append('\'')
                .toString();
    }

    /**
     * POST /user/register
     * @body name, email, password
     * @return 200, 400, 409, 500
     * Register a user into the system using the given information.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        try {
            // TODO
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);

            String name, email, password;

            if (deserialized.length() == 3 && deserialized.has("name") && deserialized.has("email") && deserialized.has("password")) {
                name = deserialized.getString("name");
                email = deserialized.getString("email");
                password = deserialized.getString("password");
            } else {
                this.sendStatus(r, 400);
                return;
            }

            ResultSet exists;
            boolean resultHasNext;

            try {
                exists = this.dao.getUserByEmail(addSingleQuote(email));
                resultHasNext = exists.next();
            } catch (SQLException e) {
                this.sendStatus(r, 500);
                return;
            }

            if (resultHasNext) {
                this.sendStatus(r, 409);
                return;
            }

            try {
                this.dao.registerUser(addSingleQuote(name), addSingleQuote(email), addSingleQuote(password));
            } catch (SQLException e) {
                this.sendStatus(r, 500);
                return;
            }

            //get uid for newly added user
            ResultSet rs;
            int uid;

            try {
                rs = this.dao.getUserByEmail(addSingleQuote(email));
                rs.next();
                uid = rs.getInt("uid");
            } catch (SQLException e) {
                this.sendStatus(r, 500);
                return;
            }

            JSONObject resp = new JSONObject();
            resp.put("uid", Integer.toString(uid));

            this.sendResponse(r, resp, 200);
            return;

        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
            return;
        }


    }
}
