import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
// import org.json.JSONObject;

public class UserData {

    public static String check(String first_name, String last_name, int user_id, String username) {
        // Start connection with MongoDB Server
        MongoClientURI connectionString = new MongoClientURI("mongodb://127.0.0.1:27017");

        // Setting the userDatabase and users collection
        MongoClient mongoClient = new MongoClient(connectionString);
        MongoDatabase database = mongoClient.getDatabase("userDatabase");
        MongoCollection<Document> collection = database.getCollection("users");

        // Checks if the user is already in the database. Returns a positive number if the user is already in the database.
        long found = collection.count(Document.parse("{id : " + Integer.toString(user_id) + "}"));

        if (found == 0) {
            Document doc = new Document("first_name", first_name)
                    .append("last_name", last_name)
                    .append("id", user_id)
                    .append("username", username);
            collection.insertOne(doc);
            mongoClient.close();
            System.out.println("User not exists in database. Written.");
            return "[user] no_exists";
        } else {
            System.out.println("User exists in database.");
            mongoClient.close();
            return "[user] exists";
        }
    }

    public static String addClass(int user_id, String crn) {
        MongoClientURI connectionString = new MongoClientURI("mongodb://127.0.0.1:27017");
        MongoClient mongoClient = new MongoClient(connectionString);
        MongoDatabase database = mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("trackedCRN");
        long found = collection.count(Document.parse("{id : " + Integer.toString(user_id) + "," + "crn : " + crn + "}"));
        if (found == 0) {
            Document doc = new Document("id", user_id)
                    .append("crn", crn);
            collection.insertOne(doc);
            mongoClient.close();
            System.out.println("Tracking new CRN.");
            return "[class] no_exists";
        } else {
            System.out.println("CRN already exists.");
            mongoClient.close();
            return "[class] exists";
        }
    }

    public static boolean checkUnique(String crn) {
        MongoClientURI connectionString = new MongoClientURI("mongodb://127.0.0.1:27017");
        MongoClient mongoClient = new MongoClient(connectionString);
        MongoDatabase database = mongoClient.getDatabase("crnDatabase");
        MongoCollection<Document> collection = database.getCollection("uniqueCRN");

        long found = collection.count(Document.parse("{id : " + crn));
        if (found > 0) {
            System.out.println("The class already exists");
            return false;
        }

        String[] classInfo = ClassTracker.getInfoArray(crn);
        if (classInfo == null) {
            System.out.println("Error: could not find class");
            return false;
        }

        Document doc = new Document("id", crn)
                .append("name", classInfo[0])
                .append("cap", classInfo[1])
                .append("remain", classInfo[2])
                .append("wait_cap", classInfo[3])
                .append("wait_remain", classInfo[4]);
        collection.insertOne(doc);
        mongoClient.close();
        return true;
    }
}
