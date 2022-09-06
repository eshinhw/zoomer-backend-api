package ca.utoronto.utm.mcs;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.sql.DriverManager;

import static com.mongodb.client.model.Filters.eq;

public class MongoDao {
	
	public MongoCollection<Document> collection;
	public MongoDatabase db;

	private final String username = "root";
	private final String password = "123456";
	Dotenv dotenv = Dotenv.load();
	String addr = dotenv.get("MONGODB_ADDR");
	private final String uriDb = String.format("mongodb://%s:%s@%s:27017", username, password, addr);
	private final String dbName = "trip";
	private final String collectionName = "trips";

	public MongoDao() {
        // TODO: 
        // Connect to the mongodb database and create the database and collection. 
        // Use Dotenv like in the DAOs of the other microservices.
		try {
			// connection to url stated above
			MongoClient mongoClient = MongoClients.create(uriDb);
			// Creating or getting the database
			this.db = mongoClient.getDatabase(this.dbName);
			this.collection = this.db.getCollection(this.collectionName);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// *** implement database operations here *** //

	public String confirmTrip(String driverUid, String passengerUid, Integer startTime) {
		String oid = new ObjectId().toString();
		Document doc = new Document()
				.append("_id", oid)
				.append("driver", driverUid)
				.append("passenger", passengerUid)
				.append("startTime", startTime);
		this.collection.insertOne(doc);
		return oid;
	}

	public MongoCursor<Document> getTripById(String tripId) {
		return this.collection.find(eq("_id", tripId)).cursor();
	}

	public MongoCursor<Document> getDriverTrips(String driverUid) {
		return this.collection.find(eq("driver", driverUid)).cursor();
	}

	public MongoCursor<Document> getPassengerTrips(String passengerUid) {
		return this.collection.find(eq("passenger", passengerUid)).cursor();
	}

	public void updateTrip(String tripId, Integer distance, Integer endTime, Integer timeElapsed, String totalCost) {
		this.collection.updateOne(eq("_id", tripId),
				Updates.combine(
						Updates.set("distance", distance),
						Updates.set("endTime", endTime),
						Updates.set("timeElapsed", timeElapsed),
						Updates.set("totalCost", totalCost)));
	}

	public void resetDatabase() {
		this.collection.drop();
		this.collection = this.db.getCollection(this.collectionName);
	}
}
