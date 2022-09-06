package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends Endpoint {

    public static String addSingleQuote(String s) {
        return new StringBuilder()
                .append('\'')
                .append(s)
                .append('\'')
                .toString();
    }

    /**
     * POST /user/login
     * @body email, password
     * @return 200, 400, 401, 404, 500
     * Login a user into the system if the given information matches the 
     * information of the user in the database.
     */
    
    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // TODO
        String body = Utils.convert(r.getRequestBody());
        try {
            JSONObject deserialized = new JSONObject(body);

            String email, password;
            // check inputs, return 400 if missing inputs
            if (deserialized.length() == 2 && deserialized.has("email") && deserialized.has("password")) {
                email = deserialized.getString("email");
                password = deserialized.getString("password");
            } else {
                this.sendStatus(r, 400);
                return;
            }

            // check for unique email
            ResultSet rs;
            boolean resultHasNext;
            try {
                rs = this.dao.getUserByEmail(addSingleQuote(email));
                resultHasNext = rs.next();
            }
            catch (SQLException e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
                return;
            }

            // check if user was found
            // if resultHasNext == false, return 404 no user found
            if (!resultHasNext) {
                this.sendStatus(r, 404);
                return;
            }

            // check for password match
            try {
                rs = this.dao.getUserByEmailPassword(addSingleQuote(email), addSingleQuote(password));
                resultHasNext = rs.next();
            }
            catch (SQLException e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
                return;
            }
            // if resultHasNext == true, there is a user with password match
            // if resultHasNext == false, user's password doesn't match, return 403
            if (!resultHasNext) {
                this.sendStatus(r, 401);
                return;
            }

            // From here, we have a user with unique email and password match
            // There should be one user which satisfies the conditions
            // get data
            int uid;
            try {
                uid = rs.getInt("uid");
            } catch (SQLException e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
                return;
            }

            // making the response
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
